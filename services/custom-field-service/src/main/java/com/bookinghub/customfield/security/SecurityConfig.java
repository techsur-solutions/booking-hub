package com.bookinghub.customfield.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(jwksOutageAuthenticationEntryPoint)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwksOutageAuthenticationEntryPoint)
                .accessDeniedHandler(customFieldAccessDeniedHandler)
            )
            .csrf(csrf -> csrf.disable()); // Stateless JWT API, no CSRF needed

        return http.build();
    }
}
