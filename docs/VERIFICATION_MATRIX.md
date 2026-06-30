# Verification Matrix

This document is the maintainer-facing verification baseline for follow-up work
after `v0.1.0`.

Use it when an issue or PR changes backend behavior, frontend behavior, or the
daily weather automation.

Related public backlog:

- Issue: `#1` Post-v0.1.0 maintainer backlog and first public feedback loop
- Release: `v0.1.0`

## Default commands

### Backend

Run from `backend/`:

```bash
mvn test
```

What this covers:

- domain and service regression tests
- API-level smoke coverage in the current local test setup

### Frontend

Run from `frontend/`:

```bash
npm install
npm run build
```

What this covers:

- type-safe production build
- basic bundling sanity for UI changes

### Daily automation

Run from the repository root:

```bash
python scripts/daily_weather_update.py
```

What this covers:

- latest weather snapshot refresh
- historical file write path
- daily log append flow

### Alert flow smoke

Run from the repository root after the backend is up:

```bash
python scripts/alert_flow_smoke.py
```

What this covers:

- backend health readiness
- fresh location plus user/rule lookup or creation path
- disable/enable rule lifecycle
- manual ingest plus alert listing summary, including one real alert event on
  the default fresh-location path
- manual outbox dispatch proof, including one `SENT` alert event on the same
  default path

## When to run which checks

| Change type | Minimum verification |
| --- | --- |
| Backend API/domain change | `mvn test` |
| Frontend UI/data-fetch change | `npm install && npm run build` |
| Daily automation/script change | `python scripts/daily_weather_update.py` |
| Alert-flow operator smoke path | `python scripts/alert_flow_smoke.py` |
| Cross-cutting release prep | all three command groups |

## Current follow-up gaps

These are the first post-`v0.1.0` maintainer checks to keep visible:

1. Keep the backend/frontend verification commands current as the repo changes.
2. Keep local test-profile scheduling deterministic so backend verification does
   not emit false `shedlock` errors.
3. Tie future cleanup work back to a public issue or release note instead of
   keeping it private.
4. Keep one copy-paste onboarding path current so a new user can try the alert
   flow without reading the whole codebase first.
5. Keep one repeatable smoke script current so maintainers can verify the alert
   lifecycle without stepping through the UI manually.
6. Keep one public usage-feedback intake path current so quick-demo friction
   turns into issue-driven follow-up work.
7. Keep the quick-demo docs explicit about the expected success checkpoints so
   a first-run user knows when the flow actually worked before filing feedback.

### Observed on 2026-06-28

- `mvn test` finished with `BUILD SUCCESS` and `Tests run: 21, Failures: 0,
  Errors: 0`.
- The test profile now disables the scheduling infrastructure through
  `weather.scheduling.enabled=false`, so the previous H2/ShedLock noise no
  longer appears during `SmokeTest`.
- The public alert-flow quickstart should stay aligned with the real dashboard
  and API surface: create/load user, create rule, reconnect with the last
  alert email, disable, re-enable, ingest, manually dispatch pending alerts,
  and inspect `sentAt` on delivered alerts.
- The repository now also has a repeatable API smoke path in
  `scripts/alert_flow_smoke.py`; keep it aligned with the same alert lifecycle
  so maintainers can verify the flow without a browser session and still prove
  one real alert event plus manual dispatcher processing on the default path.
- Public intake should now distinguish reproducible bugs/features from
  quick-demo or adoption feedback so first-run friction can land in a narrower
  follow-up issue instead of being dropped into a vague roadmap bucket.
- Public quick-demo docs should call out the visible success checkpoints and the
  usage-feedback handoff together so a first-run user can tell whether the
  alert flow actually succeeded before opening the next follow-up.

Treat future scheduler noise in tests as a regression.

## Maintainer note

When a change is released, copy only the commands that actually passed into the
PR description or release notes.
