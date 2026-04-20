FROM node:22-bookworm AS frontend-build
WORKDIR /app

COPY frontend/package*.json frontend/
RUN cd frontend && npm ci

COPY frontend frontend
COPY backend backend

WORKDIR /app/frontend
RUN npm run build
RUN rm -f /app/backend/src/main/resources/static/404.html

FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

COPY --from=frontend-build /app/backend backend
RUN mvn -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV PORT=10000
EXPOSE 10000

COPY --from=backend-build /app/backend/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar

CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT:-10000}"]
