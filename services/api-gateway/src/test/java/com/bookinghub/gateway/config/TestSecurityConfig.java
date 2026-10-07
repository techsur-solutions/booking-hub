package com.bookinghub.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Test-only security configuration that provides a mock JWT decoder.
 * Prevents tests from attempting to connect to Keycloak at startup.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Mock JWT decoder for tests. Uses a static HMAC secret instead of connecting to Keycloak JWKS.
     * Works with Spring Security's mockJwt() test utilities.
     */
    @Bean
    @Primary
    public ReactiveJwtDecoder testJwtDecoder() {
        // Use a static secret for test JWT validation (HS256)
        // Real production code uses RS256 from Keycloak's rotating keys
        String secret = "test-secret-key-minimum-256-bits-required-for-hs256-algorithm";
        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                MacAlgorithm.HS256.getName()
        );
        
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
