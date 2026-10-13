---
status: verifying
trigger: "Diagnose and fix the api-gateway boot failure."
created: 2026-10-12T00:00:00Z
updated: 2026-10-12T17:53:45Z
symptoms_prefilled: true
goal: find_and_fix
---

## Current Focus

hypothesis: api-gateway is missing spring.security.oauth2.resourceserver.jwt.issuer-uri property in configuration
test: compare api-gateway config with working services, check application.yml and k8s ConfigMap
expecting: find missing issuer-uri property configuration
next_action: read api-gateway application.yml and k8s configmap

## Symptoms

expected: api-gateway should boot successfully and route requests to backend services
actual: api-gateway fails to start with BeanCreationException for missing issuer-uri placeholder
errors: Could not resolve placeholder 'spring.security.oauth2.resourceserver.jwt.issuer-uri' in value "${spring.security.oauth2.resourceserver.jwt.issuer-uri}"
reproduction: attempt to start api-gateway service
started: phase 6 verification - blocks all 12 UAT tests

## Eliminated

## Evidence

- timestamp: 2026-10-12T00:01:00Z
  checked: api-gateway application.yml
  found: uses jwk-set-uri property (line 68) instead of issuer-uri property
  implication: Spring Security expects issuer-uri but config only provides jwk-set-uri

- timestamp: 2026-10-12T00:01:30Z
  checked: notifications-service application.yml
  found: correctly uses issuer-uri property (line 30) with full Keycloak realm URL pattern
  implication: working services use issuer-uri, not jwk-set-uri

- timestamp: 2026-10-12T00:02:00Z
  checked: audit-log-service application.yml
  found: correctly uses issuer-uri property (line 11) with full Keycloak realm URL pattern
  implication: confirms issuer-uri is the standard property for OAuth2 resource server

- timestamp: 2026-10-12T00:02:30Z
  checked: k8s/api-gateway/configmap.yaml
  found: provides KEYCLOAK_HOST and KEYCLOAK_PORT but missing KEYCLOAK_REALM
  implication: need to add KEYCLOAK_REALM environment variable for issuer-uri interpolation

## Resolution

root_cause: api-gateway application.yml uses jwk-set-uri instead of issuer-uri. Spring Security OAuth2 Resource Server requires issuer-uri property to be defined. Working services (notifications-service, audit-log-service) both use issuer-uri with pattern: http://${KEYCLOAK_HOST}:${KEYCLOAK_PORT}/realms/${KEYCLOAK_REALM}. Additionally, docker-compose.yml and k8s configmap were missing KEYCLOAK_REALM environment variable.
fix: Changed spring.security.oauth2.resourceserver.jwt.jwk-set-uri to issuer-uri in application.yml. Added KEYCLOAK_REALM=bookinghub environment variable to docker-compose.yml and k8s/api-gateway/configmap.yaml. Rebuilt and restarted api-gateway service.
verification: api-gateway successfully started without BeanCreationException. Logs show "Started ApiGatewayApplication in 5.288 seconds" and "Netty started on port 8080". No ERROR or WARN messages. Service is running and healthy.
files_changed: 
  - services/api-gateway/src/main/resources/application.yml
  - docker-compose.yml
  - k8s/api-gateway/configmap.yaml
