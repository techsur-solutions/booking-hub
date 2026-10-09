package com.bookinghub.customfield.security;

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
 * Tier-2 JWT security configuration for custom-field-service.
 *
 * Defense-in-depth: This service independently re-validates every bearer JWT,
 * never trusting the Gateway's decision alone (TechArch §5.2).
 *
 * Public routes: Only /actuator/health/** is permitted without authentication.
 * ALL other routes require a valid JWT.
 *
 * Named decision — exact match, not stricter, for this route group. Unlike
 * locations-resources-service, custom-field-service's Tier-2 requirement
 * MATCHES Tier-1 exactly for ALL of /custom-fields/**{@code /field-templates/**}
 * per TechArch's API table (§4.4, every row "admin (custom field mgmt)") and
 * the Gateway's already-built Tier-1 rule (role_customfield_admin for the
 * entire prefix, no read/write split). Controller methods carry
 * @PreAuthorize("hasRole('role_customfield_admin')") uniformly across ALL
 * methods including GETs — no method in this service's controllers has a
 * weaker or different role requirement.
 *
 * Fail-closed: JWKS-outage yields 503 (via JwksOutageAuthenticationEntryPoint),
 * matching the Gateway's fail-closed behavior exactly.
 *
 * Access-denied handling: 401 (unauthenticated) and 403 (authenticated-but-insufficient-role)
 * both produce the shared ApiError JSON shape.
 *
 * Named decision — explicit JwtAuthenticationConverter mapping realm_access.roles
 * to ROLE_-prefixed authorities (Rule 1 bugfix, same root cause independently
 * found and fixed in plan 04-06's settings-service). Spring Security's OAuth2
 * resource-server DEFAULT jwt() customizer only extracts authorities from a
 * flat "scope"/"scp" claim (producing SCOPE_-prefixed authorities) — it has no
 * knowledge of Keycloak's nested realm_access.roles claim at all. Without this
 * converter, every @PreAuthorize("hasRole('role_customfield_admin')") check in
 * this service's controllers would silently deny every caller, including a
 * genuine admin, because Spring Security would see zero authorities on any
 * real Keycloak-issued JWT regardless of its actual roles. This mirrors
 * CurrentUserProvider.getCurrentRoles()'s own realm_access.roles parsing, but
 * feeds Spring Security's own authorization machinery (hasRole()/@PreAuthorize)
 * rather than this service's manual checks, so the two mechanisms agree on the
 * same source of truth.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwksOutageAuthenticationEntryPoint jwksOutageAuthenticationEntryPoint;
    private final CustomFieldAccessDeniedHandler customFieldAccessDeniedHandler;

    public SecurityConfig(
            JwksOutageAuthenticationEntryPoint jwksOutageAuthenticationEntryPoint,
            CustomFieldAccessDeniedHandler customFieldAccessDeniedHandler) {
        this.jwksOutageAuthenticationEntryPoint = jwksOutageAuthenticationEntryPoint;
        this.customFieldAccessDeniedHandler = customFieldAccessDeniedHandler;
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
                .accessDeniedHandler(customFieldAccessDeniedHandler)
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
