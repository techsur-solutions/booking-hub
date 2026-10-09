package com.bookinghub.locationsresources.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(jwksOutageAuthenticationEntryPoint)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwksOutageAuthenticationEntryPoint)
                .accessDeniedHandler(locationResourceAccessDeniedHandler)
            )
            .csrf(csrf -> csrf.disable()); // Stateless JWT API, no CSRF needed

        return http.build();
    }
}
