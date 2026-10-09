package com.bookinghub.booking.repository;

import com.bookinghub.booking.domain.BookingResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for BookingResource entities.
 *
 * findByBookingId: used by booking read operations to load associated resources.
 * deleteByBookingId: used by booking delete/update operations to remove stale resource links.
 */
public interface BookingResourceRepository extends JpaRepository<BookingResource, UUID> {

    List<BookingResource> findByBookingId(UUID bookingId);

    void deleteByBookingId(UUID bookingId);
}
