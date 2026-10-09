package com.bookinghub.settings.security;

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
 * Tier-2 JWT security configuration for settings-service.
 *
 * Defense-in-depth: This service independently re-validates every bearer JWT,
 * never trusting the Gateway's decision alone (TechArch §5.2), matching the
 * same base shape as every other service this phase.
 *
 * Named decision — read/write split matches the Gateway exactly (TechArch §4.9
 * and the Gateway's already-built Tier-1 rule): GET /settings is authenticated
 * (any role — broadly readable per F10.4, matching the Gateway's own
 * "authenticated for GET" rule exactly). PUT /settings requires
 * role_settings_admin, enforced via plan 04-06's SettingsController
 * @PreAuthorize annotation (class-level baseline here is simply
 * "authenticated everywhere except health checks").
 *
 * Public routes: Only /actuator/health/** is permitted without authentication.
 * ALL other routes require a valid JWT at minimum.
 *
 * Fail-closed: JWKS-outage yields 503 (via JwksOutageAuthenticationEntryPoint),
 * matching the Gateway's fail-closed behavior exactly.
 *
 * Access-denied handling: 401 (unauthenticated) and 403 (authenticated-but-
 * insufficient-role) both produce the shared ApiError JSON shape.
 *
 * Named decision — explicit JwtAuthenticationConverter mapping
 * realm_access.roles to ROLE_-prefixed authorities (Rule 1 bugfix, found
 * while implementing this plan's PUT /settings endpoint). Spring Security's
 * OAuth2 resource-server DEFAULT jwt() customizer only extracts authorities
 * from a flat "scope"/"scp" claim (producing SCOPE_-prefixed authorities) —
 * it has no knowledge of Keycloak's nested realm_access.roles claim at all.
 * Without this converter, every @PreAuthorize("hasRole('role_settings_admin')")
 * check would silently deny every caller, including a genuine admin, because
 * Spring Security would see zero authorities on any JWT regardless of its
 * actual roles. This mirrors CurrentUserProvider.getCurrentRoles()'s own
 * realm_access.roles parsing, but feeds Spring Security's own authorization
 * machinery (hasRole()/@PreAuthorize) rather than this service's manual
 * checks, so the two authorization mechanisms agree on the same source of
 * truth.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwksOutageAuthenticationEntryPoint jwksOutageAuthenticationEntryPoint;
    private final SettingsAccessDeniedHandler settingsAccessDeniedHandler;

    public SecurityConfig(
            JwksOutageAuthenticationEntryPoint jwksOutageAuthenticationEntryPoint,
            SettingsAccessDeniedHandler settingsAccessDeniedHandler) {
        this.jwksOutageAuthenticationEntryPoint = jwksOutageAuthenticationEntryPoint;
        this.settingsAccessDeniedHandler = settingsAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health/**"
                ).permitAll()
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
                .accessDeniedHandler(settingsAccessDeniedHandler)
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
