package com.bookinghub.auditlog.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Plain unit test for DomainEventParser — no Spring context needed.
 *
 * Covers:
 * 1. Happy-path entity_type/action_type split + actor/timestamp from action-specific fields
 * 2. Full-fallback degradation: no actor/timestamp → NIL_UUID + Instant.now()
 * 3. Multi-segment-looking key ("template.updated") + generic actor/timestamp fallbacks
 * 4. Completely empty body → fully-populated result with nil-UUID placeholders, never throws
 *
 * Per plan 06-04 Task 1 specification.
 */
class DomainEventParserTest {

    private DomainEventParser parser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        parser = new DomainEventParser();
        objectMapper = new ObjectMapper();
    }

    /**
     * Scenario 1: booking.approved — action-specific actor ("approved_by") and timestamp ("approved_at").
     * Verifies entity_type/action_type split, and approved-specific fallback chains.
     */
    @Test
    @DisplayName("parse(booking.approved, {id, approved_by, approved_at}) → correct entity/action/id/actor/timestamp")
    void parse_bookingApproved_correctFieldExtraction() {
        UUID entityId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        String approvedAt = "2026-01-01T00:00:00Z";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("id", entityId.toString());
        body.put("approved_by", actorId.toString());
        body.put("approved_at", approvedAt);

        InboundDomainEvent result = parser.parse("booking.approved", body);

        assertThat(result.entityType()).isEqualTo("booking");
        assertThat(result.actionType()).isEqualTo("approved");
        assertThat(result.entityId()).isEqualTo(entityId);
        assertThat(result.actorId()).isEqualTo(actorId);
        assertThat(result.occurredAt()).isEqualTo(Instant.parse(approvedAt));
        assertThat(result.rawBody()).isEqualTo(body);
    }

    /**
     * Scenario 2: role.assigned — body has "id" but NO actor or timestamp fields at all.
     * Verifies full fallback degradation: actorId → NIL_UUID, occurredAt → close to Instant.now().
     * Proves the fallback chain degrades gracefully, never throws.
     */
    @Test
    @DisplayName("parse(role.assigned, {id only}) → entityType=role, actionType=assigned, actorId=NIL_UUID, occurredAt≈now")
    void parse_roleAssigned_fullFallback_neverThrows() {
        UUID entityId = UUID.randomUUID();

        ObjectNode body = objectMapper.createObjectNode();
        body.put("id", entityId.toString());
        // Deliberately no actor_id, performed_by, owner_id, updated_by, created_by
        // Deliberately no occurred_at, timestamp, assigned_at

        Instant before = Instant.now().minusSeconds(2);
        InboundDomainEvent result = parser.parse("role.assigned", body);
        Instant after = Instant.now().plusSeconds(2);

        assertThat(result.entityType()).isEqualTo("role");
        assertThat(result.actionType()).isEqualTo("assigned");
        assertThat(result.entityId()).isEqualTo(entityId);
        assertThat(result.actorId()).isEqualTo(DomainEventParser.NIL_UUID);
        // occurredAt falls back to Instant.now() — must be within a small window of test execution
        assertThat(result.occurredAt()).isBetween(before, after);
    }

    /**
     * Scenario 3: template.updated — routing key split on LAST dot (not first),
     * actor via generic "updated_by" fallback, timestamp via "updated_at" action-specific fallback.
     */
    @Test
    @DisplayName("parse(template.updated, {id, updated_by, updated_at}) → entity_type=template, action_type=updated")
    void parse_templateUpdated_genericActorTimestampFallback() {
        UUID entityId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        String updatedAt = "2026-06-15T12:00:00Z";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("id", entityId.toString());
        body.put("updated_by", actorId.toString());   // generic actor field
        body.put("updated_at", updatedAt);            // action-specific timestamp

        InboundDomainEvent result = parser.parse("template.updated", body);

        assertThat(result.entityType()).isEqualTo("template");
        assertThat(result.actionType()).isEqualTo("updated");
        assertThat(result.entityId()).isEqualTo(entityId);
        assertThat(result.actorId()).isEqualTo(actorId);
        assertThat(result.occurredAt()).isEqualTo(Instant.parse(updatedAt));
    }

    /**
     * Scenario 4: customfield.created — completely empty body.
     * The parser must return a fully-populated InboundDomainEvent with:
     *   entityId = NIL_UUID, actorId = NIL_UUID, occurredAt ≈ now()
     * and NEVER throw any exception regardless of how sparse the input body is.
     */
    @Test
    @DisplayName("parse(customfield.created, {}) → fully-populated result with nil-UUIDs, never throws")
    void parse_completelyEmptyBody_neverThrows_nilPlaceholders() {
        ObjectNode body = objectMapper.createObjectNode(); // completely empty

        Instant before = Instant.now().minusSeconds(2);
        InboundDomainEvent result = parser.parse("customfield.created", body);
        Instant after = Instant.now().plusSeconds(2);

        assertThat(result.entityType()).isEqualTo("customfield");
        assertThat(result.actionType()).isEqualTo("created");
        assertThat(result.entityId()).isEqualTo(DomainEventParser.NIL_UUID);
        assertThat(result.actorId()).isEqualTo(DomainEventParser.NIL_UUID);
        assertThat(result.occurredAt()).isBetween(before, after);
        assertThat(result.rawBody()).isEqualTo(body);
    }

    /**
     * Bonus scenario: denied action — "denied_by" actor fallback.
     */
    @Test
    @DisplayName("parse(booking.denied, {id, denied_by, denied_at}) → denied_by actor, denied_at timestamp")
    void parse_bookingDenied_deniedByActorField() {
        UUID entityId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        String deniedAt = "2026-03-20T09:00:00Z";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("id", entityId.toString());
        body.put("denied_by", actorId.toString());
        body.put("denied_at", deniedAt);

        InboundDomainEvent result = parser.parse("booking.denied", body);

        assertThat(result.entityType()).isEqualTo("booking");
        assertThat(result.actionType()).isEqualTo("denied");
        assertThat(result.entityId()).isEqualTo(entityId);
        assertThat(result.actorId()).isEqualTo(actorId);
        assertThat(result.occurredAt()).isEqualTo(Instant.parse(deniedAt));
    }
}
