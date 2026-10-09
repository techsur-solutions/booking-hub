package com.bookinghub.booking.controller;

import com.bookinghub.booking.dto.BookingReadDtos.BookingDetailResponse;
import com.bookinghub.booking.service.BookingReadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for booking read operations — calendar/day/list views and the AJAX
 * detail endpoint (plan 05-04).
 *
 * Endpoints:
 * - GET /bookings         → calendar/day/list views (query-parameter variants of ONE endpoint)
 * - GET /bookings/{id}    → AJAX-style detail, Eventdata-equivalent, last-known-values for deleted
 *
 * Named decision (calendar/day/list as query-parameter variants of one endpoint):
 * Legacy's own index/day/list/building/location actions all ultimately queried the same
 * Event model with different where-clause compositions. TechArch's API table lists exactly
 * one GET /bookings entry with this parameter set (extended here with status/q per F0
 * findings). The frontend composes calendar/day/list UX entirely from how it calls this
 * one endpoint — a narrow from/to for day view, no filters for full calendar, status/q for
 * agenda list. No separate /bookings/day or /bookings/list route exists.
 *
 * Named decision (role gating):
 * The broadest-read surface in booking-service. All three booking roles (viewer/creator/
 * approver) can read bookings — they each have legitimate reasons to see the calendar.
 * Uses @PreAuthorize for the static gate (unlike BookingApprovalController which needs
 * the FRD-named APPROVAL_FORBIDDEN code via manual check).
 */
@RestController
@RequestMapping("/bookings")
public class BookingQueryController {

    private final BookingReadService bookingReadService;

    public BookingQueryController(BookingReadService bookingReadService) {
        this.bookingReadService = bookingReadService;
    }

    /**
     * GET /bookings — calendar/day/list view (query-parameter variants of one endpoint).
     *
     * Required: from, to (Instant, ISO-8601)
     * Optional: location_id (filter to one location), resource_id (filter to bookings
     *   using this resource), status (filter by booking status), q (keyword search on
     *   title/description — named addition beyond TechArch's literal 4-param table,
     *   per F0 findings/01-booking-core.md confirming legacy list-view keyword search)
     *
     * Returns 200 List<BookingDetailResponse> ordered by start_time ASC.
     * conflict_flags are attached to every booking via the SAME ConflictDetectionService
     * create/edit use — in pure read-only mode, never blocking.
     *
     * Role: any of the 3 booking roles (viewer/creator/approver).
     */
    @GetMapping
    @PreAuthorize("hasRole('role_booking_viewer') or hasRole('role_booking_creator') or hasRole('role_booking_approver')")
    public ResponseEntity<List<BookingDetailResponse>> listBookings(
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to,
            @RequestParam(value = "location_id", required = false) UUID locationId,
            @RequestParam(value = "resource_id", required = false) UUID resourceId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "q", required = false) String q) {

        List<BookingDetailResponse> results = bookingReadService.listBookings(
                from, to, locationId, resourceId, status, q);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /bookings/{id} — AJAX-style detail endpoint, Eventdata-equivalent.
     *
     * Named decision (last-known-values policy, mirroring Phase 4's getById precedent):
     * Returns 200 with last-known values (including a populated deleted_at) for a
     * soft-deleted booking. Returns 404 BOOKING_NOT_FOUND ONLY when the id never existed
     * at all (findById returns empty). This is the same policy Phase 4 established for
     * Locations/Resources, extended here for cross-system consistency.
     *
     * Role: any of the 3 booking roles (viewer/creator/approver).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('role_booking_viewer') or hasRole('role_booking_creator') or hasRole('role_booking_approver')")
    public ResponseEntity<BookingDetailResponse> getBooking(@PathVariable UUID id) {
        BookingDetailResponse response = bookingReadService.getById(id);
        return ResponseEntity.ok(response);
    }
}
