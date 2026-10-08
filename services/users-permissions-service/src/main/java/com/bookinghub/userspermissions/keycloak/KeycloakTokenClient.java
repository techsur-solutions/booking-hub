package com.bookinghub.userspermissions.keycloak;

import com.bookinghub.userspermissions.error.ApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Keycloak direct-grant token client.
 * 
 * Plain HTTP client against Keycloak's /protocol/openid-connect/token endpoint
 * using confidential direct-grant clients (NOT using keycloak-admin-client library).
 * 
 * Two client profiles:
 * - userperm-direct-grant-client: Standard session lifespan (for login without remember-me)
 * - userperm-direct-grant-remember-client: Extended session lifespan (for login with remember-me)
 * 
 * Both clients are confidential, server-side-only (never exposed to browser JavaScript).
 * The SPA's own login uses PKCE exclusively (Phase 2 §5.1); this direct-grant path exists
 * only so THIS backend can proxy a non-SPA API login and verify a current password.
 * 
 * Zero live Keycloak dependency in unit tests — tested entirely against WireMock.
 */
@Service
public class KeycloakTokenClient {

    private final RestClient restClient;
    private final String tokenUrl;
    private final String directGrantClientId;
    private final String directGrantClientSecret;
    private final String directGrantRememberClientId;
    private final String directGrantRememberClientSecret;

    public KeycloakTokenClient(
            RestClient.Builder restClientBuilder,
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.direct-grant-client-id}") String directGrantClientId,
            @Value("${keycloak.direct-grant-client-secret}") String directGrantClientSecret,
            @Value("${keycloak.direct-grant-remember-client-id}") String directGrantRememberClientId,
            @Value("${keycloak.direct-grant-remember-client-secret}") String directGrantRememberClientSecret) {
        
        this.restClient = restClientBuilder.build();
        this.tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        this.directGrantClientId = directGrantClientId;
        this.directGrantClientSecret = directGrantClientSecret;
        this.directGrantRememberClientId = directGrantRememberClientId;
        this.directGrantRememberClientSecret = directGrantRememberClientSecret;
    }

    /**
     * Performs direct-grant login (Resource Owner Password Credentials flow).
     * 
     * @param username Username (email)
     * @param password User's password
     * @param rememberMe If true, uses extended-lifespan client; if false, standard lifespan
     * @return TokenResponse containing access_token, refresh_token, expires_in
     * @throws AuthInvalidCredentialsException if credentials are invalid (401)
     */
    public TokenResponse directGrantLogin(String username, String password, boolean rememberMe) {
        // Select client credentials based on remember-me flag
        String clientId = rememberMe ? directGrantRememberClientId : directGrantClientId;
        String clientSecret = rememberMe ? directGrantRememberClientSecret : directGrantClientSecret;

        // Build form body
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        try {
            KeycloakTokenResponse keycloakResponse = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);

            if (keycloakResponse == null) {
                throw new RuntimeException("Null response from Keycloak token endpoint");
            }

            return new TokenResponse(
                keycloakResponse.accessToken(),
                keycloakResponse.refreshToken(),
                keycloakResponse.expiresIn()
            );
        } catch (HttpClientErrorException.BadRequest e) {
            // Keycloak returns 400 {"error":"invalid_grant"} for wrong credentials
            if (e.getResponseBodyAsString().contains("invalid_grant")) {
                throw new AuthInvalidCredentialsException("Invalid username or password");
            }
            throw e;
        }
    }

    /**
     * Verifies a user's current password by attempting a token grant.
     * 
     * Used for password-change flows where the caller must prove they know the current password.
     * 
     * @param username Username (email)
     * @param currentPassword Password to verify
     * @return true if password is valid, false if invalid
     */
    public boolean verifyCurrentPassword(String username, String currentPassword) {
        try {
            directGrantLogin(username, currentPassword, false);
            return true;
        } catch (AuthInvalidCredentialsException e) {
            // Invalid credentials → password is wrong
            return false;
        }
    }

    /**
     * Response record from this service's token operations.
     */
    public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
    ) {}

    /**
     * Internal record mapping Keycloak's token endpoint JSON response.
     */
    private record KeycloakTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") long expiresIn
    ) {}
}

/**
 * Authentication failed: invalid credentials — 401 UNAUTHORIZED
 */
class AuthInvalidCredentialsException extends ApiException {
    public AuthInvalidCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", message);
    }
}
