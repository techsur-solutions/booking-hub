package com.bookinghub.userspermissions.keycloak;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Keycloak Admin API client configuration.
 * 
 * Builds an authenticated Keycloak admin client using the userperm-admin-client
 * service account with client_credentials grant.
 * 
 * This is the ONE place in the service that authenticates as an administrative
 * actor against Keycloak (TechArch §5.1). Every other interaction with Keycloak
 * is JWT validation only (via Spring Security's oauth2ResourceServer).
 */
@Configuration
public class KeycloakAdminClientConfig {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-client-id}")
    private String adminClientId;

    @Value("${keycloak.admin-client-secret}")
    private String adminClientSecret;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(adminClientId)
                .clientSecret(adminClientSecret)
                .grantType("client_credentials")
                .build();
    }
}
