# [Feature] Post-v0.1.0 maintainer backlog and first public feedback loop

Public issue mirror: `lovewave02/weather#1`

## Area

automation

## What problem should this solve?

The repository now has contribution guidance and a first public release, but there is still no public issue-driven maintenance trail.
Without at least one visible maintainer backlog item, the repo still looks quiet even after `v0.1.0`.

## Proposed solution

Create a maintainer-tracked follow-up issue that makes the next public operations work explicit:

1. document the expected local verification matrix for backend, frontend, and daily automation
2. keep the local H2-based verification path free of false scheduled `shedlock` noise
3. collect the first round of public cleanup tasks after `v0.1.0`

## Progress

Completed on 2026-06-28:

- [x] documented the expected local verification matrix for backend, frontend, and daily automation
- [x] removed the false H2-backed scheduled `shedlock` noise from test-profile verification by gating scheduling infrastructure behind `weather.scheduling.enabled`
- [x] aligned release-facing maintainer docs and the contribution guide with the real backend verification command (`mvn test`) so public maintainer guidance matches the current repository state
- [x] reject impossible latitude/longitude values at the location API boundary and return field-level validation details for invalid coordinate payloads
- [x] return manual ingest run summaries from the API and surface them in the dashboard so operators can see fetched/new/unchanged/missed counts after a refresh
- [x] surface the alert flow in the dashboard so a user can create an alert user, add a rule for the selected city, and inspect recent alert events after ingest
- [x] let the dashboard reconnect an existing alert user by email and reload the current saved rules so the alert flow survives a fresh session
- [x] remember the last alert email in the browser and auto-reconnect that user after refresh so the alert flow keeps its working context across visits
- [x] let the dashboard disable an existing alert rule so revisit users can stop a saved notification without rebuilding their whole alert setup
- [x] let the dashboard re-enable a disabled alert rule and treat the same create action as reactivation instead of leaving disabled duplicates stranded
- [x] publish a copy-paste quickstart for the alert flow so a new user can try the current dashboard and API path without reading the whole repo first
- [x] return specific duplicate-create conflict details for users, locations, and alert rules so failed actions explain what already exists

Still open:

- [ ] collect the next public cleanup task that creates stronger adoption signal beyond maintainer-surface, operator-surface, and reusable alert-flow hardening

Current observed note:

- Rechecked on 2026-06-28: `mvn test` passes (`19 tests, 0 failures`) and the
  test profile now disables scheduling infrastructure cleanly, so the prior
  H2-backed `SHEDLOCK` table noise is no longer emitted during `SmokeTest`.
- Rechecked on 2026-06-28: release-facing maintainer docs now point to `mvn test`
  rather than `./mvnw test`, which matches the current repository because
  `backend/mvnw` is not present.
- Rechecked on 2026-06-28: the contribution guide now points directly to
  `mvn test` for the same reason, so public verification guidance is consistent
  across maintainer-facing docs.
- Rechecked on 2026-06-28: `mvn test` now also covers a controller-level
  regression that rejects out-of-range coordinates with `400 Bad Request`
  details (`latitude must be less than or equal to 90.0`,
  `longitude must be less than or equal to 180.0`) instead of accepting
  impossible world coordinates.
- Rechecked on 2026-06-28: manual ingest now returns an operator-visible
  summary payload (`totalLocations`, `fetchedLocations`, `insertedSnapshots`,
  `updatedSnapshots`, `unchangedSnapshots`, `providerMisses`, `alertsCreated`)
  and the React dashboard shows the same summary after `Ingest Now`; backend
  verification passed with `10 tests, 0 failures` and frontend `npm run build`
  also passed.
- Rechecked on 2026-06-28: the React dashboard now also exposes the existing
  alert APIs end-to-end for a basic user flow: create alert user, create rule
  for the selected city, run ingest, and reload recent alert events. Frontend
  production build passed after the new flow was wired up.
- Rechecked on 2026-06-28: the same dashboard can now reconnect an existing
  alert user by email and reload the current saved rules through new lightweight
  backend lookup/list endpoints; backend verification passed with `13 tests, 0
  failures` and frontend `npm run build` also passed.
- Rechecked on 2026-06-28: the dashboard now also remembers the last alert
  email in browser storage and auto-reconnects that user after refresh when the
  account still exists, so the alert flow no longer drops back to a manual
  re-entry step between visits. Frontend `npm run build` still passed.
- Rechecked on 2026-06-28: alert rules can now be disabled from the dashboard
  through a small backend action endpoint, so revisit users can turn off a
  saved rule without recreating their whole alert setup. Backend verification
  passed with `17 tests, 0 failures` and frontend `npm run build` also passed.
- Rechecked on 2026-06-28: disabled alert rules can now be re-enabled from the
  dashboard, and the same create action reactivates a matching disabled rule
  instead of failing with a dead-end duplicate conflict. Backend verification
  passed with `19 tests, 0 failures` and frontend `npm run build` also passed.
- Rechecked on 2026-06-28: the repository now also ships a copy-paste
  onboarding path in `docs/ALERT_FLOW_QUICKSTART.md` that covers the current
  UI and API alert lifecycle end-to-end, including create/load user, create
  rule, disable, re-enable, ingest, and alert inspection. Frontend `npm run
  build` still passed after the documentation refresh.
- Rechecked on 2026-06-28: `docs/VERIFICATION_MATRIX.md` now also reflects the
  current backend proof (`19 tests, 0 failures`) and the real alert lifecycle
  users see today, including reconnecting with the last alert email before
  disable/re-enable and ingest follow-up steps.
- Rechecked on 2026-06-28: duplicate creates no longer collapse into a generic
  `conflict` detail; the backend now returns specific messages for duplicate
  users, duplicate coordinates, and duplicate alert rules. Backend verification
  passed with `16 tests, 0 failures` and frontend `npm run build` still passed.

This issue should act as the public anchor for the next small maintenance changes instead of keeping the backlog private.

## How would we verify success?

- the issue is public and labeled `enhancement`
- the issue links to `v0.1.0`
- the issue contains concrete follow-up checks instead of vague roadmap language
- the next maintainer-facing patch can reference this issue directly
- the repository keeps the same checklist in `docs/VERIFICATION_MATRIX.md`
- the remaining open item is a real public follow-up, not another stale docs mismatch
