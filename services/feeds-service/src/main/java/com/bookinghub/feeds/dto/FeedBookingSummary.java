package com.bookinghub.feeds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * The EXACT TechArch §4.8 public JSON contract for a single feed booking:
 * {@code {booking_id, title, location:{id,name}, start_time, end_time}}.
 *
 * All four feed formats (RSS2, iCal, JSON, display-board) share this projection
 * DTO — it is the single typed representation the shared FeedQueryService emits,
 * so every renderer reads the same fields and the JSON endpoint serializes this
 * record directly via Jackson.
 *
 * snake_case wire names are pinned with {@code @JsonProperty} so they do not
 * depend on any global naming strategy. {@code start_time}/{@code end_time} are
 * {@link Instant}s and serialize to ISO-8601 strings (TechArch §4.8 types them
 * as {@code ISODateTime}); spring-boot-starter-web registers the JavaTimeModule
 * with write-dates-as-timestamps disabled by default.
 */
public record FeedBookingSummary(
    @JsonProperty("booking_id") UUID bookingId,
    @JsonProperty("title") String title,
    @JsonProperty("location") LocationRef location,
    @JsonProperty("start_time") Instant startTime,
    @JsonProperty("end_time") Instant endTime
) {
    /**
     * Nested location object {@code {id, name}} exactly as TechArch §4.8 specifies —
     * the feed exposes the location's id and (denormalized) display name only.
     */
    public record LocationRef(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name
    ) {}
}
