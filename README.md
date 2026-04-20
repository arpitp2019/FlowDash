# FlowDash (Spring Boot + React)

This repo now hosts FlowDash as a full-stack app:
- `backend/` Spring Boot 3 (Java 21) with session auth + Google OAuth2
- `frontend/` React + Vite UI bundled into `backend/src/main/resources/static`

Legacy standalone HTML pages and the old `server.js` are archived in `legacy/`.

## Local dev

Frontend dev server (proxies to backend):
- `npm --prefix frontend run dev`

Backend:
- `backend\\mvnw.cmd -f backend/pom.xml spring-boot:run`

## Production on Render

Deploy the whole app as one Render web service with PostgreSQL:

1. Connect the repo to a Render Blueprint from [`render.yaml`](./render.yaml).
2. Let Render create or sync the `flowdash-web` service and `flowdash-db` Postgres database.
3. Fill the prompted secret env vars: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and any `FLOWDASH_AI_*` keys you use.
4. Add this Google OAuth redirect URI:
   - `https://<your-render-service>.onrender.com/login/oauth2/code/google`
5. Use the Render service URL as the production app. GitHub Pages can stay as a frontend-only preview, but it is not the live deployment target.

The app builds the React frontend into the Spring Boot static resources during the Docker build, so the deployed service stays same-origin and session auth works from any device.

## CI/CD

- GitHub Actions runs `frontend` lint/smoke checks and `backend` tests on every pull request and push to `main`.
- Render should be set to auto-deploy from `main`, and the Blueprint uses `autoDeployTrigger: checksPass` so deploys wait for passing checks.
- GitHub Pages can stay enabled as a frontend-only preview, but the Render URL is the live app.

## GitHub Pages preview

GitHub Pages can still host the frontend-only preview. If you use it, set `VITE_API_BASE` at build time so the frontend points at a separate backend.

## Build
- `npm --prefix frontend run build`
- `backend\\mvnw.cmd -f backend/pom.xml -DskipTests package`
- `npm run build:render`

