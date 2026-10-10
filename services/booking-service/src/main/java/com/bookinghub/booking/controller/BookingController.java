package com.bookinghub.booking.controller;

import com.bookinghub.booking.dto.BookingDtos.*;
import com.bookinghub.booking.service.BookingWriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the booking write operations (plan 05-03).
 *
 * Endpoints:
 * - POST /bookings          → create a booking (single or recurring series)
 * - PUT /bookings/{id}      → update a booking
 * - DELETE /bookings/{id}   → soft-delete a booking
 * - POST /bookings/{id}/clone → clone a booking (returns non-persisted draft)
 *
 * Named decision (Tier-2 role gating, per TechArch §4.2 + SecurityConfig):
 * - POST /bookings: role_booking_creator only (creating bookings)
 * - PUT/DELETE /bookings/{id}: role_booking_creator OR role_booking_approver at the static
 *   gate; the fine-grained ownership check (creator allowed only for own bookings)
 *   is enforced by BookingWriteService.update()/delete() — not expressible as a static annotation.
 * - POST /bookings/{id}/clone: role_booking_creator (cloning is a create-like operation)
 *
 * Named decision (no scope/EditScope field): PUT and DELETE never receive or expose a scope
 * parameter — F0 confirmed legacy has no series-scoped edit concept past creation time.
 *
 * Named decision (response shape for series): POST /bookings returns a list for recurring
 * series (size N) and unwraps to a single object for standalone bookings (size 1), per
 * TechArch §4.2's "201 Booking (or Booking[] for a series)".
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingWriteService bookingWriteService;

    public BookingController(BookingWriteService bookingWriteService) {
        this.bookingWriteService = bookingWriteService;
    }

    /**
     * POST /bookings — create a booking (single or recurring series).
     *
     * Returns 201 with a single BookingResponse for a standalone booking,
     * or 201 with a List<BookingResponse> for a recurring series.
     *
     * Role: role_booking_creator required.
     */
    @PostMapping
    @PreAuthorize("hasRole('role_booking_creator')")
    public ResponseEntity<?> createBooking(@RequestBody BookingCreateRequest request) {
        List<BookingResponse> results = bookingWriteService.create(request);

        if (results.size() == 1) {
            // Standalone booking — return single object
            return ResponseEntity.status(HttpStatus.CREATED).body(results.get(0));
        } else {
            // Recurring series — return the list
            return ResponseEntity.status(HttpStatus.CREATED).body(results);
        }
    }

    /**
     * PUT /bookings/{id} — update a booking.
     *
     * Role: role_booking_creator (+ ownership check in service) OR role_booking_approver.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('role_booking_creator') or hasRole('role_booking_approver')")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable UUID id,
            @RequestBody BookingUpdateRequest request) {
        BookingResponse response = bookingWriteService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /bookings/{id} — soft-delete a booking.
     *
     * Role: role_booking_creator (+ ownership check in service) OR role_booking_approver.
     * Returns 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('role_booking_creator') or hasRole('role_booking_approver')")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID id) {
        bookingWriteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /bookings/{id}/clone — clone a booking into a non-persisted draft.
     *
     * Returns 201 with a BookingResponse where id=null (the draft marker).
     * The client must POST this draft to /bookings to actually persist it.
     *
     * Role: role_booking_creator required.
     */
    @PostMapping("/{id}/clone")
    @PreAuthorize("hasRole('role_booking_creator')")
    public ResponseEntity<BookingResponse> cloneBooking(@PathVariable UUID id) {
        BookingResponse draft = bookingWriteService.clone(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(draft);
    }
}
