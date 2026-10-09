package com.bookinghub.booking.service;

import com.bookinghub.booking.client.CustomFieldClient;
import com.bookinghub.booking.client.LocationsResourcesClient;
import com.bookinghub.booking.client.SettingsClient;
import com.bookinghub.booking.client.dto.ClientDtos.CustomFieldDefinition;
import com.bookinghub.booking.client.dto.ClientDtos.ResourceResponse;
import com.bookinghub.booking.conflict.ConflictDetectionService;
import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictCheckResult;
import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictFlag;
import com.bookinghub.booking.domain.Booking;
import com.bookinghub.booking.domain.BookingCustomFieldValue;
import com.bookinghub.booking.domain.BookingResource;
import com.bookinghub.booking.domain.OutboxEvent;
import com.bookinghub.booking.dto.BookingDtos.*;
import com.bookinghub.booking.error.*;
import com.bookinghub.booking.recurrence.RecurrenceExpander;
import com.bookinghub.booking.recurrence.RecurrenceExpander.Occurrence;
import com.bookinghub.booking.repository.*;
import com.bookinghub.booking.security.CurrentUserProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic for booking create/update/delete/clone (plan 05-03).
 *
 * Central design principle: the CONFLICT-ENFORCEMENT-POLICY DECISION — the plan's
 * most important architectural choice — is implemented through exactly ONE private
 * method, {@link #applyConflictPolicy(ConflictCheckResult)}, called from both
 * {@code create} and {@code update}. This method is NEVER duplicated per entry point.
 *
 * The policy (implementing ROADMAP's literal Success Criterion 4, diverging from
 * TechArch §1.5's uniform-hard-block in favor of the permission-conditional split):
 * - A detected conflict → 409 BOOKING_CONFLICT for callers without role_booking_approver
 * - A detected conflict → soft warning (booking saved, conflict_flags populated) for
 *   callers holding role_booking_approver
 *
 * See plan 05-03's objective for the full reasoning behind this divergence.
 *
 * Named decision (bypass-approve role folding, F0 point 4 RESOLVED):
 * Phase 3 plan 03-01 explicitly folded legacy's separate {@code bypassApproveBooking}
 * permission into the single {@code role_booking_approver} Keycloak role. Checking
 * {@code role_booking_approver} covers BOTH the approve/deny gate AND the bypass gate.
 *
 * Named decision (recurring series, F0 parity):
 * The bypass-to-auto-approve override is applied ONLY to occurrence #1 (the base occurrence).
 * Every subsequent occurrence in the series receives the plain {@code approveBooking}-driven
 * status ({@code pending} if the flag is on, {@code approved} if off) — no bypass re-application.
 * This mirrors F0 findings/01-booking-core.md's confirmed legacy quirk exactly.
 *
 * Named decision (clone):
 * Clone returns a non-persisted draft BookingResponse per F0's confirmed legacy behavior —
 * "renders a pre-filled add form, does not persist". The client must POST the draft to
 * actually create a row.
 *
 * Named decision (custom field validation):
 * CustomFieldClient.getApplicableFields() now returns real data under normal operation
 * (plan 04-04's carve-out fix, plan 05-02's OPTION A applied). An inapplicable field_id
 * throws BookingCustomFieldNotApplicableException (400). Genuine connectivity failures
 * fall back gracefully (skip validation, log warning).
 */
@Service
public class BookingWriteService {

    private static final Logger log = LoggerFactory.getLogger(BookingWriteService.class);

    private final BookingRepository bookingRepository;
    private final BookingResourceRepository bookingResourceRepository;
    private final BookingCustomFieldValueRepository bookingCustomFieldValueRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ConflictDetectionService conflictDetectionService;
    private final LocationsResourcesClient locationsResourcesClient;
    private final CustomFieldClient customFieldClient;
    private final SettingsClient settingsClient;
    private final CurrentUserProvider currentUserProvider;
    private final RecurrenceExpander recurrenceExpander;
    private final ObjectMapper objectMapper;

    public BookingWriteService(
            BookingRepository bookingRepository,
            BookingResourceRepository bookingResourceRepository,
            BookingCustomFieldValueRepository bookingCustomFieldValueRepository,
            OutboxEventRepository outboxEventRepository,
            ConflictDetectionService conflictDetectionService,
            LocationsResourcesClient locationsResourcesClient,
            CustomFieldClient customFieldClient,
            SettingsClient settingsClient,
            CurrentUserProvider currentUserProvider,
            RecurrenceExpander recurrenceExpander,
            ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.bookingResourceRepository = bookingResourceRepository;
        this.bookingCustomFieldValueRepository = bookingCustomFieldValueRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.conflictDetectionService = conflictDetectionService;
        this.locationsResourcesClient = locationsResourcesClient;
        this.customFieldClient = customFieldClient;
        this.settingsClient = settingsClient;
        this.currentUserProvider = currentUserProvider;
        this.recurrenceExpander = recurrenceExpander;
        this.objectMapper = objectMapper;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Creates a booking (single or recurring series).
     *
     * Returns a list of BookingResponse — size 1 for a standalone booking,
     * size N for a recurring series, per TechArch §4.2's "201 Booking (or Booking[] for a series)".
     *
     * @throws BookingTitleRequiredException         if title is blank (400)
     * @throws BookingInvalidTimeRangeException      if end time is not strictly after start time (400)
     * @throws BookingLocationNotFoundException      if location does not exist or is soft-deleted (404)
     * @throws BookingResourceNotFoundException      if any resource does not exist or is soft-deleted (404)
     * @throws BookingCustomFieldNotApplicableException if a custom field is not applicable (400)
     * @throws BookingConflictException              if conflict detected and caller lacks role_booking_approver (409)
     * @throws ApprovalSettingsUnavailableException  if settings-service is unreachable (503)
     */
    @Transactional
    public List<BookingResponse> create(BookingCreateRequest request) {
        // Step 1: Title validation
        if (request.title() == null || request.title().isBlank()) {
            throw new BookingTitleRequiredException();
        }

        // Step 2: Resolve times (default end = start + 1h)
        Instant startTime = request.startTime();
        Instant endTime = request.endTime() != null
                ? request.endTime()
                : startTime.plus(1, ChronoUnit.HOURS);

        // Step 3: Validate time range (zero-duration rejected — defense in depth)
        if (!endTime.isAfter(startTime)) {
            throw new BookingInvalidTimeRangeException();
        }

        // Step 4: Validate location (must exist AND not soft-deleted)
        validateLocationForCreate(request.locationId());

        // Steps 5-6: Validate resources and collect unique resource IDs
        List<UUID> uniqueResourceIds = resolveAndValidateResources(request.resourceIds());

        // Step 7: Check conflicts
        ConflictCheckResult conflictResult = conflictDetectionService.checkConflicts(
                request.locationId(), uniqueResourceIds, startTime, endTime, null);

        // Step 8: Apply conflict enforcement policy (SINGLE call site, shared with update)
        applyConflictPolicy(conflictResult);

        // Step 9: Determine initial status (with bypass-approve mechanism for occurrence #1)
        boolean approveBooking = settingsClient.getApproveBookingFlag();
        String status = determineStatus(approveBooking, true /* apply bypass for first occurrence */);

        // Step 10: Validate custom field values applicability
        validateCustomFieldValues(request.locationId(), request.customFieldValues());

        // Steps 11-12: Persist first occurrence and outbox event in one transaction
        UUID seriesId = request.recurrence() != null ? UUID.randomUUID() : null;
        Booking firstBooking = persistBookingWithResources(
                request.title(), request.locationId(), startTime, endTime,
                request.resourceIds(), request.customFieldValues(), status, seriesId,
                request.allDay(), request.description(), request.layoutStyle(),
                request.contactName(), request.contactEmail(), request.contactNo());

        publishOutboxEvent(firstBooking, "booking.created",
                buildCreatePayload(firstBooking, request.emailContact()));

        BookingResponse firstResponse = toResponse(firstBooking, conflictResult.conflicts());

        // Step 13: Recurring bulk-create (if recurrence requested)
        if (request.recurrence() != null) {
            return createRecurringSeries(
                    firstResponse, firstBooking, request, startTime, endTime,
                    approveBooking, seriesId, uniqueResourceIds);
        }

        return List.of(firstResponse);
    }

    /**
     * Updates an existing booking's fields.
     *
     * Status is never touched by update per F0 (legacy's update() never re-runs checkApproval).
     *
     * @throws BookingNotFoundException         if booking does not exist or is soft-deleted (404)
     * @throws BookingForbiddenException        if caller lacks ownership or approver role (403)
     * @throws BookingInvalidTimeRangeException if updated time range is invalid (400)
     * @throws BookingLocationNotFoundException if updated location does not exist or is soft-deleted (404)
     * @throws BookingResourceNotFoundException if any updated resource does not exist or is soft-deleted (404)
     * @throws BookingCustomFieldNotApplicableException if updated custom field is not applicable (400)
     * @throws BookingConflictException         if conflict detected and caller lacks role_booking_approver (409)
     */
    @Transactional
    public BookingResponse update(UUID id, BookingUpdateRequest request) {
        // Step 1: Find the booking (soft-delete aware)
        Booking booking = bookingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BookingNotFoundException(id.toString()));

        // Step 2: Ownership check (TechArch §4.2's "(owner) or admin")
        checkOwnership(booking);

        // Step 3: Apply partial updates — resolve start/end times
        Instant startTime = request.startTime() != null ? request.startTime() : booking.getStartTime();
        Instant endTime = request.endTime() != null ? request.endTime() : booking.getEndTime();

        if (request.startTime() != null || request.endTime() != null) {
            if (!endTime.isAfter(startTime)) {
                throw new BookingInvalidTimeRangeException();
            }
        }

        UUID locationId = request.locationId() != null ? request.locationId() : booking.getLocationId();

        // Re-validate location if changed
        if (request.locationId() != null) {
            validateLocationForCreate(locationId);
        }

        // Re-validate resources and get unique IDs for conflict detection.
        // When resource_ids is absent the existing DB resource set is used so that a
        // time-only update still runs the full resource-level conflict check.
        // resolveAndValidateResources() filters to is_unique=true (caller responsibility
        // per ConflictDetectionService's named decision).
        List<UUID> candidateResourceIds = request.resourceIds() != null
                ? request.resourceIds()
                : bookingResourceRepository.findByBookingId(id).stream()
                        .map(BookingResource::getResourceId)
                        .collect(Collectors.toList());

        List<UUID> uniqueResourceIds = resolveAndValidateResources(candidateResourceIds);

        // Step 4: Re-run conflict detection, excluding this booking's own prior state
        ConflictCheckResult conflictResult = conflictDetectionService.checkConflicts(
                locationId, uniqueResourceIds, startTime, endTime, id);

        // Step 5: Apply the SAME conflict policy (single shared call site)
        applyConflictPolicy(conflictResult);

        // Step 3 (continued): Validate custom field applicability if provided
        if (request.customFieldValues() != null) {
            validateCustomFieldValues(locationId, request.customFieldValues());
        }

        // Step 6: Status is NEVER touched by update (F0 confirmed)

        // Step 7: Persist changes
        applyFieldUpdates(booking, request, startTime, endTime, locationId);
        bookingRepository.save(booking);

        // Replace resource set (delete-then-recreate for REPLACE semantics)
        if (request.resourceIds() != null) {
            bookingResourceRepository.deleteByBookingId(id);
            for (UUID resourceId : request.resourceIds()) {
                bookingResourceRepository.save(new BookingResource(id, resourceId));
            }
        }

        // Replace custom field value set (delete-then-recreate)
        if (request.customFieldValues() != null) {
            bookingCustomFieldValueRepository.deleteByBookingId(id);
            for (CustomFieldValueInput cfv : request.customFieldValues()) {
                bookingCustomFieldValueRepository.save(
                        new BookingCustomFieldValue(id, cfv.fieldId(), cfv.value()));
            }
        }

        // Step 8: Publish outbox event
        publishOutboxEvent(booking, "booking.updated", buildUpdatePayload(booking));

        return toResponse(booking, conflictResult.conflicts());
    }

    /**
     * Soft-deletes a booking.
     *
     * @throws BookingNotFoundException   if booking does not exist or is soft-deleted (404)
     * @throws BookingForbiddenException  if caller lacks ownership or approver role (403)
     */
    @Transactional
    public void delete(UUID id) {
        // Step 1: Find the booking
        Booking booking = bookingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BookingNotFoundException(id.toString()));

        // Ownership check
        checkOwnership(booking);

        // Step 2: Soft-delete (FRD's adopted interim policy)
        booking.setDeletedAt(Instant.now());
        bookingRepository.save(booking);

        // Step 3: Publish outbox event
        publishOutboxEvent(booking, "booking.deleted", buildDeletePayload(booking));
    }

    /**
     * Returns a non-persisted draft BookingResponse copying the source booking's fields.
     *
     * Named decision (F0 point 7 RESOLVED — non-persisted draft):
     * Clone "renders a pre-filled add form, does not persist". The client must POST the
     * returned draft to actually create a row.
     *
     * Named decision (F0 point 8 — clone conflict-check quirk resolved by design):
     * Legacy preserved the source id in a hidden concurrency-check field on the clone form,
     * so the clone form never flagged conflicts against the original event. Since this design's
     * clone is a non-persisted draft submitted via normal POST /bookings (no excludeBookingId),
     * a fresh POST correctly conflict-checks against ALL existing bookings — the quirk cannot
     * mechanically recur. This is MORE correct than legacy's behavior.
     *
     * @throws BookingSourceNotFoundException if source booking does not exist or is soft-deleted (404)
     */
    @Transactional(readOnly = true)
    public BookingResponse clone(UUID sourceId) {
        // Step 1: Find the source booking
        Booking source = bookingRepository.findByIdAndDeletedAtIsNull(sourceId)
                .orElseThrow(() -> new BookingSourceNotFoundException(sourceId.toString()));

        // Fetch associated resource IDs
        List<UUID> resourceIds = bookingResourceRepository.findByBookingId(sourceId)
                .stream().map(BookingResource::getResourceId).collect(Collectors.toList());

        // Fetch associated custom field values
        List<BookingCustomFieldValue> cfvs = bookingCustomFieldValueRepository.findByBookingId(sourceId);
        List<CustomFieldValueOutput> cfvOutputs = cfvs.stream()
                .map(cfv -> new CustomFieldValueOutput(cfv.getCustomFieldId(), cfv.getValue(), null, null))
                .collect(Collectors.toList());

        // Step 2: Compute fresh status for the calling user (not copied from source)
        boolean approveBooking = settingsClient.getApproveBookingFlag();
        String freshStatus = determineStatus(approveBooking, true /* apply bypass */);

        // Step 3: Build the non-persisted draft response
        // id = null (draft marker — never persisted)
        // seriesId = null (clone is never part of the original series)
        // ownerId = calling user's id (fresh submission, not the original owner's)
        // approvedBy/approvedAt/deniedBy/deniedAt/denialReason = null (fresh start)
        // createdAt/updatedAt/deletedAt = null (not persisted)
        // conflictFlags = [] (conflict evaluation happens when client POSTs the draft)
        return new BookingResponse(
                null,                                   // id = null (draft)
                null,                                   // seriesId = null (not in original series)
                source.getTitle(),
                source.getLocationId(),
                source.getStartTime(),
                source.getEndTime(),
                resourceIds,
                cfvOutputs,
                freshStatus,
                currentUserProvider.getCurrentUserId(), // ownerId = calling user
                null, null,                             // approvedBy/approvedAt = null
                null, null, null,                       // deniedBy/deniedAt/denialReason = null
                source.isAllDay(),
                source.getDescription(),
                source.getLayoutStyle(),
                source.getContactName(),
                source.getContactEmail(),
                source.getContactNo(),
                List.of(),                              // conflictFlags = [] (not checked for draft)
                null, null, null                        // createdAt/updatedAt/deletedAt = null
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // THE CONFLICT ENFORCEMENT POLICY — SINGLE CALL SITE, NEVER DUPLICATED
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Applies the plan's central conflict-enforcement-policy decision.
     *
     * Called from BOTH {@code create} and {@code update} — never duplicated per entry point.
     *
     * Policy (ROADMAP Success Criterion 4, implementing permission-conditional split):
     * - Hard block (409 BOOKING_CONFLICT) for callers without role_booking_approver
     * - Soft warning (booking proceeds, conflict_flags populated in response) for callers
     *   holding role_booking_approver
     *
     * Named decision: this DIVERGES from TechArch §1.5's documented decision (uniform hard
     * block for ALL roles) in favor of ROADMAP's literal wording. Future documentation pass
     * should correct TechArch §1.5 to match this implementation.
     *
     * @param result The conflict check result from ConflictDetectionService
     * @throws BookingConflictException if conflict detected and caller lacks role_booking_approver
     */
    private void applyConflictPolicy(ConflictCheckResult result) {
        if (result.hasConflict() && !currentUserProvider.hasRole("role_booking_approver")) {
            throw new BookingConflictException(); // 409 — hard block for non-approver
        }
        // else: soft warning — proceed; conflict_flags attached to response via toResponse()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Determines the initial booking status, with optional bypass-approve override.
     *
     * Named decision (two-stage mechanic, F0 parity):
     * 1. If approveBooking=false → "approved" directly.
     * 2. If approveBooking=true → "pending" initially.
     * 3. Then — if caller holds role_booking_approver AND applyBypass=true → override to "approved".
     *    applyBypass is false for recurring series occurrences after #1 (legacy parity).
     *
     * @param approveBooking The current approveBooking flag from settings-service
     * @param applyBypass    Whether to apply the bypass-approve override (true for #1, false for #2..N)
     */
    private String determineStatus(boolean approveBooking, boolean applyBypass) {
        if (!approveBooking) {
            return "approved";
        }
        String status = "pending";
        if (applyBypass && currentUserProvider.hasRole("role_booking_approver")) {
            status = "approved"; // Bypass: approver can force-approve at creation time
        }
        return status;
    }

    /**
     * Validates that a location exists and is not soft-deleted (for new bookings only).
     *
     * Named decision: a NEW booking must not reference an already-deleted location.
     * Unlike the read-path's last-known-values behavior (Phase 4), a create/update must
     * reference an active location. Empty = never existed → 404. Populated but deletedAt != null
     * → also 404 (soft-deleted).
     */
    private void validateLocationForCreate(UUID locationId) {
        if (locationId == null) {
            return; // null location not explicitly rejected here (nullable by record definition)
        }
        locationsResourcesClient.getLocation(locationId).ifPresentOrElse(
                loc -> {
                    if (loc.deletedAt() != null) {
                        throw new BookingLocationNotFoundException(locationId.toString());
                    }
                },
                () -> { throw new BookingLocationNotFoundException(locationId.toString()); }
        );
    }

    /**
     * Validates each resource and returns the list of unique resource IDs.
     *
     * Named decision (is_unique filtering): caller pre-filters to only is_unique=true resources
     * before passing to ConflictDetectionService (plan 05-02's named decision).
     *
     * @throws BookingResourceNotFoundException if any resource does not exist or is soft-deleted
     */
    private List<UUID> resolveAndValidateResources(List<UUID> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return List.of();
        }

        List<UUID> uniqueIds = new ArrayList<>();
        for (UUID resourceId : resourceIds) {
            ResourceResponse resource = locationsResourcesClient.getResource(resourceId)
                    .orElseThrow(() -> new BookingResourceNotFoundException(resourceId.toString()));

            if (resource.deletedAt() != null) {
                throw new BookingResourceNotFoundException(resourceId.toString());
            }

            if (resource.isUnique()) {
                uniqueIds.add(resourceId);
            }
        }
        return uniqueIds;
    }

    /**
     * Validates that all submitted custom_field_values[].field_id are applicable to the context.
     *
     * Named decision (real enforcement, plan 05-02 OPTION A + plan 04-04 carve-out):
     * CustomFieldClient now returns real field definitions under normal operation.
     * An inapplicable field_id throws BookingCustomFieldNotApplicableException (400).
     * Empty list from a genuine connectivity failure → skip validation (narrow degraded mode).
     *
     * @param locationId    The booking's location (context id for field applicability lookup)
     * @param customFieldValues The submitted custom field values (may be null/empty)
     */
    private void validateCustomFieldValues(UUID locationId, List<CustomFieldValueInput> customFieldValues) {
        if (customFieldValues == null || customFieldValues.isEmpty()) {
            return;
        }
        if (locationId == null) {
            return; // can't validate without a context
        }

        List<CustomFieldDefinition> applicableFields = customFieldClient.getApplicableFields(locationId);

        if (applicableFields.isEmpty()) {
            // Narrow degraded-mode fallback: connectivity outage or rare 403
            // Log was already written by CustomFieldClient; skip validation for this request
            log.warn("Custom field applicability check skipped (empty response from CustomFieldClient) " +
                     "for location {}. Proceeding without validation.", locationId);
            return;
        }

        Set<UUID> applicableFieldIds = applicableFields.stream()
                .map(CustomFieldDefinition::id)
                .collect(Collectors.toSet());

        for (CustomFieldValueInput cfv : customFieldValues) {
            if (!applicableFieldIds.contains(cfv.fieldId())) {
                throw new BookingCustomFieldNotApplicableException(cfv.fieldId().toString());
            }
        }
    }

    /**
     * Ownership check for update/delete operations.
     *
     * TechArch §4.2's "(owner) or admin":
     * - role_booking_approver → allowed unconditionally (admin-equivalent)
     * - role_booking_creator AND booking.ownerId == calling user → allowed
     * - Otherwise → BookingForbiddenException (403)
     */
    private void checkOwnership(Booking booking) {
        if (currentUserProvider.hasRole("role_booking_approver")) {
            return; // Admin-equivalent override
        }
        if (currentUserProvider.hasRole("role_booking_creator")
                && booking.getOwnerId().equals(currentUserProvider.getCurrentUserId())) {
            return; // Owner allowed
        }
        throw new BookingForbiddenException();
    }

    /**
     * Persists a Booking entity with its associated BookingResource and BookingCustomFieldValue rows.
     * Runs as part of the caller's existing transaction.
     */
    private Booking persistBookingWithResources(
            String title, UUID locationId, Instant startTime, Instant endTime,
            List<UUID> resourceIds, List<CustomFieldValueInput> customFieldValues,
            String status, UUID seriesId,
            Boolean allDay, String description, String layoutStyle,
            String contactName, String contactEmail, String contactNo) {

        Booking booking = new Booking();
        booking.setTitle(title);
        booking.setLocationId(locationId);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(status);
        booking.setOwnerId(currentUserProvider.getCurrentUserId());
        booking.setSeriesId(seriesId);
        booking.setAllDay(allDay != null && allDay);
        booking.setDescription(description);
        booking.setLayoutStyle(layoutStyle);
        booking.setContactName(contactName);
        booking.setContactEmail(contactEmail);
        booking.setContactNo(contactNo);

        Booking saved = bookingRepository.save(booking);

        // Persist resource associations
        if (resourceIds != null) {
            for (UUID resourceId : resourceIds) {
                bookingResourceRepository.save(new BookingResource(saved.getId(), resourceId));
            }
        }

        // Persist custom field values
        if (customFieldValues != null) {
            for (CustomFieldValueInput cfv : customFieldValues) {
                bookingCustomFieldValueRepository.save(
                        new BookingCustomFieldValue(saved.getId(), cfv.fieldId(), cfv.value()));
            }
        }

        return saved;
    }

    /**
     * Creates the recurring series occurrences after the first one.
     *
     * Named decision (legacy parity — bypass applied only to occurrence #1):
     * F0 findings/01-booking-core.md confirmed legacy only applies the bypass once.
     * Subsequent occurrences use the plain approveBooking flag without bypass re-application.
     */
    private List<BookingResponse> createRecurringSeries(
            BookingResponse firstResponse, Booking firstBooking, BookingCreateRequest request,
            Instant baseStart, Instant baseEnd, boolean approveBooking,
            UUID seriesId, List<UUID> baseUniqueResourceIds) {

        List<BookingResponse> results = new ArrayList<>();
        results.add(firstResponse);

        List<Occurrence> occurrences = recurrenceExpander.expand(
                baseStart, baseEnd, request.recurrence());

        for (Occurrence occ : occurrences) {
            // Full re-validation per the plan's spec
            Instant occStart = occ.start();
            Instant occEnd = occ.end();

            // Re-validate resources (same set, just check uniqueness)
            List<UUID> uniqueIds = resolveAndValidateResources(request.resourceIds());

            // Re-run conflict check for this occurrence
            ConflictCheckResult occConflict = conflictDetectionService.checkConflicts(
                    request.locationId(), uniqueIds, occStart, occEnd, null);

            // Apply conflict policy (same shared method)
            applyConflictPolicy(occConflict);

            // Status: plain approveBooking flag, NO bypass re-application (legacy parity)
            String occStatus = determineStatus(approveBooking, false /* NO bypass for #2..N */);

            // Validate custom fields (same validation for each occurrence)
            validateCustomFieldValues(request.locationId(), request.customFieldValues());

            // Persist occurrence
            Booking occBooking = persistBookingWithResources(
                    request.title(), request.locationId(), occStart, occEnd,
                    request.resourceIds(), request.customFieldValues(),
                    occStatus, seriesId,
                    request.allDay(), request.description(), request.layoutStyle(),
                    request.contactName(), request.contactEmail(), request.contactNo());

            // Publish outbox event (booking.created only — named decision, F0 point 4 RESOLVED)
            publishOutboxEvent(occBooking, "booking.created",
                    buildCreatePayload(occBooking, request.emailContact()));

            results.add(toResponse(occBooking, occConflict.conflicts()));
        }

        return results;
    }

    /**
     * Applies partial update fields from the request to the booking entity.
     */
    private void applyFieldUpdates(Booking booking, BookingUpdateRequest request,
                                   Instant startTime, Instant endTime, UUID locationId) {
        if (request.title() != null) booking.setTitle(request.title());
        booking.setLocationId(locationId);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        if (request.allDay() != null) booking.setAllDay(request.allDay());
        if (request.description() != null) booking.setDescription(request.description());
        if (request.layoutStyle() != null) booking.setLayoutStyle(request.layoutStyle());
        if (request.contactName() != null) booking.setContactName(request.contactName());
        if (request.contactEmail() != null) booking.setContactEmail(request.contactEmail());
        if (request.contactNo() != null) booking.setContactNo(request.contactNo());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // OUTBOX EVENT PUBLISHING
    // ═══════════════════════════════════════════════════════════════════════════

    private void publishOutboxEvent(Booking booking, String routingKey, String payload) {
        OutboxEvent event = new OutboxEvent(
                "booking",
                booking.getId(),
                "booking.events",
                routingKey,
                payload);
        outboxEventRepository.save(event);
    }

    private String buildCreatePayload(Booking booking, Boolean emailContact) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", booking.getId());
            payload.put("title", booking.getTitle());
            payload.put("location_id", booking.getLocationId());
            payload.put("start_time", booking.getStartTime());
            payload.put("end_time", booking.getEndTime());
            payload.put("status", booking.getStatus());
            payload.put("owner_id", booking.getOwnerId());
            payload.put("series_id", booking.getSeriesId());
            // emailContact is TRANSIENT — never persisted, carried only in this event payload
            payload.put("email_contact", emailContact != null && emailContact);
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize booking.created payload for booking {}", booking.getId(), e);
            return "{}";
        }
    }

    private String buildUpdatePayload(Booking booking) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", booking.getId());
            payload.put("title", booking.getTitle());
            payload.put("location_id", booking.getLocationId());
            payload.put("start_time", booking.getStartTime());
            payload.put("end_time", booking.getEndTime());
            payload.put("status", booking.getStatus());
            payload.put("owner_id", booking.getOwnerId());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize booking.updated payload for booking {}", booking.getId(), e);
            return "{}";
        }
    }

    private String buildDeletePayload(Booking booking) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", booking.getId());
            payload.put("deleted_at", booking.getDeletedAt());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize booking.deleted payload for booking {}", booking.getId(), e);
            return "{}";
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RESPONSE MAPPING
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Maps a Booking entity to a BookingResponse DTO.
     * Resource IDs and custom field values are loaded from the repository.
     * conflict_flags are populated from the caller's conflict check result.
     */
    private BookingResponse toResponse(Booking booking, List<ConflictFlag> conflictFlags) {
        List<UUID> resourceIds = bookingResourceRepository.findByBookingId(booking.getId())
                .stream().map(BookingResource::getResourceId).collect(Collectors.toList());

        List<BookingCustomFieldValue> cfvs = bookingCustomFieldValueRepository.findByBookingId(booking.getId());
        List<CustomFieldValueOutput> cfvOutputs = cfvs.stream()
                .map(cfv -> new CustomFieldValueOutput(cfv.getCustomFieldId(), cfv.getValue(), null, null))
                .collect(Collectors.toList());

        return new BookingResponse(
                booking.getId(),
                booking.getSeriesId(),
                booking.getTitle(),
                booking.getLocationId(),
                booking.getStartTime(),
                booking.getEndTime(),
                resourceIds,
                cfvOutputs,
                booking.getStatus(),
                booking.getOwnerId(),
                booking.getApprovedBy(),
                booking.getApprovedAt(),
                booking.getDeniedBy(),
                booking.getDeniedAt(),
                booking.getDenialReason(),
                booking.isAllDay(),
                booking.getDescription(),
                booking.getLayoutStyle(),
                booking.getContactName(),
                booking.getContactEmail(),
                booking.getContactNo(),
                conflictFlags != null ? conflictFlags : List.of(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                booking.getDeletedAt()
        );
    }
}
