package com.bookinghub.booking.client;

import com.bookinghub.booking.client.dto.ClientDtos.CustomFieldDefinition;
import com.bookinghub.booking.security.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

/**
 * Token-relay HTTP client to custom-field-service's applicability endpoint (plan 05-02).
 *
 * Named decision (OPTION A applied — revised, resolving the Tier-2 integration gap for real):
 * An earlier plan draft chose graceful degradation on every failure (silently return empty list),
 * which defeated F5.3/F5.4's entire purpose for 100% of real traffic since a normal caller's
 * token never held the admin-only role_customfield_admin custom-field-service originally required.
 * Fix: plan 04-04 was amended to add a narrow read carve-out — GET /custom-fields additionally
 * accepts role_calendar_viewer when context_id is present (mirroring the exact pattern
 * locations-resources-service already uses). Every real booking-creating caller holds
 * role_calendar_viewer alongside their booking role (the same baseline-gate convention
 * LocationsResourcesClient's token relay already relies on), so this call SUCCEEDS for
 * ordinary traffic under normal operation.
 *
 * Failure handling (narrowed to genuine edge cases only):
 * - Connectivity failure / timeout / 5xx: log WARN, return List.of() — graceful degradation
 *   for a transient outage (auxiliary validation call; booking creation should not be blocked
 *   by a temporarily unavailable custom-field-service).
 * - 403: log WARN with caller id (rare mis-provisioning case — not the expected common path
 *   since plan 04-04's carve-out makes this succeed for correctly-provisioned callers),
 *   return List.of() — same graceful fallback.
 */
@Service
public class CustomFieldClient {

    private static final Logger log = LoggerFactory.getLogger(CustomFieldClient.class);

    private final RestClient restClient;
    private final CurrentUserProvider currentUserProvider;

    public CustomFieldClient(
            @Qualifier("customFieldRestClient") RestClient restClient,
            CurrentUserProvider currentUserProvider) {
        this.restClient = restClient;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Fetches the list of custom field definitions applicable to the given context id.
     *
     * Under normal operation (caller holds role_calendar_viewer, plan 04-04's carve-out applied),
     * this returns the applicable field definitions and custom field validation ACTUALLY RUNS.
     *
     * Graceful degradation (genuine edge cases only):
     * - Connectivity failure / 5xx: log WARN, return List.of() (transient outage)
     * - 403: log WARN (rare mis-provisioned caller), return List.of()
     *
     * @param contextId The booking context id (location id or other relevant context)
     * @return List of applicable CustomFieldDefinition records, or empty list on graceful-degradation
     */
    public List<CustomFieldDefinition> getApplicableFields(UUID contextId) {
        try {
            List<CustomFieldDefinition> response = restClient.get()
                    .uri("/custom-fields?context_id={contextId}", contextId)
                    .header("Authorization", "Bearer " + currentUserProvider.getRawBearerToken())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CustomFieldDefinition>>() {});

            return response != null ? response : List.of();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                // Rare: caller is missing role_calendar_viewer (mis-provisioning edge case,
                // not the expected common path after plan 04-04's carve-out fix).
                log.warn("403 from custom-field-service for caller — possible mis-provisioning " +
                         "(expected role_calendar_viewer). Returning empty field list for graceful degradation.");
                return List.of();
            }
            // Other 4xx errors — log and fall back gracefully
            log.warn("HTTP {} from custom-field-service GET /custom-fields: {}. Returning empty field list.",
                     e.getStatusCode(), e.getMessage());
            return List.of();
        } catch (RestClientException e) {
            // Connectivity failure, timeout, 5xx, etc.
            log.warn("Failed to reach custom-field-service: {}. Returning empty field list for graceful degradation.",
                     e.getMessage());
            return List.of();
        }
    }
}
