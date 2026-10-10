package com.bookinghub.notifications.security;

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
 * Tier-2 JWT security configuration for notifications-service.
 *
 * Defense-in-depth: This service independently re-validates every bearer JWT,
 * never trusting the Gateway's decision alone (TechArch §5.2).
 *
 * Public routes: Only /actuator/health/** is permitted without authentication.
 * ALL other routes (both GET endpoints plan 06-02 adds) require role_audit_viewer.
 * This entire service is an internal admin/ops surface — unlike users-permissions-service
 * which has public /auth/** routes, there is no public or self-service route here.
 *
 * Named decision: Both F8's GET /notifications/delivery-status and
 * GET /notifications/dead-letter are "admin (ops/observability)" endpoints.
 * The realm's role catalog (Phase 2, frozen) has no dedicated "ops" role — the
 * closest fit is role_audit_viewer, which F11's GET /audit-log also uses.
 * Both are read-only operational/observability surfaces; gating them with the
 * same role is consistent.
 *
 * Named decision (authority mapping): We grant realm_access.roles values as-is
 * (without the ROLE_ prefix) and match them with hasAuthority("role_audit_viewer")
 * rather than hasRole() + ROLE_ prefix, to avoid a double-prefix collision bug.
 * Spring Security's hasRole("x") internally matches "ROLE_x"; if we stored the
 * role as "ROLE_role_audit_viewer" and queried hasRole("role_audit_viewer"), the
 * match would work. But if we stored it as "role_audit_viewer" and used hasRole(),
 * Spring would look for "ROLE_role_audit_viewer" and fail. Using hasAuthority()
 * with the verbatim role name sidesteps this entirely.
 *
 * Fail-closed: JWKS-outage yields 503 (via JwksOutageAuthenticationEntryPoint),
 * matching the Gateway's fail-closed behavior exactly, proven by Tier2FailClosedTest.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwksOutageAuthenticationEntryPoint jwksOutageAuthenticationEntryPoint;
    private final ApiAccessDeniedHandler apiAccessDeniedHandler;

    public SecurityConfig(
            JwksOutageAuthenticationEntryPoint jwksOutageAuthenticationEntryPoint,
            ApiAccessDeniedHandler apiAccessDeniedHandler) {
        this.jwksOutageAuthenticationEntryPoint = jwksOutageAuthenticationEntryPoint;
        this.apiAccessDeniedHandler = apiAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .anyRequest().hasAuthority("role_audit_viewer")
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(realmRoleJwtAuthenticationConverter())
                )
                .authenticationEntryPoint(jwksOutageAuthenticationEntryPoint)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwksOutageAuthenticationEntryPoint)
                .accessDeniedHandler(apiAccessDeniedHandler)
            )
            .csrf(csrf -> csrf.disable()); // Stateless JWT API, no CSRF needed

        return http.build();
    }

    /**
     * Converts a Keycloak-issued JWT's realm_access.roles claim into
     * GrantedAuthority instances WITHOUT the ROLE_ prefix, so Spring Security's
     * hasAuthority("role_audit_viewer") expressions (used in this service instead
     * of hasRole()) match the verbatim role names from the Keycloak realm.
     *
     * This avoids the double-prefix collision bug: Spring's hasRole("x") matches
     * "ROLE_x"; hasAuthority("role_audit_viewer") matches "role_audit_viewer" exactly.
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
        // Grant verbatim role names (no ROLE_ prefix) so hasAuthority("role_audit_viewer") matches directly
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }
}
