# Contributing to Weather

Thanks for contributing to `weather`.

This repository is a monorepo for a weather alert platform plus a small daily data collection workflow.

## Scope

- `backend/`: Spring Boot API and domain logic
- `frontend/`: React/Vite UI
- `scripts/`: daily automation utilities
- `data/`: generated weather snapshots and daily history

## Contribution types

- bug fixes
- tests
- documentation improvements
- alert rule improvements
- cache/ingest/reliability improvements
- frontend usability improvements

## Issue intake

- Use the bug/feature templates under `.github/ISSUE_TEMPLATE/`.
- Include the affected area, reproduction steps, and exact validation commands.
- Keep one issue focused on one operator or user problem.

## Before opening a PR

1. Keep the change scoped to one problem.
2. Update tests or explain why tests were not needed.
3. Note any API, schema, or workflow impact.
4. If the change affects the daily automation, include the exact command you ran.

## Local verification

### Backend

```bash
cd backend
./mvnw test
```

If `./mvnw` is unavailable in your environment, use:

```bash
mvn test
```

### Frontend

```bash
cd frontend
npm install
npm run build
```

### Daily weather automation

```bash
python scripts/daily_weather_update.py
```

## Pull request notes

- Describe the user or operator problem first.
- Then describe the root cause.
- Then list the validation commands you actually ran.
- Keep unrelated formatting or refactors out of the same PR.

## Release hygiene

- Update `CHANGELOG.md` for any user-visible or operator-visible change.
- Use `docs/RELEASE_CHECKLIST.md` before cutting a release or announcing a major update.
- If the change affects daily automation output, note the expected data-file impact in the PR.
