package com.bookinghub.settings.service;

import com.bookinghub.settings.domain.OutboxEvent;
import com.bookinghub.settings.domain.Settings;
import com.bookinghub.settings.dto.SettingsDtos.SettingsResponse;
import com.bookinghub.settings.dto.SettingsDtos.SettingsUpdateRequest;
import com.bookinghub.settings.error.SettingsInvalidCalendarRangeException;
import com.bookinghub.settings.error.SettingsInvalidSlotSizeException;
import com.bookinghub.settings.repository.OutboxEventRepository;
import com.bookinghub.settings.repository.SettingsRepository;
import com.bookinghub.settings.security.CurrentUserProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service implementing F10.1/F10.2: the approveBooking flag and calendar
 * display parameters, read/written against the singleton Settings row.
 *
 * Named decision — immediate propagation, no reload-required quirk (closes
 * F0's confirmed legacy restart-required caching behavior, point 18 in the
 * phase brief). F0 (findings/05-platform-settings.md) confirmed legacy loads
 * Settings into an in-memory struct ONCE at onApplicationStart — a UI-driven
 * settings change has NO runtime effect until the application is
 * reloaded/restarted. This implementation has NO in-memory cache at all:
 * getCurrent() reads the database directly on every call, so a PUT is
 * visible to the very next GET with zero propagation delay. This is a
 * deliberate, explicitly-named improvement over legacy's confirmed behavior,
 * not a silent divergence. Separately, this service ALSO publishes
 * settings.updated (via the outbox) for any dependent service that wants an
 * event-driven signal in addition to the always-fresh synchronous read.
 */
@Service
public class SettingsService {

    /**
     * Fixed sentinel UUID representing the singleton Settings aggregate for
     * outbox rows — the Settings entity's own PK is the integer 1, but every
     * other service's outbox table uses a UUID-shaped aggregate_id, so this
     * service uses a fixed, well-known UUID here for cross-service
     * consistency (there is only ever one Settings aggregate, so a single
     * constant is all that's needed — never randomly generated per write,
     * so tooling/consumers can recognize "the" settings aggregate across
     * every event it ever emits).
     */
    private static final UUID SETTINGS_AGGREGATE_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final int SINGLETON_ID = 1;

    private final SettingsRepository settingsRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ObjectMapper objectMapper;

    public SettingsService(SettingsRepository settingsRepository,
                            OutboxEventRepository outboxEventRepository,
                            CurrentUserProvider currentUserProvider,
                            ObjectMapper objectMapper) {
        this.settingsRepository = settingsRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.currentUserProvider = currentUserProvider;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns the current settings singleton. The row always exists
     * (pre-seeded by Phase 2's V1 migration) — no not-found case is possible
     * for this singleton, so no exception path exists here.
     *
     * Reads the database directly on every call (no in-memory cache) — see
     * class javadoc's named decision on immediate propagation.
     */
    public SettingsResponse getCurrent() {
        Settings settings = settingsRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Settings singleton row (id=1) not found — Phase 2 seed is missing"));
        return toResponse(settings);
    }

    /**
     * Updates the settings singleton (F10.3: upsert-against-fixed-id
     * discipline). Always fetches the EXISTING row via findById(1) — never
     * constructs a new Settings() with a different id. There is no create
     * path for Settings at all, matching the FRD's explicit "no
     * create/delete endpoints exist for Settings" statement.
     *
     * Applies only the fields present in request (partial update — null
     * fields are left unchanged).
     *
     * Server-side validation (defense in depth beyond the DB CHECK
     * constraints): validates the resulting (post-merge) calendar range and
     * slot size, so that changing only ONE of calendarMinTime/calendarMaxTime
     * is validated against the OTHER field's current persisted value, not a
     * stale pre-merge pair.
     */
    @Transactional
    public SettingsResponse update(SettingsUpdateRequest request) {
        Settings settings = settingsRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Settings singleton row (id=1) not found — Phase 2 seed is missing"));

        if (request.approveBooking() != null) {
            settings.setApproveBooking(request.approveBooking());
        }

        // Compute the POST-merge calendar fields before validating/persisting,
        // so a single-field change is validated against the other field's
        // current persisted value rather than a stale pre-merge pair.
        LocalTime resultingMinTime = request.calendarMinTime() != null
                ? request.calendarMinTime()
                : settings.getCalendarMinTime();
        LocalTime resultingMaxTime = request.calendarMaxTime() != null
                ? request.calendarMaxTime()
                : settings.getCalendarMaxTime();

        if (!resultingMinTime.isBefore(resultingMaxTime)) {
            throw new SettingsInvalidCalendarRangeException(
                    "calendar_min_time must be strictly before calendar_max_time (got min="
                            + resultingMinTime + ", max=" + resultingMaxTime + ")");
        }

        Integer resultingSlotSize = request.calendarSlotSize() != null
                ? request.calendarSlotSize()
                : settings.getCalendarSlotSize();

        if (resultingSlotSize <= 0) {
            throw new SettingsInvalidSlotSizeException(
                    "calendar_slot_size must be positive (got " + resultingSlotSize + ")");
        }

        if (request.calendarMinTime() != null) {
            settings.setCalendarMinTime(request.calendarMinTime());
        }
        if (request.calendarMaxTime() != null) {
            settings.setCalendarMaxTime(request.calendarMaxTime());
        }
        if (request.calendarSlotSize() != null) {
            settings.setCalendarSlotSize(request.calendarSlotSize());
        }

        UUID currentUserId = currentUserProvider.getCurrentUserId();
        settings.setUpdatedBy(currentUserId);
        settings.setUpdatedAt(Instant.now());

        settingsRepository.save(settings);

        // Write the outbox row in the SAME transaction as the business change.
        Map<String, Object> payload = new HashMap<>();
        payload.put("approve_booking", settings.isApproveBooking());
        payload.put("calendar_slot_size", settings.getCalendarSlotSize());
        payload.put("calendar_min_time", settings.getCalendarMinTime().toString());
        payload.put("calendar_max_time", settings.getCalendarMaxTime().toString());
        payload.put("updated_at", settings.getUpdatedAt().toString());
        payload.put("updated_by", currentUserId.toString());

        OutboxEvent event = new OutboxEvent(
                "settings",
                SETTINGS_AGGREGATE_ID,
                "settings.events",
                "settings.updated",
                serializePayload(payload)
        );
        outboxEventRepository.save(event);

        return toResponse(settings);
    }

    private SettingsResponse toResponse(Settings settings) {
        return new SettingsResponse(
                settings.isApproveBooking(),
                settings.getCalendarSlotSize(),
                settings.getCalendarMinTime(),
                settings.getCalendarMaxTime(),
                settings.getUpdatedAt(),
                settings.getUpdatedBy()
        );
    }

    private String serializePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }
    }
}
