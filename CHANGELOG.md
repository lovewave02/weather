# Changelog

All notable changes to this project should be recorded here.

This file follows a simple keep-a-changelog style.

## Unreleased

### Changed

- Prepare the repository for the next post-`v0.1.0` maintenance cycle
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
