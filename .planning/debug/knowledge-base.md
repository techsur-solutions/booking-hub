# Pivota Spec Debug Knowledge Base

Resolved debug sessions. Used by `pivota_spec-debugger` to surface known-pattern hypotheses at the start of new investigations.

---

## api-gateway-boot-failure — BeanCreationException for missing issuer-uri property
- **Date:** 2026-10-12
- **Error patterns:** BeanCreationException, Could not resolve placeholder, spring.security.oauth2.resourceserver.jwt.issuer-uri, api-gateway, boot failure, startup
- **Root cause:** api-gateway application.yml uses jwk-set-uri instead of issuer-uri. Spring Security OAuth2 Resource Server requires issuer-uri property to be defined. Working services (notifications-service, audit-log-service) both use issuer-uri with pattern: http://${KEYCLOAK_HOST}:${KEYCLOAK_PORT}/realms/${KEYCLOAK_REALM}. Additionally, docker-compose.yml and k8s configmap were missing KEYCLOAK_REALM environment variable.
- **Fix:** Changed spring.security.oauth2.resourceserver.jwt.jwk-set-uri to issuer-uri in application.yml. Added KEYCLOAK_REALM=bookinghub environment variable to docker-compose.yml and k8s/api-gateway/configmap.yaml. Rebuilt and restarted api-gateway service.
- **Files changed:** services/api-gateway/src/main/resources/application.yml, docker-compose.yml, k8s/api-gateway/configmap.yaml
---
