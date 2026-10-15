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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                        // Public routes - no authentication required
                        // Health/info endpoints must be public for Docker healthcheck and monitoring
                        .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        // NOTE: /feeds/** bypasses gateway-level auth - feeds-service is responsible
                        // for any authorization logic (e.g. if admin endpoints are added under /feeds/admin/**)
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
                        .jwt(jwt -> jwt
                                .jwtDecoder(failClosedJwtDecoder())
                                .jwtAuthenticationConverter(keycloakRoleConverter()))
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> handleAuthenticationError(exchange, ex))
                        .accessDeniedHandler((exchange, ex) -> handleAccessDenied(exchange, ex))
                )
                .build();
    }

    /**
     * Keycloak puts realm roles in the {@code realm_access.roles} claim, not in {@code scope}.
     * The rules above check {@code SCOPE_role_*} authorities, which Spring derives only from
     * {@code scope} — so a real Keycloak token carried none of them and every role-gated route
     * answered 403 for every user (the tests passed because they inject the authorities directly).
     * Each realm role is granted as {@code SCOPE_<role>} alongside the standard scope authorities,
     * so the rules keep their names. The downstream services read {@code realm_access.roles} too.
     */
    static ReactiveJwtAuthenticationConverterAdapter keycloakRoleConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));
            for (String role : realmRoles(jwt)) {
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + role));
            }
            return authorities;
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> realmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return List.of();
        }
        return (Collection<String>) roles;
    }

    /**
     * Fail-closed JWT decoder with LAZY initialization: defers JWKS fetch until first validation request.
     * This allows the Gateway to start even when Keycloak is temporarily unavailable.
     * At request time: if Keycloak's JWKS endpoint is unreachable, returns 503 SERVICE_UNAVAILABLE
     * rather than treating unverifiable tokens as valid (fail-closed posture).
     * 
     * IMPORTANT: Uses jwk-set-uri (internal Docker address) to fetch keys efficiently,
     * but does NOT validate issuer claim. This allows tokens issued by Keycloak accessed
     * via localhost:8180 (from browser) to be validated by Gateway inside Docker.
     * Issuer validation is delegated to individual services if needed.
     */
    @Bean
    public ReactiveJwtDecoder failClosedJwtDecoder() {
        // Lazy initialization: construct JWK Set URI but do NOT fetch yet
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        // Wrap with fail-closed error handling: JWKS fetch failures → 503
        // BUT: allow JWT validation errors (JwtException) to propagate as 401
        return token -> decoder.decode(token)
                .onErrorResume(ex -> {
                    // JWT validation errors (expired, invalid signature, malformed) → propagate for 401
                    if (ex instanceof JwtException) {
                        return Mono.error(ex);
                    }
                    // JWKS fetch failures (network errors, timeouts, DNS failures, etc.) → fail closed with 503
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
