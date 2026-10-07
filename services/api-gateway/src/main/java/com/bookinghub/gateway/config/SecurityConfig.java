package com.bookinghub.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                        // Public routes - no authentication required
                        .pathMatchers("/feeds/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/auth/password-reset/**").permitAll()
                        
                        // Bookings - requires role_booking_viewer OR role_booking_creator OR role_booking_approver
                        .pathMatchers("/bookings/**").hasAnyAuthority("SCOPE_role_booking_viewer", "SCOPE_role_booking_creator", "SCOPE_role_booking_approver")
                        
                        // Locations/Resources - authenticated for GET, role_location_admin for POST/PUT/DELETE
                        .pathMatchers(HttpMethod.GET, "/locations/**", "/resources/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/locations/**", "/resources/**").hasAuthority("SCOPE_role_location_admin")
                        .pathMatchers(HttpMethod.PUT, "/locations/**", "/resources/**").hasAuthority("SCOPE_role_location_admin")
                        .pathMatchers(HttpMethod.DELETE, "/locations/**", "/resources/**").hasAuthority("SCOPE_role_location_admin")
                        
                        // Custom fields - role_customfield_admin required
                        .pathMatchers("/custom-fields/**", "/field-templates/**").hasAuthority("SCOPE_role_customfield_admin")
                        
                        // Users - role_user_admin (self-access to /users/me is Tier-2 per-service concern)
                        .pathMatchers("/users/**").hasAuthority("SCOPE_role_user_admin")
                        
                        // Permissions/Roles - role_permissions_admin required
                        .pathMatchers("/permissions/**", "/roles/**").hasAuthority("SCOPE_role_permissions_admin")
                        
                        // Notifications - role_audit_viewer (ops/observability role)
                        .pathMatchers("/notifications/**").hasAuthority("SCOPE_role_audit_viewer")
                        
                        // Settings - authenticated for GET, role_settings_admin for PUT
                        .pathMatchers(HttpMethod.GET, "/settings/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/settings/**").hasAuthority("SCOPE_role_settings_admin")
                        
                        // Audit log - role_audit_viewer required
                        .pathMatchers("/audit-log/**").hasAuthority("SCOPE_role_audit_viewer")
                        
                        // All other routes require authentication
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(failClosedJwtDecoder()))
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> handleAuthenticationError(exchange, ex))
                        .accessDeniedHandler((exchange, ex) -> handleAccessDenied(exchange, ex))
                )
                .build();
    }

    /**
     * Fail-closed JWT decoder wrapper: if Keycloak's JWKS endpoint is unreachable,
     * return 503 SERVICE_UNAVAILABLE rather than treating unverifiable tokens as valid.
     */
    @Bean
    public ReactiveJwtDecoder failClosedJwtDecoder() {
        ReactiveJwtDecoder delegate = ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
        
        return token -> delegate.decode(token)
                .onErrorResume(ex -> {
                    // Any failure reaching JWKS endpoint → fail closed with 503
                    // This includes network errors, timeouts, DNS failures, etc.
                    return Mono.error(new JwksUnreachableException(
                            "Identity provider JWKS endpoint unreachable - failing closed", ex));
                });
    }

    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, Exception ex) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> error = new HashMap<>();
        error.put("error_code", "AUTH_UNAUTHENTICATED");
        error.put("message", ex.getMessage() != null ? ex.getMessage() : "Authentication required");
        error.put("timestamp", Instant.now().toString());
        error.put("path", exchange.getRequest().getPath().value());
        
        return writeJsonResponse(exchange, error);
    }

    private Mono<Void> handleAccessDenied(ServerWebExchange exchange, Exception ex) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> error = new HashMap<>();
        error.put("error_code", "GATEWAY_FORBIDDEN");
        error.put("message", "Insufficient permissions for this resource");
        error.put("timestamp", Instant.now().toString());
        error.put("path", exchange.getRequest().getPath().value());
        
        return writeJsonResponse(exchange, error);
    }

    private Mono<Void> writeJsonResponse(ServerWebExchange exchange, Map<String, Object> body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(body);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    /**
     * Custom exception for JWKS unreachability - caught by global error handler
     * to return 503 instead of 401.
     */
    public static class JwksUnreachableException extends RuntimeException {
        public JwksUnreachableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
