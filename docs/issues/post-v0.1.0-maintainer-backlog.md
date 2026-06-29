# [Feature] Post-v0.1.0 maintainer backlog and first public feedback loop

Public issue mirror: `lovewave02/weather#1`

## Area

automation

## What problem should this solve?

The repository now has public maintainer trail artifacts, but it still needs a
single issue-driven backlog item that records what shipped after `v0.1.0` and
what adoption-facing gap still remains.

The repo no longer looks unmaintained; the remaining problem is that outside
usage/adoption signal is still weaker than the maintainer surface.

## Proposed solution

Keep one maintainer-tracked follow-up issue that makes the public operations
trail explicit:

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
- [x] add a repeatable alert-flow smoke script so maintainers can verify the same lifecycle without clicking through the UI manually
- [x] add a manual dispatch path so the same maintainer proof run can also verify outbox processing from `PENDING` to `SENT`
- [x] expose the same manual dispatch path in the dashboard so an outsider can finish the alert flow in the UI and see `sentAt` without waiting for the scheduled dispatcher
- [x] return specific duplicate-create conflict details for users, locations, and alert rules so failed actions explain what already exists
- [x] pull release, CI, and quick-demo signals into the README header so an outsider can trust the current public state faster
- [x] add a dedicated usage-feedback intake template so outsider quick-demo friction can turn into a focused public follow-up instead of staying implicit

Still open:

- [ ] collect the next public cleanup task that creates stronger adoption
      signal beyond maintainer-surface, operator-surface, reusable alert-flow
      hardening, release management, and smoke-proof automation

Current observed note:

- Rechecked on 2026-06-28: backend verification now passes with
  `21 tests, 0 failures`, and test-profile scheduling stays deterministic
  because `weather.scheduling.enabled` gates the earlier H2-backed ShedLock
  noise out of `SmokeTest`.
- Rechecked on 2026-06-28: public maintainer docs now consistently point to
  the real backend verification command `mvn test`, which matches the current
  repository because `backend/mvnw` is not present.
- Rechecked on 2026-06-28: the backend now rejects impossible coordinates,
  returns specific duplicate-create conflict details, and exposes manual ingest
  summaries plus manual dispatch so operators can see both refresh counts and
  `PENDING -> SENT` delivery steps.
- Rechecked on 2026-06-28: the dashboard now supports the full current alert
  lifecycle: create/load user, remember and auto-reconnect the last alert
  email, create rule, disable/re-enable, run ingest, manually dispatch pending
  alerts, and inspect `sentAt` on delivered alerts.
- Rechecked on 2026-06-28: outsider-facing docs now match that same flow
  through `docs/ALERT_FLOW_QUICKSTART.md`, `docs/VERIFICATION_MATRIX.md`, the
  README quick-demo section, and the repeatable `scripts/alert_flow_smoke.py`
  proof path.
- Rechecked on 2026-06-28: the repo now has two public releases (`v0.1.0`,
  `v0.1.1`), and `v0.1.1` is tied to the current alert-flow proof path plus an
  owner-authored release update comment on this issue.
- Rechecked on 2026-06-29: the public issue intake now distinguishes bugs,
  feature requests, and quick-demo usage feedback, so first-run friction has a
  direct path into issue-driven maintainer work.
- Rechecked on 2026-06-28: the conservative eligibility check still returns
  `manual_core_maintainer_evidence_needed`; the remaining missing signal is
  outside adoption/feedback, not missing maintainer surface.

This issue should act as the public anchor for the next small maintenance changes instead of keeping the backlog private.

## How would we verify success?

- the issue is public and labeled `enhancement`
- the issue links to the current public release trail (`v0.1.0`, `v0.1.1`)
- the issue contains concrete follow-up checks instead of vague roadmap language
- the next maintainer-facing patch can reference this issue directly
- the repository keeps the same checklist in `docs/VERIFICATION_MATRIX.md`
- the remaining open item is a real public follow-up, not another stale docs mismatch
