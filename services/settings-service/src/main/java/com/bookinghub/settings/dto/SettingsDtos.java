package com.bookinghub.settings.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTOs for GET/PUT /settings (F10.1/F10.2), mapped to TechArch §4.9's exact
 * snake_case wire shape.
 *
 * SettingsUpdateRequest has every field optional — a PUT may update any
 * subset of the singleton row; a null field is left unchanged by
 * SettingsService.update (partial-update semantics, not full-replace).
 */
public class SettingsDtos {

    public record SettingsUpdateRequest(
            @JsonProperty("approve_booking") Boolean approveBooking,
            @JsonProperty("calendar_slot_size") Integer calendarSlotSize,
            @JsonProperty("calendar_min_time") LocalTime calendarMinTime,
            @JsonProperty("calendar_max_time") LocalTime calendarMaxTime
    ) {}

    public record SettingsResponse(
            @JsonProperty("approve_booking") boolean approveBooking,
            @JsonProperty("calendar_slot_size") int calendarSlotSize,
            @JsonProperty("calendar_min_time") LocalTime calendarMinTime,
            @JsonProperty("calendar_max_time") LocalTime calendarMaxTime,
            @JsonProperty("updated_at") Instant updatedAt,
            @JsonProperty("updated_by") UUID updatedBy
    ) {}
}
