package com.bookinghub.booking.security;

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
 * Tier-2 JWT security configuration for booking-service.
 *
 * Defense-in-depth: this service independently re-validates every bearer JWT,
 * never trusting the Gateway's decision alone (Tier 2).
 *
 * Public routes: Only /actuator/health/** is permitted without authentication.
 * ALL other routes require a valid JWT.
 *
 * Named decision — Tier-2 role split per action (per-HTTP-method), narrower than
 * the Gateway's coarse Tier-1 gate (never weaker):
 *
 * The Gateway's already-executed Tier-1 route table applies ONE coarse rule to ALL
 * methods on /bookings/**: hasAnyAuthority("SCOPE_role_booking_viewer",
 * "SCOPE_role_booking_creator", "SCOPE_role_booking_approver") — no per-HTTP-method
 * distinction (unlike /locations/** and /resources/** which the Gateway already splits).
 *
 * This plan's Tier-2 layer narrows per-action, per TechArch §4.2's permission column:
 * - POST /bookings, POST /bookings/{id}/clone, POST /bookings/check-conflicts
 *     -> @PreAuthorize("hasRole('role_booking_creator')")
 * - GET /bookings, GET /bookings/{id}
 *     -> @PreAuthorize("hasAnyRole('role_booking_viewer','role_booking_creator','role_booking_approver')")
 * - PUT /bookings/{id}, DELETE /bookings/{id}
 *     -> @PreAuthorize("hasRole('role_booking_creator')") + ownership check in BookingWriteService
 *        OR hasRole('role_booking_approver') for any booking — per TechArch §4.2 "(owner) or admin"
 * - POST /bookings/{id}/approve, POST /bookings/{id}/deny
 *     -> manual hasRole("role_booking_approver") check in BookingApprovalController (plan 05-04)
 *        throwing ApprovalForbiddenException (same pattern as Phase 3 PermissionController)
 *
 * Every one of these Tier-2 role requirements is a SUBSET of the Gateway's 3-role
 * Tier-1 gate, never a role outside it. Tier1Tier2ConsistencyTest proves this.
 *
 * Fail-closed: JWKS-outage yields 503 (via JwksOutageAuthenticationEntryPoint),
 * matching the Gateway's and every other service's fail-closed behavior exactly.
 *
 * Named decision — JwtAuthenticationConverter mapping realm_access.roles to
 * ROLE_-prefixed authorities (same bugfix independently discovered and applied in
 * locations-resources-service plan 04-01, settings-service plan 04-05, and
 * custom-field-service plan 04-03): Spring Security's OAuth2 resource-server DEFAULT
 * jwt() customizer only extracts authorities from a flat "scope"/"scp" claim
 * (SCOPE_-prefixed). Without this converter, every @PreAuthorize("hasRole(...)") check
 * silently denies all real Keycloak callers regardless of actual roles.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwksOutageAuthenticationEntryPoint jwksOutageAuthenticationEntryPoint;
    private final BookingAccessDeniedHandler bookingAccessDeniedHandler;

    public SecurityConfig(
            JwksOutageAuthenticationEntryPoint jwksOutageAuthenticationEntryPoint,
            BookingAccessDeniedHandler bookingAccessDeniedHandler) {
        this.jwksOutageAuthenticationEntryPoint = jwksOutageAuthenticationEntryPoint;
        this.bookingAccessDeniedHandler = bookingAccessDeniedHandler;
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
                .accessDeniedHandler(bookingAccessDeniedHandler)
            )
            .csrf(csrf -> csrf.disable()); // Stateless JWT API, no CSRF needed

        return http.build();
    }

    /**
     * Converts a Keycloak-issued JWT's realm_access.roles claim into
     * ROLE_-prefixed GrantedAuthority instances, so Spring Security's
     * hasRole()/@PreAuthorize("hasRole(...)") expressions (which implicitly
     * add the ROLE_ prefix when matching) actually see this service's
     * realm roles. Without this converter, every @PreAuthorize check silently
     * denies all callers — same root cause independently fixed in all other
     * services (named decision in Phase 4 plan 04-01/04-03/04-05/04-06).
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
