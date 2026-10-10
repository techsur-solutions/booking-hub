package com.bookinghub.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Spring configuration exposing one RestClient bean per downstream service (plan 05-02).
 *
 * Uses Spring Framework 6.1's RestClient (included transitively via spring-boot-starter-web
 * on Spring Boot 3.3.x — no WebFlux or extra dependency needed for a synchronous client).
 *
 * Each RestClient is pre-configured with its downstream service's base URL from the
 * downstream.* application.yml properties. Auth headers are NOT set here — each
 * outbound call sets Authorization: Bearer <token> per-request via the calling user's
 * own bearer token (token relay via CurrentUserProvider.getRawBearerToken()).
 */
@Configuration
public class ClientConfig {

    @Bean("locationsResourcesRestClient")
    public RestClient locationsResourcesRestClient(
            @Value("${downstream.locations-resources-service.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("customFieldRestClient")
    public RestClient customFieldRestClient(
            @Value("${downstream.custom-field-service.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("settingsRestClient")
    public RestClient settingsRestClient(
            @Value("${downstream.settings-service.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
