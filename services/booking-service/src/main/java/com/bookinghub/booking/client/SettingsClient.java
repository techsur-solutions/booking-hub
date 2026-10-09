package com.bookinghub.booking.client;

import com.bookinghub.booking.client.dto.ClientDtos.SettingsResponse;
import com.bookinghub.booking.error.ApprovalSettingsUnavailableException;
import com.bookinghub.booking.security.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Token-relay HTTP client to settings-service with a 5-second local cache (plan 05-02).
 *
 * Named decision (5-second local cache, per TechArch §1.5's explicit architectural decision):
 * "synchronous read, with a short-TTL local cache (5s) in booking-service... approveBooking
 * is read exactly once per booking creation... a stale cache for a few seconds has no
 * correctness impact." Implemented via a simple volatile field holding (value, fetchedAtEpochMillis),
 * re-fetching only when the cache is older than 5 seconds. No new caching library dependency
 * needed for a single cached value.
 *
 * Named decision (token relay): settings-service's GET /settings requires NO specific role —
 * any authenticated caller may read it (Phase 4 plan 04-06). The calling user's bearer token
 * is relayed to satisfy the "authenticated" requirement without a service-account credential.
 *
 * Failure handling: any connection failure, timeout, or non-2xx response throws
 * ApprovalSettingsUnavailableException (503) per FRD's exact "Settings Service unavailable
 * when reading approveBooking at creation time" error state (Phase 4 plan 04-05 named decision:
 * "SETTINGS_UNAVAILABLE deliberately not implemented in settings-service — it is the calling
 * service's error when this service is unreachable").
 */
@Service
public class SettingsClient {

    private static final Logger log = LoggerFactory.getLogger(SettingsClient.class);
    private static final long CACHE_TTL_MS = 5_000L;

    private final RestClient restClient;
    private final CurrentUserProvider currentUserProvider;

    /** Cached settings value + the epoch-millis timestamp when it was fetched. */
    private volatile CachedSettings cachedSettings;

    public SettingsClient(
            @Qualifier("settingsRestClient") RestClient restClient,
            CurrentUserProvider currentUserProvider) {
        this.restClient = restClient;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Returns the approveBooking flag from settings-service, using a 5-second local cache.
     *
     * @return true if bookings require approval before being confirmed
     * @throws ApprovalSettingsUnavailableException if settings-service is unreachable or returns non-2xx
     */
    public boolean getApproveBookingFlag() {
        CachedSettings snapshot = cachedSettings;
        long now = System.currentTimeMillis();

        if (snapshot != null && (now - snapshot.fetchedAtEpochMillis) <= CACHE_TTL_MS) {
            // Cache hit — within TTL
            return snapshot.value.approveBooking();
        }

        // Cache miss or expired — re-fetch
        return fetchAndCache().approveBooking();
    }

    private SettingsResponse fetchAndCache() {
        try {
            SettingsResponse response = restClient.get()
                    .uri("/settings")
                    .header("Authorization", "Bearer " + currentUserProvider.getRawBearerToken())
                    .retrieve()
                    .body(SettingsResponse.class);

            if (response == null) {
                throw new ApprovalSettingsUnavailableException();
            }

            cachedSettings = new CachedSettings(response, System.currentTimeMillis());
            return response;
        } catch (RestClientException e) {
            log.warn("Failed to reach settings-service: {}", e.getMessage());
            throw new ApprovalSettingsUnavailableException();
        }
    }

    /** Package-visible for testing: forces the cache to expire (simulates time passing). */
    void expireCache() {
        cachedSettings = null;
    }

    private record CachedSettings(SettingsResponse value, long fetchedAtEpochMillis) {
    }
}
