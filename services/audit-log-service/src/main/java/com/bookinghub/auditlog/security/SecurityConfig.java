package com.bookinghub.auditlog.security;

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
 * Tier-2 JWT security configuration for audit-log-service.
 *
 * Defense-in-depth: this service independently re-validates every bearer JWT,
 * never trusting the Gateway's decision alone (TechArch §5.2).
 *
 * Public routes: Only /actuator/health/** is permitted without authentication.
 * ALL other routes require role_audit_viewer — this entire service is an internal
 * admin/ops surface. The only planned endpoint (GET /audit-log, plan 06-04) is
 * explicitly an admin log-viewing surface; there is no public or self-service route.
 *
 * Named decision (role_audit_viewer): identical reasoning to plan 06-01 —
 * role_audit_viewer is the only provisioned role fitting F11's "admin (log viewing,
 * legacy Logfiles equivalent)" requirement, and is literally named for this exact
 * purpose.
 *
 * Named decision (JwtAuthenticationConverter mapping realm_access.roles to
 * ROLE_-prefixed authorities): same bugfix as every prior-phase service
 * (Phase 4 plans 04-01/04-03/04-05/04-06, Phase 5 plan 05-01). Spring Security's
 * OAuth2 resource-server DEFAULT jwt() customizer only extracts authorities from a
 * flat "scope"/"scp" claim (SCOPE_-prefixed). Without this converter, every
 * .hasAuthority("role_audit_viewer") check silently denies all real Keycloak callers.
 *
 * Fail-closed: JWKS-outage yields 503 (via JwksOutageAuthenticationEntryPoint),
 * matching the Gateway's and every other service's fail-closed behavior exactly.
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
     * GrantedAuthority instances (WITHOUT ROLE_ prefix since we use hasAuthority()
     * not hasRole() — hasAuthority() does no prefix manipulation, so the authority
     * value must match the string we pass to .hasAuthority("role_audit_viewer") exactly).
     *
     * Named decision (hasAuthority vs hasRole): using hasAuthority("role_audit_viewer")
     * with authorities granted as "role_audit_viewer" (no ROLE_ prefix) rather than
     * hasRole("audit_viewer") with ROLE_-prefixed authorities. Either works; this
     * pattern is explicit and avoids the double-prefix confusion the 06-01 plan notes.
     * The authority string matches the Keycloak realm role name verbatim.
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
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }
}
