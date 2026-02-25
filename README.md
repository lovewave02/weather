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
