package com.bookinghub.userspermissions.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Tier-2 JWT security configuration for users-permissions-service.
 * 
 * Defense-in-depth: This service independently re-validates every bearer JWT,
 * never trusting the Gateway's decision alone (TechArch §5.2).
 * 
 * Public routes: Only /auth/login, /auth/password-reset/**, /actuator/health/**
 * are permitted without authentication. ALL other routes require a valid JWT.
 * 
 * Fail-closed: JWKS-outage yields 503 (via JwksOutageAuthenticationEntryPoint),
 * matching the Gateway's fail-closed behavior exactly.
 * 
 * Access-denied handling: 401 (unauthenticated) and 403 (authenticated-but-insufficient-role)
 * both produce the shared ApiError JSON shape (F6.6 decision).
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
                .requestMatchers(
                    "/auth/login",
                    "/auth/password-reset/request",
                    "/auth/password-reset/complete",
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
                .accessDeniedHandler(apiAccessDeniedHandler)
            )
            .csrf(csrf -> csrf.disable()); // Stateless JWT API, no CSRF needed

        return http.build();
    }
}
