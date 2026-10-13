package com.bookinghub.auditlog.controller;

import com.bookinghub.auditlog.domain.AuditLogEntry;
import com.bookinghub.auditlog.dto.AuditLogEntryResponse;
import com.bookinghub.auditlog.error.AuditLogForbiddenException;
import com.bookinghub.auditlog.error.AuditLogInvalidDateRangeException;
import com.bookinghub.auditlog.repository.AuditLogEntryRepository;
import com.bookinghub.auditlog.repository.AuditLogSpecifications;
import com.bookinghub.auditlog.security.CurrentUserProvider;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the admin-facing audit log endpoint.
 *
 * Exposes GET /audit-log with optional filters:
 * - entityType: filter by entity type (e.g., "booking", "location")
 * - entityId: filter by specific entity ID
 * - actor: filter by actor ID
 * - dateFrom / dateTo: filter by occurred_at date range (inclusive)
 *
 * <p>Role-based access control:
 * - Requires authenticated user with role_audit_viewer
 * - Manual hasRole() check (not @PreAuthorize) to produce FRD-specific AUDIT_LOG_FORBIDDEN code
 *   instead of the generic AUTH_FORBIDDEN — identical pattern to Phase 3 PermissionController
 * - SecurityConfig also enforces role_audit_viewer at the filter-chain level as defense-in-depth
 *   (this manual check ensures the right error code when both layers trigger)
 *
 * <p>Intentionally exposes GET only. No PUT, DELETE, or POST mappings — the immutability
 * guarantee is both at the DB grant level (plan 06-03) and the API surface level (this controller).
 */
@RestController
@RequestMapping("/audit-log")
public class AuditLogController {

    private final AuditLogEntryRepository auditLogEntryRepository;
    private final CurrentUserProvider currentUserProvider;

    public AuditLogController(
            AuditLogEntryRepository auditLogEntryRepository,
            CurrentUserProvider currentUserProvider) {
        this.auditLogEntryRepository = auditLogEntryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Lists audit log entries with optional filters.
     *
     * @param entityType filter by entity type (e.g., "booking"), optional
     * @param entityId filter by entity ID, optional
     * @param actor filter by actor ID, optional
     * @param dateFrom filter by start date (inclusive), optional
     * @param dateTo filter by end date (inclusive), optional
     * @return list of audit log entries matching all filters
     * @throws AuditLogForbiddenException if caller lacks role_audit_viewer
     * @throws AuditLogInvalidDateRangeException if dateFrom is after dateTo
     */
    @GetMapping
    public List<AuditLogEntryResponse> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) UUID actor,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateTo) {

        // Step 1: Check role_audit_viewer privilege (manual check for FRD-specific error code)
        if (!currentUserProvider.hasRole("role_audit_viewer")) {
            throw new AuditLogForbiddenException("caller lacks role_audit_viewer");
        }

        // Step 2: Validate date range if both bounds are provided
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new AuditLogInvalidDateRangeException("date_from must be before date_to");
        }

        // Step 3: Build Specification with optional filters
        Instant fromInstant = dateFrom == null ? null : dateFrom.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = dateTo == null ? null : dateTo.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusSeconds(1);

        Specification<AuditLogEntry> spec = Specification
                .where(AuditLogSpecifications.entityType(entityType))
                .and(AuditLogSpecifications.entityId(entityId))
                .and(AuditLogSpecifications.actor(actor))
                .and(AuditLogSpecifications.occurredBetween(fromInstant, toInstant));

        // Step 4: Query and map to response DTOs
        return auditLogEntryRepository.findAll(spec)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ==================== Helper Methods ====================

    /**
     * Maps an AuditLogEntry entity to its response DTO.
     */
    private AuditLogEntryResponse toResponse(AuditLogEntry entry) {
        return new AuditLogEntryResponse(
                entry.getId(),
                entry.getActorId(),
                entry.getOccurredAt(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getActionType(),
                entry.getBeforeValues(),
                entry.getAfterValues()
        );
    }
}
