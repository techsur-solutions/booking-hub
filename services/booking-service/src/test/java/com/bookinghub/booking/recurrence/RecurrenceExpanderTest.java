package com.bookinghub.booking.recurrence;

import com.bookinghub.booking.dto.BookingDtos.RecurrenceDefinition;
import com.bookinghub.booking.recurrence.RecurrenceExpander.Occurrence;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for RecurrenceExpander (plan 05-03, Task 1).
 *
 * Tests the FRD's already-adopted interim weekly recurrence shape:
 * base booking Monday 10:00-11:00, daysOfWeek=[1,3,5] (Mon/Wed/Fri per 0=Sunday numbering),
 * endDate = 2 weeks after the base Monday.
 *
 * Expected behavior:
 * - Base Monday occurrence itself is NOT in the result (caller handles it)
 * - All subsequent Mon/Wed/Fri within range are returned in chronological order
 * - Each occurrence preserves the 10:00-11:00 time span
 *
 * No Spring context needed — RecurrenceExpander is a plain @Component with no dependencies.
 */
class RecurrenceExpanderTest {

    private final RecurrenceExpander expander = new RecurrenceExpander();

    /**
     * Scenario: base is Monday 2025-06-02 10:00-11:00 UTC.
     * daysOfWeek=[1,3,5] = Mon/Wed/Fri (FRD's 0=Sunday numbering).
     * endDate = 2025-06-16 (2 weeks after base).
     *
     * Expected occurrences (strictly after 2025-06-02):
     * - Wed 2025-06-04 10:00-11:00
     * - Fri 2025-06-06 10:00-11:00
     * - Mon 2025-06-09 10:00-11:00
     * - Wed 2025-06-11 10:00-11:00
     * - Fri 2025-06-13 10:00-11:00
     * - Mon 2025-06-16 10:00-11:00 (endDate inclusive)
     */
    @Test
    void expandsWeeklyByDayOfWeek_excludingBaseOccurrence_inChronologicalOrder() {
        // Base: Monday 2025-06-02 10:00–11:00 UTC
        Instant baseStart = LocalDate.of(2025, 6, 2).atTime(10, 0).toInstant(ZoneOffset.UTC);
        Instant baseEnd = LocalDate.of(2025, 6, 2).atTime(11, 0).toInstant(ZoneOffset.UTC);

        // daysOfWeek: 1=Mon, 3=Wed, 5=Fri (FRD's 0=Sunday numbering)
        RecurrenceDefinition recurrence = new RecurrenceDefinition(
                "weekly",
                List.of(1, 3, 5),
                LocalDate.of(2025, 6, 16)
        );

        List<Occurrence> occurrences = expander.expand(baseStart, baseEnd, recurrence);

        // Verify count: 2 weeks × 3 days/week = 6, minus the base Monday itself = 5 (wait, let me recount)
        // Week 1 (after base Mon Jun 2): Wed Jun 4, Fri Jun 6 → 2
        // Week 2: Mon Jun 9, Wed Jun 11, Fri Jun 13 → 3
        // Week 3: Mon Jun 16 (endDate = inclusive) → 1
        // Total = 6 occurrences
        assertThat(occurrences).hasSize(6);

        // Verify chronological order
        for (int i = 0; i < occurrences.size() - 1; i++) {
            assertThat(occurrences.get(i).start())
                    .isBefore(occurrences.get(i + 1).start());
        }

        // Verify each occurrence preserves 10:00-11:00 time span
        for (Occurrence occ : occurrences) {
            LocalTime startTime = occ.start().atZone(ZoneOffset.UTC).toLocalTime();
            LocalTime endTime = occ.end().atZone(ZoneOffset.UTC).toLocalTime();
            assertThat(startTime).isEqualTo(LocalTime.of(10, 0));
            assertThat(endTime).isEqualTo(LocalTime.of(11, 0));
        }

        // Verify base Monday (2025-06-02) is NOT in the list
        Instant baseDate = LocalDate.of(2025, 6, 2).atTime(10, 0).toInstant(ZoneOffset.UTC);
        assertThat(occurrences).noneMatch(occ -> occ.start().equals(baseDate));

        // Verify specific dates match expected Wed/Fri/Mon/Wed/Fri/Mon pattern
        assertThat(occurrences.get(0).start().atZone(ZoneOffset.UTC).toLocalDate())
                .isEqualTo(LocalDate.of(2025, 6, 4));  // Wed
        assertThat(occurrences.get(1).start().atZone(ZoneOffset.UTC).toLocalDate())
                .isEqualTo(LocalDate.of(2025, 6, 6));  // Fri
        assertThat(occurrences.get(2).start().atZone(ZoneOffset.UTC).toLocalDate())
                .isEqualTo(LocalDate.of(2025, 6, 9));  // Mon
        assertThat(occurrences.get(3).start().atZone(ZoneOffset.UTC).toLocalDate())
                .isEqualTo(LocalDate.of(2025, 6, 11)); // Wed
        assertThat(occurrences.get(4).start().atZone(ZoneOffset.UTC).toLocalDate())
                .isEqualTo(LocalDate.of(2025, 6, 13)); // Fri
        assertThat(occurrences.get(5).start().atZone(ZoneOffset.UTC).toLocalDate())
                .isEqualTo(LocalDate.of(2025, 6, 16)); // Mon (endDate inclusive)
    }

    /** Edge case: null recurrence → empty list */
    @Test
    void nullRecurrence_returnsEmpty() {
        Instant baseStart = Instant.parse("2025-06-02T10:00:00Z");
        Instant baseEnd = Instant.parse("2025-06-02T11:00:00Z");

        assertThat(expander.expand(baseStart, baseEnd, null)).isEmpty();
    }

    /** Edge case: endDate before base → no sibling occurrences */
    @Test
    void endDateBeforeBase_returnsEmpty() {
        Instant baseStart = Instant.parse("2025-06-02T10:00:00Z");
        Instant baseEnd = Instant.parse("2025-06-02T11:00:00Z");

        RecurrenceDefinition recurrence = new RecurrenceDefinition(
                "weekly",
                List.of(1, 3, 5),
                LocalDate.of(2025, 6, 1) // before base date
        );

        assertThat(expander.expand(baseStart, baseEnd, recurrence)).isEmpty();
    }

    /** Edge case: endDate = base date → no sibling occurrences (strictly AFTER) */
    @Test
    void endDateEqualToBase_returnsEmpty() {
        Instant baseStart = Instant.parse("2025-06-02T10:00:00Z");
        Instant baseEnd = Instant.parse("2025-06-02T11:00:00Z");

        RecurrenceDefinition recurrence = new RecurrenceDefinition(
                "weekly",
                List.of(1, 3, 5),
                LocalDate.of(2025, 6, 2) // same as base date
        );

        // The base date itself is excluded (strictly AFTER), so no occurrences
        assertThat(expander.expand(baseStart, baseEnd, recurrence)).isEmpty();
    }

    /** Verify duration is preserved for multi-hour bookings */
    @Test
    void preservesDurationForMultiHourBooking() {
        // 2-hour booking: Mon 09:00-11:00
        Instant baseStart = LocalDate.of(2025, 6, 2).atTime(9, 0).toInstant(ZoneOffset.UTC);
        Instant baseEnd = LocalDate.of(2025, 6, 2).atTime(11, 0).toInstant(ZoneOffset.UTC);

        RecurrenceDefinition recurrence = new RecurrenceDefinition(
                "weekly",
                List.of(3), // Wednesday only
                LocalDate.of(2025, 6, 4) // just the first Wed
        );

        List<Occurrence> occurrences = expander.expand(baseStart, baseEnd, recurrence);

        assertThat(occurrences).hasSize(1);
        Occurrence occ = occurrences.get(0);
        assertThat(occ.start().atZone(ZoneOffset.UTC).toLocalTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(occ.end().atZone(ZoneOffset.UTC).toLocalTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(Duration.between(occ.start(), occ.end())).isEqualTo(Duration.ofHours(2));
    }
}
