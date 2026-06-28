# Weather (Monorepo)

- `backend/`: Spring Boot 기반 Weather Alert Platform 백엔드
- `frontend/`: React(Vite) 프론트엔드

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

## Maintainer roadmap

Short-term maintainer surface goals:

1. Keep backend/frontend verification commands current
2. Track user-facing changes in `CHANGELOG.md`
3. Use issue templates for bugs and feature requests
4. Cut small, explainable releases instead of silent repo drift
5. Keep public follow-up work anchored to issue `#1` and the verification matrix
6. Keep one copy-paste quickstart current so outsiders can try the alert flow quickly
