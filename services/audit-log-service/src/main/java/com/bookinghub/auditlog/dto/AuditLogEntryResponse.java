package com.bookinghub.auditlog.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Data transfer object for audit log entries returned by the GET /audit-log endpoint.
 *
 * Maps from AuditLogEntry domain entity to the API response shape.
 */
public record AuditLogEntryResponse(
    UUID id,
    UUID actorId,
    Instant occurredAt,
    String entityType,
    UUID entityId,
    String actionType,
    String beforeValues,
    String afterValues
) {}
