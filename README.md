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

## GitHub Pages
GitHub Pages can host only the frontend (static). The Spring Boot backend must be hosted separately (Render/Railway/Fly.io/etc.).

To point the Pages frontend at your backend, set `VITE_API_BASE` at build time (or update the workflow to inject it).

## Build
- `npm --prefix frontend run build`
- `backend\\mvnw.cmd -f backend/pom.xml -DskipTests package`

