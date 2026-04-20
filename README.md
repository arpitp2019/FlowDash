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

## Build
- `npm --prefix frontend run build`
- `backend\\mvnw.cmd -f backend/pom.xml -DskipTests package`

