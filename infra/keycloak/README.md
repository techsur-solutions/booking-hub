# Keycloak Realm Configuration

## Overview

This directory contains the declarative Keycloak realm configuration for BookingHub, imported automatically at container startup.

## Files

- **`realm-export.json`**: Complete Keycloak realm export in the standard admin-console export format, containing:
  - Realm: `bookinghub`
  - **1 public client** (frontend PKCE): `bookinghub-frontend`
  - **9 confidential bearer-only clients** (backend services): `booking-service`, `locations-resources-service`, `custom-field-service`, `users-permissions-service`, `notifications-service`, `feeds-service`, `settings-service`, `audit-log-service`, `api-gateway`
  - **1 confidential service-account client**: `userperm-admin-client` (for Keycloak Admin API access by users-permissions-service)
  - **2 confidential direct-grant clients** (server-side login): `userperm-direct-grant-client`, `userperm-direct-grant-remember-client` (see Session Timeouts section below)
  - **11 realm roles** per TechArch §5.2: `role_calendar_viewer`, `role_booking_creator`, `role_booking_viewer`, `role_booking_approver`, `role_permissions_admin`, `role_feed_api`, `role_location_admin`, `role_customfield_admin`, `role_user_admin`, `role_settings_admin`, `role_audit_viewer`
  - **1 seed user**: `admin@bookinghub.local` holding all 11 roles

## Session Timeouts and Password Policy

### Session Timeouts

**Realm-level defaults** (F0 Open Question #17 — fresh decision for the new system):
- `ssoSessionIdleTimeout`: **1800 seconds (30 minutes)** — idle sessions expire after 30 minutes of inactivity
- `ssoSessionMaxLifespan`: **36000 seconds (10 hours)** — active sessions expire after 10 hours regardless of activity

**Rationale:** Legacy's actual session timeout was never discoverable from source code (ColdFusion/Lucee engine-level default, absent from the codebase's config). The values above are reasonable modern defaults, explicitly NOT a parity claim with legacy behavior.

### Remember-Me Session Extension

The new system introduces **two direct-grant clients** for server-side login (used by `AuthController.login`), which differ ONLY in their session lifespan overrides:

- **`userperm-direct-grant-client`** (standard):
  - `client.session.idle.timeout`: 1800 (30 min)
  - `client.session.max.lifespan`: 36000 (10 hours)

- **`userperm-direct-grant-remember-client`** (remember-me):
  - `client.session.idle.timeout`: 1800 (30 min)
  - `client.session.max.lifespan`: **2592000 (30 days)**

**Rationale (F6 remember_me improvement):** Legacy's `RBS_UN` cookie (360-day expiry, F0-confirmed) was PURELY a login-form email-prefill convenience — it never extended the underlying session. The new system's `remember_me` flag **genuinely extends the issued token's session lifetime** (30 days vs 10 hours) — an explicit, deliberate IMPROVEMENT over legacy, not a parity claim. The 30-day figure is a fresh choice, NOT derived from legacy's 360-day cookie (deliberately avoiding a false connection).

### Password Policy

**`passwordPolicy`: `length(6) and digits(1) and lowerCase(1)`**

This policy matches the **confirmed baseline from legacy's `models/User.cfc` regex** (F0-confirmed):
- Minimum 6 characters
- At least 1 digit
- At least 1 lowercase letter
- No uppercase or symbol requirement (matching legacy exactly)

Per the F0 audit's explicit requirement: the new system's password policy must be **no weaker** than legacy's enforced floor.

## Import Mechanism

The realm is imported at Keycloak container boot via the `--import-realm` startup flag (configured in plan 02-12's `docker-compose.yml`):

```yaml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:25.0.0
    command:
      - start-dev
      - --import-realm
    volumes:
      - ./infra/keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json:ro
```

### Idempotency

The `--import-realm` flag **only imports a realm that does not already exist** in the Keycloak database. If the `bookinghub` realm already exists (e.g., on container restart with a persisted PostgreSQL-backed realm DB volume), the import is a safe no-op. This makes the import idempotent across container restarts.

## Security Warnings — LOCAL DEV ONLY

⚠️ **This configuration is ONLY suitable for local development.** The following placeholders **MUST** be regenerated before any non-local deployment:

### 1. Client Secrets

All confidential client secrets are set to **placeholder values**:

- `booking-service`: `CHANGE_ME_booking_service_secret`
- `locations-resources-service`: `CHANGE_ME_locations_resources_service_secret`
- `custom-field-service`: `CHANGE_ME_custom_field_service_secret`
- `users-permissions-service`: `CHANGE_ME_users_permissions_service_secret`
- `notifications-service`: `CHANGE_ME_notifications_service_secret`
- `feeds-service`: `CHANGE_ME_feeds_service_secret`
- `settings-service`: `CHANGE_ME_settings_service_secret`
- `audit-log-service`: `CHANGE_ME_audit_log_service_secret`
- `api-gateway`: `CHANGE_ME_api_gateway_secret`
- `userperm-admin-client`: `CHANGE_ME_userperm_admin_client_secret`
- `userperm-direct-grant-client`: `CHANGE_ME_userperm_direct_grant_secret`
- `userperm-direct-grant-remember-client`: `CHANGE_ME_userperm_direct_grant_remember_secret`

**Important:** The 9 bearer-only service clients do NOT actively use their client secrets in the current architecture. Bearer-only clients validate incoming JWTs via the realm's JWKS endpoint and do not initiate OAuth flows that require a client secret. The placeholder values in the realm export are safe for bearer-only clients and can remain as-is for local development.

**However**, three confidential clients **DO actively use their secrets**:
- `userperm-admin-client` (service-account): client credentials flow for Keycloak Admin API access
- `userperm-direct-grant-client` (direct-grant): resource owner password credentials flow for standard login
- `userperm-direct-grant-remember-client` (direct-grant): resource owner password credentials flow for remember-me login

**Before deploying to staging/production:**

1. Generate cryptographically secure secrets for all three active clients (e.g., `openssl rand -base64 32`)
2. Update the clients' secrets via Keycloak Admin Console or Admin API
3. Store the secrets in Kubernetes `Secret` objects, NOT in this committed JSON file
4. Bearer-only client secrets can remain as placeholders unless service-to-service calls using client credentials flow are added in future phases

### 2. Seed User Password

The seed user `admin@bookinghub.local` has password: **`ChangeMe123!`**

This is a **non-expiring, non-temporary password** suitable only for local development and end-to-end testing.

**Before deploying to any shared/non-local environment:**

1. Delete this seed user OR reset its password via Keycloak Admin Console
2. Never commit real production credentials to version control

## Client Roles — Admin API Access

The `userperm-admin-client` service account is granted the following **realm-management** client roles:

- `manage-users` — create/update/delete user accounts, assign roles
- `view-users` — read user account metadata

These roles scope the service account's Admin API blast radius to account provisioning and credential-reset operations only (per TechArch §5.1), NOT full realm admin access.

## Frontend Client Configuration

The `bookinghub-frontend` public client is configured for **Authorization Code + PKCE** flow:

- `publicClient: true` — no client secret (cannot be kept confidential in browser-shipped code)
- `directAccessGrantsEnabled: false` — resource-owner-password-credentials (ROPC) flow disabled, preventing credential-phishing-friendly direct grants
- `pkce.code.challenge.method: S256` — **mandatory PKCE** using SHA-256 code challenge
- `redirectUris`: `http://localhost:3000/*` (local dev) and `https://*` (wildcard for staging/prod environments, to be tightened to exact domains before production deployment)
- `webOrigins: ["*"]` — wildcard CORS for local dev (must be tightened to exact frontend origins before production deployment)

## Integration with docker-compose

This realm export is mounted into the Keycloak container and imported at boot in plan 02-12's `docker-compose.yml`. The full-stack integration includes:

- Keycloak service with PostgreSQL-backed realm DB (volume-persisted)
- Gateway service validating JWTs against this realm's JWKS endpoint
- Each backend service validating bearer tokens as confidential clients from this realm

See `.planning/phases/02-platform-foundation-infrastructure/02-12-PLAN.md` for the complete docker-compose integration.

## Verification

After importing the realm, verify it was loaded correctly:

```bash
# Realm exists and is enabled
curl -s http://localhost:8080/realms/bookinghub/.well-known/openid-configuration | jq '.issuer'
# Expected: "http://localhost:8080/realms/bookinghub"

# All 11 realm roles exist
# (requires admin token — shown as example only, actual verification in plan 02-12)
```

---

**Last updated:** 2026-10-08  
**Plans:** 02-09 (initial realm), 03-03 (session timeouts, password policy, direct-grant clients)  
**Phase:** 03-identity-access-control
