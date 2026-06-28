# Alert Flow Quickstart

This is the fastest copy-paste path for trying the current weather alert flow
without reading the whole repository first.

## Prerequisites

1. Start the backend.

```bash
cd backend
docker compose up -d --build
```

2. Start the frontend.

```bash
cd frontend
npm install
npm run dev
```

3. Open the UI.

- Frontend: `http://localhost:5173`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## UI Path

1. Click `Load Korea` if the city list is empty.
2. Click `Ingest Now` once so the dashboard has current snapshots.
3. Pick a city from the left grid.
4. In `Alert Flow`, enter an email and click `Create Alert User` or `Load Existing`.
5. Create a rule for the selected city.
6. Run `Ingest Now` again and reload alerts if you want to inspect the newest
   generated alert events.
7. Use `Disable Rule` or `Enable Rule` from `Current rules` to test the saved
   rule lifecycle.

## API Path

These examples assume the backend runs on `http://localhost:8080`.

Create a user:

```bash
curl -sS -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"alerts@example.com"}'
```

Look up an existing user:

```bash
curl -sS 'http://localhost:8080/api/v1/users/by-email?email=alerts@example.com'
```

List locations:

```bash
curl -sS http://localhost:8080/api/v1/locations
```

Create a rule after you know the `userId` and `locationId`:

```bash
curl -sS -X POST http://localhost:8080/api/v1/subscriptions \
  -H 'Content-Type: application/json' \
  -d '{
    "userId":"<user-id>",
    "locationId":"<location-id>",
    "ruleType":"TEMP_BELOW",
    "threshold":20
  }'
```

List current rules:

```bash
curl -sS 'http://localhost:8080/api/v1/subscriptions?userId=<user-id>'
```

Disable a rule:

```bash
curl -sS -X POST http://localhost:8080/api/v1/subscriptions/<subscription-id>/disable
```

Re-enable a rule:

```bash
curl -sS -X POST http://localhost:8080/api/v1/subscriptions/<subscription-id>/enable
```

Run ingest:

```bash
curl -sS -X POST http://localhost:8080/api/v1/ingest/run
```

List alert events:

```bash
curl -sS http://localhost:8080/api/v1/users/<user-id>/alerts
```

## Verification

For quickstart changes, keep the public proof lightweight:

```bash
cd frontend
npm run build
```

When the quickstart relies on backend behavior that changed in the same bundle,
also run:

```bash
cd backend
mvn test
```
