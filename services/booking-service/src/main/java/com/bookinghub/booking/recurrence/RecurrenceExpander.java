package com.bookinghub.booking.recurrence;

import com.bookinghub.booking.dto.BookingDtos.RecurrenceDefinition;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Expands a recurrence definition into individual time-slot occurrences (plan 05-03).
 *
 * Named decision (FRD's already-adopted interim shape):
 * TechArch §4.1's RecurrenceDefinition type comment: "exact shape pending F0 confirmation...
 * interim assumption: weekly-by-day-of-week with an end date". F0 did not further resolve
 * the exact pattern grammar beyond what legacy's own weekly/monthly-N-times loop showed
 * (a materially different, simpler mechanic). This plan uses the FRD's already-adopted
 * interim shape as-is, not a new invention.
 *
 * Expansion rules (FRD's shape, "weekly" pattern):
 * - daysOfWeek: 0=Sunday, 1=Monday, ..., 6=Saturday (FRD's 0=Sunday numbering, not ISO's)
 * - Starting from baseStart's LOCAL date (using the UTC zone for simplicity — bookings
 *   are assumed to use UTC times, as no timezone context is stored), walk forward one
 *   calendar day at a time up to and including endDate.
 * - For each date whose day-of-week (converted to FRD's 0=Sunday..6=Saturday numbering)
 *   is present in daysOfWeek, AND the date is strictly AFTER baseStart's own date (the
 *   base occurrence itself is handled separately by the caller, not duplicated here),
 *   produce an Occurrence at that date with the SAME time-of-day span as baseStart→baseEnd
 *   (same hour/minute/duration, shifted calendar date only).
 * - Returns occurrences in chronological order.
 *
 * Note: The base occurrence (the booking's own initial date) is NOT included in the
 * returned list — the caller handles it (and commits it) separately as occurrence #1.
 * This expander returns occurrences #2..N only.
 */
@Component
public class RecurrenceExpander {

    /** A single expanded occurrence's time slot. */
    public record Occurrence(Instant start, Instant end) {}

    /**
     * Expands a recurrence definition into sibling occurrences (excluding the base occurrence itself).
     *
     * @param baseStart  The base booking's start time (occurrence #1)
     * @param baseEnd    The base booking's end time (occurrence #1)
     * @param recurrence The recurrence definition (pattern, daysOfWeek, endDate)
     * @return Ordered list of occurrences strictly after baseStart's date, up to and including endDate
     */
    public List<Occurrence> expand(Instant baseStart, Instant baseEnd, RecurrenceDefinition recurrence) {
        if (recurrence == null || recurrence.endDate() == null || recurrence.daysOfWeek() == null
                || recurrence.daysOfWeek().isEmpty()) {
            return List.of();
        }

        // Extract time-of-day span from baseStart/baseEnd (in UTC)
        LocalDate baseDate = baseStart.atZone(ZoneOffset.UTC).toLocalDate();
        LocalTime startTimeOfDay = baseStart.atZone(ZoneOffset.UTC).toLocalTime();
        LocalTime endTimeOfDay = baseEnd.atZone(ZoneOffset.UTC).toLocalTime();

        // Calculate the duration in case end is on the next day (e.g. late-night bookings)
        Duration duration = Duration.between(baseStart, baseEnd);

        LocalDate endDate = recurrence.endDate();
        List<Integer> targetDays = recurrence.daysOfWeek(); // FRD: 0=Sun, 1=Mon, ..., 6=Sat

        List<Occurrence> occurrences = new ArrayList<>();

        // Walk from baseDate+1 (strictly after base) through endDate inclusive
        LocalDate current = baseDate.plusDays(1);
        while (!current.isAfter(endDate)) {
            // Convert Java's DayOfWeek (MONDAY=1..SUNDAY=7) to FRD's (0=Sun..6=Sat)
            int javaDow = current.getDayOfWeek().getValue(); // 1=Mon..7=Sun
            int frdDow = (javaDow == 7) ? 0 : javaDow; // 0=Sun, 1=Mon..6=Sat

            if (targetDays.contains(frdDow)) {
                // Produce an occurrence at this date with the same time-of-day span
                Instant occStart = current.atTime(startTimeOfDay).toInstant(ZoneOffset.UTC);
                Instant occEnd = occStart.plus(duration);
                occurrences.add(new Occurrence(occStart, occEnd));
            }

            current = current.plusDays(1);
        }

        return occurrences;
    }
}
