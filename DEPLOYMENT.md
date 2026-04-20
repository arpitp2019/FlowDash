# FlowDash Deployment Guide

This repo is set up for two environments:
- `GitHub Actions` for CI
- `Render` for production

## 1. GitHub CI

The repo already includes [`./.github/workflows/ci.yml`](./.github/workflows/ci.yml). It runs on:
- pull requests
- pushes to `main`

It checks:
- frontend lint
- frontend Pages smoke test
- frontend build
- backend tests
- backend package

Recommended GitHub setting:
- protect `main`
- require the `CI` check before merge

## 2. Render Production

The repo includes [`render.yaml`](./render.yaml) and [`Dockerfile`](./Dockerfile).

Render should create:
- one web service: `flowdash-web`
- one PostgreSQL database: `flowdash-db`

The web service uses:
- `SPRING_PROFILES_ACTIVE=prod`
- `FLOWDASH_DB_URL` from the Render database
- `FLOWDASH_DB_USERNAME` from the Render database
- `FLOWDASH_DB_PASSWORD` from the Render database
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- any `FLOWDASH_AI_*` keys you want enabled

The service is configured to auto-deploy from `main` after checks pass.

## 3. Google OAuth

Add this redirect URI in Google Cloud Console:

```text
https://<your-render-service>.onrender.com/login/oauth2/code/google
```

After that, Google sign-in will work on the Render app URL.

## 4. How Changes Flow

1. Make code changes locally.
2. Push a branch and open a pull request.
3. GitHub Actions runs CI.
4. Merge to `main` after CI passes.
5. Render auto-deploys `main`.

## 5. Local Checks

Useful commands:

- `npm --prefix frontend run lint`
- `npm --prefix frontend run smoke:pages`
- `npm run build:render`
- `backend\\mvnw.cmd -f backend/pom.xml test`

