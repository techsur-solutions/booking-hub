package com.bookinghub.booking.client;

import com.bookinghub.booking.client.dto.ClientDtos.LocationResponse;
import com.bookinghub.booking.client.dto.ClientDtos.ResourceResponse;
import com.bookinghub.booking.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

/**
 * Token-relay HTTP client to locations-resources-service (plan 05-02).
 *
 * Named decision (token relay, not service-account credential): locations-resources-service's
 * Tier-2 config (Phase 4 plan 04-01) gates GET endpoints by role_calendar_viewer — a broad
 * baseline "can see the calendar/reference-data at all" gate, directly mirroring legacy's own
 * accesscalendar filter which stacked underneath EVERY Bookings-controller action. Any real
 * user capable of reaching a booking action is expected to also hold role_calendar_viewer as
 * part of normal Keycloak role provisioning. Token relay is therefore sufficient; no separate
 * service-account credential is needed.
 *
 * Named decision (200 with last-known values for soft-deleted records, not 404): Per Phase 4
 * plan 04-02's named decision, getById() on Location/Resource uses plain findById (not
 * findByIdAndDeletedAtIsNull), so soft-deleted rows return 200 with deletedAt populated rather
 * than 404. This client reflects that by returning a populated Optional (not empty) when the
 * response has a deletedAt. 404 maps to Optional.empty() — genuinely nonexistent id only.
 */
@Service
public class LocationsResourcesClient {

    private final RestClient restClient;
    private final CurrentUserProvider currentUserProvider;

    public LocationsResourcesClient(
            @Qualifier("locationsResourcesRestClient") RestClient restClient,
            CurrentUserProvider currentUserProvider) {
        this.restClient = restClient;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Fetches a location by id from locations-resources-service.
     *
     * @param id Location UUID
     * @return Optional with last-known values (including soft-deleted), or empty if genuinely nonexistent (404)
     * @throws org.springframework.web.client.RestClientException for non-404 errors (caller decides how to translate)
     */
    public Optional<LocationResponse> getLocation(UUID id) {
        try {
            LocationResponse response = restClient.get()
                    .uri("/locations/{id}", id)
                    .header("Authorization", "Bearer " + currentUserProvider.getRawBearerToken())
                    .retrieve()
                    .body(LocationResponse.class);
            return Optional.ofNullable(response);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }

    /**
     * Fetches a resource by id from locations-resources-service.
     * The ResourceResponse exposes isUnique and restrictLocations for is_unique-aware
     * conflict detection (callers pre-filter via this before calling ConflictDetectionService).
     *
     * @param id Resource UUID
     * @return Optional with last-known values (including soft-deleted), or empty if genuinely nonexistent (404)
     * @throws org.springframework.web.client.RestClientException for non-404 errors
     */
    public Optional<ResourceResponse> getResource(UUID id) {
        try {
            ResourceResponse response = restClient.get()
                    .uri("/resources/{id}", id)
                    .header("Authorization", "Bearer " + currentUserProvider.getRawBearerToken())
                    .retrieve()
                    .body(ResourceResponse.class);
            return Optional.ofNullable(response);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
