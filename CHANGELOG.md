# Changelog

All notable changes to this project should be recorded here.

This file follows a simple keep-a-changelog style.

## Unreleased

### Changed

- Pull release, CI, license, and quick-demo trust signals into the README
  header so outsiders can identify the current public state faster
- Add a dedicated usage-feedback issue template and quickstart link so outside
  demo friction can turn into a narrower public follow-up instead of staying
  private

## v0.1.1 - 2026-06-28

### Changed

- Prepare the repository for the next post-`v0.1.0` maintenance cycle
- Surface the alert flow in the dashboard so a user can create an alert user,
  add a rule for the selected city, and inspect recent alert events after ingest
- Let the dashboard reconnect an existing alert user by email and reload the
  current saved rules instead of forcing a fresh-user-only flow
- Remember the last alert email in the browser and auto-reconnect that user
  after a refresh so the alert flow keeps its working context across visits
- Let the dashboard disable an existing alert rule so a user can stop a saved
  notification without deleting the rest of the alert history
- Let the dashboard re-enable a disabled alert rule, and treat a matching
  create action as a reactivation instead of a dead-end duplicate conflict
- Add a copy-paste alert quickstart so a new user can try the current
  dashboard and API flow without reading the whole repository first
- Add a repeatable alert-flow smoke script so maintainers can verify the
  current API lifecycle and ingest path without clicking through the UI
- Add a manual alert-dispatch endpoint and extend the smoke path so the same
  maintainer proof run verifies `PENDING -> SENT` outbox processing
- Expose the same manual alert-dispatch path in the dashboard so an outsider
  can move `PENDING` alerts to `SENT` and inspect `sentAt` without leaving the
  UI
- Return specific duplicate-create conflict messages for users, locations, and
  alert rules instead of a generic `conflict` response
- Return ingest run summaries from the manual weather refresh API and surface
  the counts in the dashboard so operators can see what a run actually did
- Reject impossible latitude/longitude values at the location API boundary and
  return field-level validation details for bad coordinate requests
- Gate backend scheduling and ShedLock behind `weather.scheduling.enabled` so test-profile verification no longer emits false scheduler errors
- Align release-facing maintainer docs with the current backend verification command (`mvn test`)
- Align the contribution guide with the current backend verification command (`mvn test`)

## v0.1.0 - 2026-06-26

### Added

- Contribution workflow documentation
- Maintainer release checklist
- GitHub issue and PR templates
- CODEOWNERS and pull request expectations for maintainer review

## 2026-06-25

### Existing project baseline

- Spring Boot backend for weather alert management
- React/Vite frontend
- Daily Open-Meteo update workflow and historical weather data snapshots
