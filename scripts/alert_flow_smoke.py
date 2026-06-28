#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import sys
import time
from dataclasses import dataclass
from datetime import UTC, datetime
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urljoin
from urllib.request import Request, urlopen

DEFAULT_LOCATION = {
    "name": "Seoul",
    "latitude": 37.5665,
    "longitude": 126.9780,
}


class ApiFailure(RuntimeError):
    pass


@dataclass
class Client:
    base_url: str
    timeout: float

    def request(self, method: str, path: str, *, query: dict[str, Any] | None = None, body: Any | None = None) -> Any:
        url = urljoin(self.base_url, path)
        if query:
            url = f"{url}?{urlencode(query)}"

        payload: bytes | None = None
        headers = {"Content-Type": "application/json"}
        if body is not None:
            payload = json.dumps(body).encode("utf-8")

        request = Request(url, data=payload, method=method, headers=headers)
        try:
            with urlopen(request, timeout=self.timeout) as response:
                raw = response.read().decode("utf-8")
                if not raw:
                    return None
                return json.loads(raw)
        except HTTPError as exc:
            detail = _read_error_detail(exc)
            raise ApiFailure(f"{method} {path} -> {exc.code}: {detail}") from exc
        except URLError as exc:
            raise ApiFailure(f"{method} {path} -> network error: {exc.reason}") from exc


def _read_error_detail(exc: HTTPError) -> str:
    try:
        body = exc.read().decode("utf-8")
    except Exception:
        return exc.reason or "request failed"

    if not body:
        return exc.reason or "request failed"

    try:
        payload = json.loads(body)
    except json.JSONDecodeError:
        return body

    if isinstance(payload, dict):
        return str(payload.get("detail") or payload.get("title") or body)
    return body


def wait_for_health(client: Client, retries: int, interval: float) -> dict[str, Any]:
    last_error: Exception | None = None
    for _ in range(retries):
        try:
            response = client.request("GET", "/actuator/health")
            if response.get("status") == "UP":
                return response
            last_error = ApiFailure(f"health status is {response.get('status')!r}")
        except Exception as exc:  # noqa: BLE001
            last_error = exc
        time.sleep(interval)
    raise ApiFailure(f"backend health check did not become ready: {last_error}")


def ensure_location(client: Client, requested_name: str) -> dict[str, Any]:
    locations = client.request("GET", "/api/v1/locations")
    requested = requested_name.strip().lower()

    for location in locations:
        if str(location.get("name", "")).strip().lower() == requested:
            return location

    if locations:
        return locations[0]

    created = client.request("POST", "/api/v1/locations", body=DEFAULT_LOCATION)
    return created


def ensure_user(client: Client, email: str) -> dict[str, Any]:
    try:
        return client.request("GET", "/api/v1/users/by-email", query={"email": email})
    except ApiFailure as exc:
        if "404" not in str(exc):
            raise
    return client.request("POST", "/api/v1/users", body={"email": email})


def find_matching_subscription(subscriptions: list[dict[str, Any]], location_id: str, rule_type: str, threshold: float) -> dict[str, Any] | None:
    for subscription in subscriptions:
        if (
            subscription.get("locationId") == location_id
            and subscription.get("ruleType") == rule_type
            and float(subscription.get("threshold")) == threshold
        ):
            return subscription
    return None


def default_threshold_for_rule(rule_type: str) -> float:
    if rule_type == "TEMP_BELOW":
        return 100.0
    if rule_type == "TEMP_ABOVE":
        return -100.0
    return 0.0


def ensure_subscription(
    client: Client,
    *,
    user_id: str,
    location_id: str,
    rule_type: str,
    threshold: float,
) -> tuple[dict[str, Any], str]:
    payload = {
        "userId": user_id,
        "locationId": location_id,
        "ruleType": rule_type,
        "threshold": threshold,
    }

    try:
        created = client.request("POST", "/api/v1/subscriptions", body=payload)
        status = "created" if created.get("enabled", True) else "updated"
        return created, status
    except ApiFailure as exc:
        if "409" not in str(exc):
            raise

    subscriptions = client.request("GET", "/api/v1/subscriptions", query={"userId": user_id})
    matched = find_matching_subscription(subscriptions, location_id, rule_type, threshold)
    if not matched:
        raise ApiFailure("subscription create failed with conflict, but no matching rule was found afterward")
    return matched, "reused"


def main() -> int:
    parser = argparse.ArgumentParser(description="Run a local smoke test for the weather alert flow.")
    parser.add_argument("--base-url", default="http://localhost:8080", help="Backend base URL.")
    parser.add_argument("--email", help="Email to use for the smoke user.")
    parser.add_argument("--location-name", default="Seoul", help="Preferred location name to exercise.")
    parser.add_argument("--rule-type", default="TEMP_BELOW", choices=["TEMP_BELOW", "TEMP_ABOVE", "PRECIP_ABOVE"])
    parser.add_argument("--threshold", type=float, help="Threshold to apply. Defaults to an alert-friendly value for the selected rule type.")
    parser.add_argument("--skip-toggle", action="store_true", help="Skip disable/enable lifecycle exercise.")
    parser.add_argument("--skip-ingest", action="store_true", help="Skip the ingest + alert listing step.")
    parser.add_argument("--health-retries", type=int, default=20)
    parser.add_argument("--health-interval", type=float, default=2.0)
    parser.add_argument("--timeout", type=float, default=20.0)
    args = parser.parse_args()

    timestamp = datetime.now(UTC).strftime("%Y%m%d%H%M%S")
    email = args.email or f"alerts-smoke-{timestamp}@example.com"
    threshold = args.threshold if args.threshold is not None else default_threshold_for_rule(args.rule_type)

    client = Client(base_url=args.base_url.rstrip("/") + "/", timeout=args.timeout)

    try:
        health = wait_for_health(client, retries=args.health_retries, interval=args.health_interval)
        location = ensure_location(client, args.location_name)
        user = ensure_user(client, email)
        subscription, subscription_status = ensure_subscription(
            client,
            user_id=user["id"],
            location_id=location["id"],
            rule_type=args.rule_type,
            threshold=threshold,
        )

        toggled: list[str] = []
        if not args.skip_toggle:
            if subscription.get("enabled", True):
                subscription = client.request("POST", f"/api/v1/subscriptions/{subscription['id']}/disable")
                toggled.append("disabled")
            subscription = client.request("POST", f"/api/v1/subscriptions/{subscription['id']}/enable")
            toggled.append("enabled")

        ingest_result = None
        alerts: list[dict[str, Any]] = []
        if not args.skip_ingest:
            ingest_result = client.request("POST", "/api/v1/ingest/run")
            alerts = client.request("GET", f"/api/v1/users/{user['id']}/alerts")

        summary = {
            "health": health.get("status"),
            "user": {"id": user["id"], "email": user["email"]},
            "location": {"id": location["id"], "name": location["name"]},
            "subscription": {
                "id": subscription["id"],
                "ruleType": subscription["ruleType"],
                "threshold": subscription["threshold"],
                "enabled": subscription["enabled"],
                "status": subscription_status,
                "toggled": toggled,
            },
            "ingest": ingest_result,
            "alertCount": len(alerts),
            "latestAlert": alerts[0] if alerts else None,
        }
        print(json.dumps(summary, ensure_ascii=False, indent=2))
        return 0
    except ApiFailure as exc:
        print(str(exc), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
