package com.bookinghub.booking.repository;

import com.bookinghub.booking.domain.Booking;
import com.bookinghub.booking.domain.BookingCustomFieldValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the V2 migration (F0 schema completion + custom_field_values ownership fix)
 * applies cleanly on top of V1 and that every new column round-trips correctly
 * through its JPA entity field.
 *
 * Covers:
 * 1. The 6 new F0-confirmed columns on bookings (all_day, description, layout_style,
 *    contact_name, contact_email, contact_no)
 * 2. booking_custom_field_values (new table, local to booking_db, no FK to customfld_db)
 *
 * Uses the running Postgres from docker-compose instead of Testcontainers:
 * this sandbox's Docker daemon reports API version 1.32 while Testcontainers
 * 1.20.4 requires >=1.40 (identical pre-existing incompatibility already hit —
 * and worked around the same way — by Phase 3 plan 03-01's SchemaCompletionTest
 * for users-permissions-service and Phase 4 plan 04-01's SchemaCompletionTest
 * for locations-resources-service). @DataJpaTest wraps each test in a rolled-back
 * transaction, so no cleanup between tests is needed despite sharing the persistent
 * booking_db_test database.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SchemaCompletionTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingCustomFieldValueRepository bookingCustomFieldValueRepository;

    /**
     * Proves all 6 F0-confirmed fields round-trip through JPA:
     * allDay, description, layoutStyle, contactName, contactEmail, contactNo
     */
    @Test
    void bookingRoundTripsAllSixF0ConfirmedFields() {
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(3600);

        Booking booking = new Booking();
        booking.setTitle("Boardroom Session");
        booking.setLocationId(UUID.randomUUID());
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setOwnerId(UUID.randomUUID());
        booking.setAllDay(true);
        booking.setDescription("test description");
        booking.setLayoutStyle("boardroom");
        booking.setContactName("Jane Doe");
        booking.setContactEmail("jane@example.com");
        booking.setContactNo("555-1234");

        Booking saved = bookingRepository.save(booking);
        bookingRepository.flush();

        Booking found = bookingRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getTitle()).isEqualTo("Boardroom Session");
        assertThat(found.isAllDay()).isTrue();
        assertThat(found.getDescription()).isEqualTo("test description");
        assertThat(found.getLayoutStyle()).isEqualTo("boardroom");
        assertThat(found.getContactName()).isEqualTo("Jane Doe");
        assertThat(found.getContactEmail()).isEqualTo("jane@example.com");
        assertThat(found.getContactNo()).isEqualTo("555-1234");
        assertThat(found.getStatus()).isEqualTo("pending");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(found.getDeletedAt()).isNull();
    }

    /**
     * Proves booking_custom_field_values round-trips through JPA.
     * cross-service reference: customFieldId is a plain UUID, no FK to customfld_db.
     */
    @Test
    void bookingCustomFieldValueRoundTripsByBookingId() {
        // First create a booking to reference
        Instant start = Instant.now().plusSeconds(7200);
        Instant end = start.plusSeconds(3600);

        Booking booking = new Booking();
        booking.setTitle("Custom Field Test Booking");
        booking.setLocationId(UUID.randomUUID());
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setOwnerId(UUID.randomUUID());

        Booking savedBooking = bookingRepository.save(booking);
        bookingRepository.flush();

        UUID customFieldId = UUID.randomUUID();
        BookingCustomFieldValue cfv = new BookingCustomFieldValue(
            savedBooking.getId(),
            customFieldId,
            "some-value"
        );
        bookingCustomFieldValueRepository.save(cfv);
        bookingCustomFieldValueRepository.flush();

        List<BookingCustomFieldValue> found = bookingCustomFieldValueRepository
            .findByBookingId(savedBooking.getId());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getCustomFieldId()).isEqualTo(customFieldId);
        assertThat(found.get(0).getValue()).isEqualTo("some-value");
        assertThat(found.get(0).getCreatedAt()).isNotNull();
        assertThat(found.get(0).getUpdatedAt()).isNotNull();
    }

    /**
     * Proves findByIdAndDeletedAtIsNull returns empty for soft-deleted bookings.
     */
    @Test
    void findByIdAndDeletedAtIsNull_hidessoftDeletedBookings() {
        Instant start = Instant.now().plusSeconds(10800);
        Instant end = start.plusSeconds(3600);

        Booking booking = new Booking();
        booking.setTitle("Soft-delete test");
        booking.setLocationId(UUID.randomUUID());
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setOwnerId(UUID.randomUUID());
        booking.setDeletedAt(Instant.now());

        Booking saved = bookingRepository.save(booking);
        bookingRepository.flush();

        assertThat(bookingRepository.findByIdAndDeletedAtIsNull(saved.getId()))
            .isEmpty();
        assertThat(bookingRepository.findById(saved.getId()))
            .isPresent();
    }
}
