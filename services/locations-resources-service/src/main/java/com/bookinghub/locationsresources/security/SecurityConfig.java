package com.bookinghub.locationsresources.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tier-2 JWT security configuration for locations-resources-service.
 *
 * Defense-in-depth: This service independently re-validates every bearer JWT,
 * never trusting the Gateway's decision alone (Tier 2).
 *
 * Public routes: Only /actuator/health/** is permitted without authentication.
 * ALL other routes require a valid JWT.
 *
 * Named decision — Tier-2 stricter than Tier-1 for reads (deliberate, per the
 * two-tier model): the Gateway's Tier-1 route table requires only
 * "authenticated" for GET /locations/**  / GET /resources/**. This service's
 * controllers (plan 04-02) layer @PreAuthorize("hasRole('role_calendar_viewer')")
 * on top for GETs — STRICTER than Tier-1's "any authenticated" — which is
 * architecturally correct per the two-tier model (Tier-2 may be stricter,
 * never weaker) and proven never-weaker by Tier1Tier2ConsistencyTest.
 *
 * Fail-closed: JWKS-outage yields 503 (via JwksOutageAuthenticationEntryPoint),
 * matching the Gateway's fail-closed behavior exactly.
 *
 * Access-denied handling: 401 (unauthenticated) and 403 (authenticated-but-
 * insufficient-role) both produce the shared ApiError JSON shape.
 *
 * [Rule 1 - Bug, found during plan 04-02 Task 3 integration testing] Explicit
 * JwtAuthenticationConverter mapping realm_access.roles to ROLE_-prefixed
 * authorities. Spring Security's OAuth2 resource-server DEFAULT jwt()
 * customizer only extracts authorities from a flat "scope"/"scp" claim
 * (producing SCOPE_-prefixed authorities) — it has no knowledge of Keycloak's
 * nested realm_access.roles claim at all. Without this converter, every
 * @PreAuthorize("hasRole('role_location_admin')")/hasRole('role_calendar_viewer')
 * check on LocationController/ResourceController silently denied EVERY
 * caller, including a genuine admin, because Spring Security saw zero
 * authorities on any real Keycloak JWT regardless of its actual roles —
 * LocationControllerIntegrationTest's admin-create scenario returned 403
 * instead of 201 until this was fixed. Same bug independently discovered and
 * fixed identically in settings-service's SecurityConfig (plan 04-05/04-06);
 * this mirrors that fix verbatim. Also mirrors
 * CurrentUserProvider.getCurrentRoles()'s own realm_access.roles parsing, so
 * both authorization mechanisms (Spring's hasRole()/@PreAuthorize and this
 * service's manual CurrentUserProvider checks) agree on the same source of
 * truth.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwksOutageAuthenticationEntryPoint jwksOutageAuthenticationEntryPoint;
    private final LocationResourceAccessDeniedHandler locationResourceAccessDeniedHandler;

    public SecurityConfig(
            JwksOutageAuthenticationEntryPoint jwksOutageAuthenticationEntryPoint,
            LocationResourceAccessDeniedHandler locationResourceAccessDeniedHandler) {
        this.jwksOutageAuthenticationEntryPoint = jwksOutageAuthenticationEntryPoint;
        this.locationResourceAccessDeniedHandler = locationResourceAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(realmRoleJwtAuthenticationConverter())
                )
                .authenticationEntryPoint(jwksOutageAuthenticationEntryPoint)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwksOutageAuthenticationEntryPoint)
                .accessDeniedHandler(locationResourceAccessDeniedHandler)
            )
            .csrf(csrf -> csrf.disable()); // Stateless JWT API, no CSRF needed

        return http.build();
    }

    /**
     * Converts a Keycloak-issued JWT's realm_access.roles claim into
     * ROLE_-prefixed GrantedAuthority instances, so Spring Security's
     * hasRole()/@PreAuthorize("hasRole(...)") expressions (which implicitly
     * add the ROLE_ prefix when matching) actually see this service's
     * realm roles. See class javadoc's named decision above for why this is
     * required (not optional) for @PreAuthorize to function at all against a
     * real Keycloak token.
     */
    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> realmRoleJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractRealmRoleAuthorities);
        return converter;
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoleAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return List.of();
        }

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List)) {
            return List.of();
        }

        List<String> roles = (List<String>) rolesObj;
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
