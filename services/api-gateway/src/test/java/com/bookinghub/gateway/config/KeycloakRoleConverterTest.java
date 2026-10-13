package com.bookinghub.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A token shaped the way Keycloak actually issues it: roles in realm_access.roles, only OIDC
 * scopes in "scope". The route rules check SCOPE_role_* authorities, so the realm roles must
 * become those — otherwise every role-gated route answers 403 to every real user.
 */
class KeycloakRoleConverterTest {

    private static Set<String> authoritiesOf(Jwt jwt) {
        return SecurityConfig.keycloakRoleConverter().convert(jwt).block()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    private static Jwt.Builder keycloakToken() {
        return Jwt.withTokenValue("t").header("alg", "RS256")
                .issuer("https://8180-pivota-sandbox-x.preview.example/realms/bookinghub")
                .subject("admin").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300))
                .claim("scope", "profile email");
    }

    @Test
    void realmRolesBecomeTheAuthoritiesTheRouteRulesCheck() {
        Jwt jwt = keycloakToken()
                .claim("realm_access", Map.of("roles", List.of("role_booking_viewer", "role_audit_viewer")))
                .build();

        assertThat(authoritiesOf(jwt))
                .contains("SCOPE_role_booking_viewer", "SCOPE_role_audit_viewer", "SCOPE_profile", "SCOPE_email");
    }

    @Test
    void aTokenWithoutRealmRolesGetsOnlyItsScopes() {
        assertThat(authoritiesOf(keycloakToken().build())).containsExactlyInAnyOrder("SCOPE_profile", "SCOPE_email");
    }
}
