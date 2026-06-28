# Release Checklist

Use this checklist before cutting a public release or announcing a significant update.

## Product

- Confirm the README matches the current backend/frontend setup.
- Confirm any new endpoints or UI behavior are documented.
- Summarize the user-visible change in one paragraph.

## Verification

### Backend

```bash
cd backend
mvn test
```

### Frontend

```bash
cd frontend
npm install
npm run build
```

### Daily automation

```bash
python scripts/daily_weather_update.py
```

## Maintainer evidence

- Update `CHANGELOG.md`
- Record validation commands that actually passed
- Confirm `docs/VERIFICATION_MATRIX.md` still matches the commands you ran
- Link the issue or task that motivated the change
- Note whether the change affects API consumers, operators, or daily automation

## Release note prompts

- What user problem does this release improve?
- What was the root cause or missing capability?
- How was it verified?
- What follow-up work remains?
