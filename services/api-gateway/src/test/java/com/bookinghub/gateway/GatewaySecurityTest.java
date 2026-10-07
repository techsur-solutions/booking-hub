package com.bookinghub.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

/**
 * Security tests for Spring Cloud Gateway using WebTestClient and mockJwt().
 * Tests 401/403/public-passthrough behavior without requiring a live Keycloak.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GatewaySecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testBookingsEndpoint_noAuthorizationHeader_returns401() {
        webTestClient
                .get()
                .uri("/bookings")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error_code").isEqualTo("AUTH_UNAUTHENTICATED");
    }

    @Test
    public void testBookingsEndpoint_jwtWithNoRoles_returns403() {
        webTestClient
                .mutateWith(mockJwt())
                .get()
                .uri("/bookings")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error_code").isEqualTo("GATEWAY_FORBIDDEN");
    }

    @Test
    public void testBookingsEndpoint_jwtWithBookingViewerRole_passesSecurityFilter() {
        // The downstream service is unreachable in test context, so we expect 503 from CircuitBreaker fallback
        // The key assertion: NOT 401 or 403, proving the role check passed
        webTestClient
                .mutateWith(mockJwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_role_booking_viewer")
                ))
                .get()
                .uri("/bookings")
                .exchange()
                .expectStatus().is5xxServerError(); // 503 from unreachable downstream, not 401/403 from security
    }

    @Test
    public void testFeedsEndpoint_noToken_passesSecurityFilter() {
        // Public route - should pass security even with no token
        // Downstream unreachable → 503, but NOT 401 (proves public bypass works)
        webTestClient
                .get()
                .uri("/feeds/json")
                .exchange()
                .expectStatus().is5xxServerError(); // 503 from unreachable downstream, never 401
    }

    @Test
    public void testAuthLoginEndpoint_noToken_passesSecurityFilter() {
        // Public route - POST /auth/login should not require token
        webTestClient
                .post()
                .uri("/auth/login")
                .exchange()
                .expectStatus().is5xxServerError(); // 503 from unreachable downstream, not 401
    }

    @Test
    public void testLocationsEndpoint_authenticatedForGet_noSpecificRole() {
        // GET /locations requires authenticated, but no specific role
        webTestClient
                .mutateWith(mockJwt())
                .get()
                .uri("/locations")
                .exchange()
                .expectStatus().is5xxServerError(); // Passes security (authenticated), fails on unreachable downstream
    }

    @Test
    public void testLocationsEndpoint_postWithoutLocationAdminRole_returns403() {
        // POST /locations requires role_location_admin
        webTestClient
                .mutateWith(mockJwt())
                .post()
                .uri("/locations")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error_code").isEqualTo("GATEWAY_FORBIDDEN");
    }

    @Test
    public void testLocationsEndpoint_postWithLocationAdminRole_passesSecurityFilter() {
        webTestClient
                .mutateWith(mockJwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_role_location_admin")
                ))
                .post()
                .uri("/locations")
                .exchange()
                .expectStatus().is5xxServerError(); // Passes security, fails on unreachable downstream
    }

    @Test
    public void testCustomFieldsEndpoint_withoutCustomFieldAdminRole_returns403() {
        webTestClient
                .mutateWith(mockJwt())
                .get()
                .uri("/custom-fields")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error_code").isEqualTo("GATEWAY_FORBIDDEN");
    }

    @Test
    public void testCustomFieldsEndpoint_withCustomFieldAdminRole_passesSecurityFilter() {
        webTestClient
                .mutateWith(mockJwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_role_customfield_admin")
                ))
                .get()
                .uri("/custom-fields")
                .exchange()
                .expectStatus().is5xxServerError(); // Passes security, fails on unreachable downstream
    }

    @Test
    public void testSettingsEndpoint_getRequiresAuthenticated() {
        webTestClient
                .mutateWith(mockJwt())
                .get()
                .uri("/settings")
                .exchange()
                .expectStatus().is5xxServerError(); // Passes security (authenticated), fails on unreachable downstream
    }

    @Test
    public void testSettingsEndpoint_putWithoutSettingsAdminRole_returns403() {
        webTestClient
                .mutateWith(mockJwt())
                .put()
                .uri("/settings")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error_code").isEqualTo("GATEWAY_FORBIDDEN");
    }

    @Test
    public void testSettingsEndpoint_putWithSettingsAdminRole_passesSecurityFilter() {
        webTestClient
                .mutateWith(mockJwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_role_settings_admin")
                ))
                .put()
                .uri("/settings")
                .exchange()
                .expectStatus().is5xxServerError(); // Passes security, fails on unreachable downstream
    }

    @Test
    public void testAuditLogEndpoint_withoutAuditViewerRole_returns403() {
        webTestClient
                .mutateWith(mockJwt())
                .get()
                .uri("/audit-log")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.error_code").isEqualTo("GATEWAY_FORBIDDEN");
    }

    @Test
    public void testAuditLogEndpoint_withAuditViewerRole_passesSecurityFilter() {
        webTestClient
                .mutateWith(mockJwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_role_audit_viewer")
                ))
                .get()
                .uri("/audit-log")
                .exchange()
                .expectStatus().is5xxServerError(); // Passes security, fails on unreachable downstream
    }
}
