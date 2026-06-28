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
- [x] aligned release-facing maintainer docs with the real backend verification command (`mvn test`) so public maintainer guidance matches the current repository state

Still open:

- [ ] collect the next public cleanup task that can create stronger user or adoption signals beyond maintainer-surface hygiene

Current observed note:

- Rechecked on 2026-06-28: `mvn test` passes (`6 tests, 0 failures`) and the
  test profile now disables scheduling infrastructure cleanly, so the prior
  H2-backed `SHEDLOCK` table noise is no longer emitted during `SmokeTest`.
- Rechecked on 2026-06-28: release-facing maintainer docs now point to `mvn test`
  rather than `./mvnw test`, which matches the current repository because
  `backend/mvnw` is not present.

This issue should act as the public anchor for the next small maintenance changes instead of keeping the backlog private.

## How would we verify success?

- the issue is public and labeled `enhancement`
- the issue links to `v0.1.0`
- the issue contains concrete follow-up checks instead of vague roadmap language
- the next maintainer-facing patch can reference this issue directly
- the repository keeps the same checklist in `docs/VERIFICATION_MATRIX.md`
- the remaining open item is a real public follow-up, not another stale docs mismatch
