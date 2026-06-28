# Weather (Monorepo)

![Release](https://img.shields.io/github/v/release/lovewave02/weather?display_name=tag)
![CI](https://github.com/lovewave02/weather/actions/workflows/ci.yml/badge.svg)
![License](https://img.shields.io/github/license/lovewave02/weather)

- `backend/`: Spring Boot 기반 Weather Alert Platform 백엔드
- `frontend/`: React(Vite) 프론트엔드

Latest public release:

- [`Weather v0.1.1`](https://github.com/lovewave02/weather/releases/tag/v0.1.1)

## Quick Demo

Fastest outsider path:

1. Start the backend with `cd backend && docker compose up -d --build`
2. Start the frontend with `cd frontend && npm install && npm run dev`
3. Open `http://localhost:5173`
4. Click `Load Korea`, then `Ingest Now`
5. Create or load an alert user, add a rule, run `Ingest Now` again, and click `Dispatch Pending`

If you want one repeatable CLI proof instead of the browser path, run:

```bash
python scripts/alert_flow_smoke.py
```

## Run (Backend)

- `cd backend`
- `docker compose up -d --build`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Run (Frontend)

- `cd frontend`
- `npm install`
- `npm run dev`
- Dev server: `http://localhost:5173`

## Dashboard Capabilities

- 저장된 도시 목록과 현재 관측/시간별 예보 확인
- 수동 `Ingest Now` 실행 후 fetched/new/unchanged/missed 요약 확인
- alert 사용자 생성 또는 기존 이메일 재연결, 선택 도시 기준 rule 등록/비활성화/재활성화, 현재 rule 목록/최신 alert 이벤트 조회
- 대시보드에서 `Dispatch Pending`을 눌러 PENDING alert를 SENT로 처리하고 `sentAt`까지 확인

## Daily Auto Improvement

- 스크립트: `scripts/daily_weather_update.py`
  - Open-Meteo API로 서울 날씨를 수집해 `data/latest-seoul-weather.json`, `data/history/YYYY-MM-DD.json` 갱신
  - `DAILY_IMPROVEMENTS.md`에 당일 개선 로그 추가
- 워크플로우: `.github/workflows/daily-improvement.yml`
  - 매일 07:30(KST) 자동 실행 후 변경분이 있으면 커밋/푸시

수동 실행:

```bash
python scripts/daily_weather_update.py
```

## Project Operations

- Contribution guide: [`CONTRIBUTING.md`](CONTRIBUTING.md)
- Alert flow quickstart: [`docs/ALERT_FLOW_QUICKSTART.md`](docs/ALERT_FLOW_QUICKSTART.md)
- Verification matrix: [`docs/VERIFICATION_MATRIX.md`](docs/VERIFICATION_MATRIX.md)
- Release checklist: [`docs/RELEASE_CHECKLIST.md`](docs/RELEASE_CHECKLIST.md)
- Changelog: [`CHANGELOG.md`](CHANGELOG.md)
- Smoke test script: `python scripts/alert_flow_smoke.py`
- Current public backlog anchor: issue [`#1`](https://github.com/lovewave02/weather/issues/1)

## Maintainer roadmap

Short-term maintainer surface goals:

1. Keep backend/frontend verification commands current
2. Track user-facing changes in `CHANGELOG.md`
3. Use issue templates for bugs and feature requests
4. Cut small, explainable releases instead of silent repo drift
5. Keep public follow-up work anchored to issue `#1` and the verification matrix
6. Keep one copy-paste quickstart current so outsiders can try the alert flow quickly
7. Keep one repeatable smoke script current so maintainers and outsiders can verify the alert lifecycle without clicking through the UI
