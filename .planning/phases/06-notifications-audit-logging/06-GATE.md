---
phase: 06
gate_status: passed
build_command: "docker run --rm --network project_default maven:3.9-eclipse-temurin-21 mvn -q compile -DskipTests (notifications-service + audit-log-service)"
test_command: "docker run --rm --network project_default maven:3.9-eclipse-temurin-21 mvn -q test (notifications-service + audit-log-service)"
last_updated: 2026-10-10T03:35:43Z
tests_disabled_during_fixes: none
shadowed_sources: 0
waves:
  - wave: 1
    build: pass
    tests: pass
    fix_attempts: 1
---

## Wave 1

- Build: `docker run --rm --network project_default maven:3.9-eclipse-temurin-21 mvn -q compile -DskipTests (notifications-service + audit-log-service)` → pass
- Tests: `docker run --rm --network project_default maven:3.9-eclipse-temurin-21 mvn -q test (notifications-service + audit-log-service)` → pass
- Fix attempts: 1/3 — Fix attempt 1: Replaced Testcontainers with docker-compose Postgres+WireMock in ApplicationContextBootTest (both services). Fix commit: faecaeb

