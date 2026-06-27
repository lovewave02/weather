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
2. confirm the test/runtime gap around scheduled `shedlock` behavior in local H2-based verification
3. collect the first round of public cleanup tasks after `v0.1.0`

Current observed note:

- `mvn test` passes (`6 tests, 0 failures`) but local scheduled-task logs still
  show `SHEDLOCK` table lookup errors during H2-backed execution.

This issue should act as the public anchor for the next small maintenance changes instead of keeping the backlog private.

## How would we verify success?

- the issue is public and labeled `enhancement`
- the issue links to `v0.1.0`
- the issue contains concrete follow-up checks instead of vague roadmap language
- the next maintainer-facing patch can reference this issue directly
- the repository keeps the same checklist in `docs/VERIFICATION_MATRIX.md`
