package com.bookinghub.booking.repository;

import com.bookinghub.booking.domain.BookingCustomFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for BookingCustomFieldValue entities.
 *
 * findByBookingId: used by booking read operations to load associated custom field values.
 * deleteByBookingId: used by booking delete/update operations to remove stale custom field values.
 */
public interface BookingCustomFieldValueRepository extends JpaRepository<BookingCustomFieldValue, UUID> {

    List<BookingCustomFieldValue> findByBookingId(UUID bookingId);

    void deleteByBookingId(UUID bookingId);
}
