package com.bookinghub.booking.conflict;

import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictCheckResult;
import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictFlag;
import com.bookinghub.booking.repository.BookingRepository;
import com.bookinghub.booking.repository.BookingResourceRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The single, shared conflict-evaluation engine for booking-service (plan 05-02).
 *
 * Named decision (ZERO cross-service HTTP dependencies, by design):
 * TechArch describes this as "an internal Booking Service function" querying only
 * tables this service owns (bookings, booking_resources). The is_unique-awareness
 * required by F2.2 is a CALLER responsibility: the caller (ConflictController,
 * plan 05-03's BookingWriteService, plan 05-04's BookingReadService) resolves each
 * resource's is_unique flag via LocationsResourcesClient FIRST (a call it already
 * needs to make anyway for existence validation), filters the resource id list down
 * to only the unique ones, and passes that pre-filtered list in. This keeps this
 * service maximally simple, fast, and independently testable with zero mocking
 * of external services.
 *
 * Named decision (UNION of per-resource results, called once per resource id in a loop):
 * TechArch §3.3 describes "each attached resource is checked independently... the
 * booking's full conflict result is the UNION of the location-level result and every
 * per-resource result." The per-resource loop is simpler and safer than a single
 * IN/ANY array query (Postgres UUID-array JDBC binding is awkward without a custom
 * type), and produces the identical result set.
 *
 * This method NEVER throws. It NEVER enforces anything (no 409, no block).
 * It NEVER checks the caller's role. It is a pure, side-effect-free query.
 * Enforcement (hard-block vs. soft-warning decision based on result) is a SEPARATE,
 * single call site that plan 05-03 adds around this method's result — not here.
 */
@Service
public class ConflictDetectionService {

    private final BookingRepository bookingRepository;
    private final BookingResourceRepository bookingResourceRepository;

    public ConflictDetectionService(
            BookingRepository bookingRepository,
            BookingResourceRepository bookingResourceRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingResourceRepository = bookingResourceRepository;
    }

    /**
     * Checks for booking conflicts using TechArch §3.3's half-open-interval semantics.
     *
     * @param locationId        The location to check for location-level conflicts, or null to skip
     * @param uniqueResourceIds Resources to check for resource-level conflicts (pre-filtered to
     *                          is_unique=true by the caller — see named decision above)
     * @param proposedStart     Proposed booking start time (inclusive)
     * @param proposedEnd       Proposed booking end time (exclusive, half-open interval)
     * @param excludeBookingId  On edit, the booking's own id to self-exclude; null for new bookings
     * @return ConflictCheckResult with hasConflict flag and the union of all detected conflicts
     */
    public ConflictCheckResult checkConflicts(
            UUID locationId,
            List<UUID> uniqueResourceIds,
            Instant proposedStart,
            Instant proposedEnd,
            UUID excludeBookingId) {

        List<ConflictFlag> conflicts = new ArrayList<>();

        // Step 1: Location-level conflict check (if locationId provided)
        if (locationId != null) {
            List<UUID> locationConflicts = bookingRepository.findConflictingBookingIdsByLocation(
                    locationId, proposedStart, proposedEnd, excludeBookingId);

            for (UUID conflictingId : locationConflicts) {
                conflicts.add(new ConflictFlag(conflictingId, "location", locationId, null));
            }
        }

        // Step 2: Per-resource conflict checks (union of results, per TechArch §3.3)
        // uniqueResourceIds is already pre-filtered to is_unique=true by the caller.
        if (uniqueResourceIds != null) {
            for (UUID resourceId : uniqueResourceIds) {
                List<UUID> resourceConflicts = bookingResourceRepository.findConflictingBookingIdsByResource(
                        resourceId, proposedStart, proposedEnd, excludeBookingId);

                for (UUID conflictingId : resourceConflicts) {
                    conflicts.add(new ConflictFlag(conflictingId, "resource", null, resourceId));
                }
            }
        }

        return new ConflictCheckResult(!conflicts.isEmpty(), conflicts);
    }
}
