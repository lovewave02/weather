#!/usr/bin/env python3
from __future__ import annotations

import json
from datetime import datetime
from pathlib import Path
from urllib.parse import urlencode
from urllib.request import urlopen

ROOT = Path(__file__).resolve().parents[1]
LATEST_PATH = ROOT / "data" / "latest-seoul-weather.json"
HISTORY_DIR = ROOT / "data" / "history"
LOG_PATH = ROOT / "DAILY_IMPROVEMENTS.md"


def fetch_weather() -> dict:
    params = {
        "latitude": "37.5665",
        "longitude": "126.9780",
        "timezone": "Asia/Seoul",
        "current": "temperature_2m,apparent_temperature,precipitation_probability,pm2_5,pm10",
        "daily": "temperature_2m_min,temperature_2m_max,precipitation_probability_max",
    }
    url = "https://api.open-meteo.com/v1/forecast?" + urlencode(params)
    with urlopen(url, timeout=20) as resp:
        return json.loads(resp.read().decode("utf-8"))


def append_daily_log(kst_date: str, payload: dict) -> None:
    current = payload.get("current", {})
    daily = payload.get("daily", {})
    min_temp = (daily.get("temperature_2m_min") or [None])[0]
    max_temp = (daily.get("temperature_2m_max") or [None])[0]
    line = (
        f"- {kst_date}: 서울 날씨 데이터 자동 업데이트 "
        f"(현재 {current.get('temperature_2m')}°C, 체감 {current.get('apparent_temperature')}°C, "
        f"오늘 {min_temp}~{max_temp}°C)"
    )

    if not LOG_PATH.exists():
        LOG_PATH.write_text("# Daily Improvements\n\n", encoding="utf-8")

    content = LOG_PATH.read_text(encoding="utf-8")
    if kst_date in content:
        return

    with LOG_PATH.open("a", encoding="utf-8") as f:
        f.write(line + "\n")


def main() -> None:
    payload = fetch_weather()
    kst_now = datetime.fromisoformat(payload["current"]["time"])
    kst_date = kst_now.date().isoformat()

    LATEST_PATH.parent.mkdir(parents=True, exist_ok=True)
    HISTORY_DIR.mkdir(parents=True, exist_ok=True)

    normalized = {
        "updated_at_kst": payload["current"]["time"],
        "location": "Seoul",
        "current": payload.get("current", {}),
        "daily": {
            "date": (payload.get("daily", {}).get("time") or [None])[0],
            "temperature_2m_min": (payload.get("daily", {}).get("temperature_2m_min") or [None])[0],
            "temperature_2m_max": (payload.get("daily", {}).get("temperature_2m_max") or [None])[0],
            "precipitation_probability_max": (payload.get("daily", {}).get("precipitation_probability_max") or [None])[0],
        },
    }

    LATEST_PATH.write_text(json.dumps(normalized, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (HISTORY_DIR / f"{kst_date}.json").write_text(
        json.dumps(normalized, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    append_daily_log(kst_date, payload)


if __name__ == "__main__":
    main()
