package com.bookinghub.auditlog.event;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DomainEventParser.
 * Tests the routing-key-derived entity_type/action_type extraction and the
 * documented fallback chains for entity_id/actor_id/occurred_at.
 */
class DomainEventParserTest {

    private static final UUID NIL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private DomainEventParser parser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        parser = new DomainEventParser();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testParseBookingApprovedWithAllFields() throws Exception {
        // Scenario 1: parse("booking.approved", {...}) with all fields present
        String routingKey = "booking.approved";
        UUID entityId = UUID.randomUUID();
        UUID approvedBy = UUID.randomUUID();
        String timestamp = "2026-01-01T00:00:00Z";

        JsonNode body = objectMapper.readTree(String.format(
            """
            {
              "id": "%s",
              "approved_by": "%s",
              "approved_at": "%s"
            }
            """,
            entityId, approvedBy, timestamp
        ));

        InboundDomainEvent result = parser.parse(routingKey, body);

        assertEquals("booking", result.entityType());
        assertEquals("approved", result.actionType());
        assertEquals(entityId, result.entityId());
        assertEquals(approvedBy, result.actorId());
        assertEquals(Instant.parse(timestamp), result.occurredAt());
    }

    @Test
    void testParseRoleAssignedWithMinimalFields() throws Exception {
        // Scenario 2: parse("role.assigned", {"id": "<uuid>"}) - no actor/timestamp fields at all
        String routingKey = "role.assigned";
        UUID entityId = UUID.randomUUID();
        Instant beforeCall = Instant.now();

        JsonNode body = objectMapper.readTree(String.format(
            """
            {
              "id": "%s"
            }
            """,
            entityId
        ));

        InboundDomainEvent result = parser.parse(routingKey, body);
        Instant afterCall = Instant.now();

        assertEquals("role", result.entityType());
        assertEquals("assigned", result.actionType());
        assertEquals(entityId, result.entityId());
        assertEquals(NIL_UUID, result.actorId()); // Falls back to nil-UUID
        assertTrue(result.occurredAt().isAfter(beforeCall) && result.occurredAt().isBefore(afterCall.plusSeconds(1)),
            "occurred_at should be close to now()");
    }

    @Test
    void testParseTemplateUpdatedWithUpdateFields() throws Exception {
        // Scenario 3: parse("template.updated", {...}) with updated_by and updated_at
        // Verifies multi-segment routing key is split on LAST dot correctly
        String routingKey = "customfield.updated";
        UUID entityId = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        String timestamp = "2026-02-15T10:30:00Z";

        JsonNode body = objectMapper.readTree(String.format(
            """
            {
              "id": "%s",
              "updated_by": "%s",
              "updated_at": "%s"
            }
            """,
            entityId, updatedBy, timestamp
        ));

        InboundDomainEvent result = parser.parse(routingKey, body);

        assertEquals("customfield", result.entityType());
        assertEquals("updated", result.actionType());
        assertEquals(entityId, result.entityId());
        assertEquals(updatedBy, result.actorId());
        assertEquals(Instant.parse(timestamp), result.occurredAt());
    }

    @Test
    void testParseCompletelyEmptyBody() throws Exception {
        // Scenario 4: parse("customfield.created", {}) - completely empty body
        // Proves graceful degradation, never throws
        String routingKey = "customfield.created";
        Instant beforeCall = Instant.now();

        JsonNode body = objectMapper.readTree("{}");

        InboundDomainEvent result = parser.parse(routingKey, body);
        Instant afterCall = Instant.now();

        assertEquals("customfield", result.entityType());
        assertEquals("created", result.actionType());
        assertEquals(NIL_UUID, result.entityId()); // Falls back to nil-UUID
        assertEquals(NIL_UUID, result.actorId()); // Falls back to nil-UUID
        assertTrue(result.occurredAt().isAfter(beforeCall) && result.occurredAt().isBefore(afterCall.plusSeconds(1)),
            "occurred_at should be close to now()");
    }

    @Test
    void testParseDeniedActionWithDeniedByField() throws Exception {
        // Test that "denied_by" is correctly extracted for "denied" actions
        String routingKey = "booking.denied";
        UUID entityId = UUID.randomUUID();
        UUID deniedBy = UUID.randomUUID();
        String timestamp = "2026-01-05T14:22:00Z";

        JsonNode body = objectMapper.readTree(String.format(
            """
            {
              "id": "%s",
              "denied_by": "%s",
              "denied_at": "%s"
            }
            """,
            entityId, deniedBy, timestamp
        ));

        InboundDomainEvent result = parser.parse(routingKey, body);

        assertEquals("booking", result.entityType());
        assertEquals("denied", result.actionType());
        assertEquals(deniedBy, result.actorId()); // Should use "denied_by"
        assertEquals(Instant.parse(timestamp), result.occurredAt());
    }

    @Test
    void testParseWithPerformedByFallback() throws Exception {
        // Test that "performed_by" is used as fallback when "actor_id" is absent
        String routingKey = "location.created";
        UUID entityId = UUID.randomUUID();
        UUID performedBy = UUID.randomUUID();

        JsonNode body = objectMapper.readTree(String.format(
            """
            {
              "id": "%s",
              "performed_by": "%s",
              "created_at": "2026-01-10T08:00:00Z"
            }
            """,
            entityId, performedBy
        ));

        InboundDomainEvent result = parser.parse(routingKey, body);

        assertEquals(performedBy, result.actorId());
    }

    @Test
    void testParseWithOwnerIdFallback() throws Exception {
        // Test that "owner_id" is used as generic fallback for non-approved/denied actions
        String routingKey = "resource.created";
        UUID entityId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        JsonNode body = objectMapper.readTree(String.format(
            """
            {
              "id": "%s",
              "owner_id": "%s",
              "created_at": "2026-01-12T09:15:00Z"
            }
            """,
            entityId, ownerId
        ));

        InboundDomainEvent result = parser.parse(routingKey, body);

        assertEquals(ownerId, result.actorId());
    }

    @Test
    void testParseWithNullValues() throws Exception {
        // Test that null values in the body are handled gracefully
        String routingKey = "permission.assigned";
        UUID entityId = UUID.randomUUID();

        JsonNode body = objectMapper.readTree(String.format(
            """
            {
              "id": "%s",
              "actor_id": null,
              "occurred_at": null
            }
            """,
            entityId
        ));

        InboundDomainEvent result = parser.parse(routingKey, body);

        assertEquals(entityId, result.entityId());
        assertEquals(NIL_UUID, result.actorId());
        // occurred_at should fall back to now()
        assertTrue(result.occurredAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void testParseMultiSegmentRoutingKey() throws Exception {
        // Test that routing keys with multiple dots split on the LAST dot only
        // e.g., "booking.custom.field.updated" should split as entity="booking.custom.field", action="updated"
        String routingKey = "booking.custom.field.updated";
        UUID entityId = UUID.randomUUID();

        JsonNode body = objectMapper.readTree(String.format(
            """
            {
              "id": "%s",
              "created_at": "2026-01-15T12:00:00Z"
            }
            """,
            entityId
        ));

        InboundDomainEvent result = parser.parse(routingKey, body);

        assertEquals("booking.custom.field", result.entityType());
        assertEquals("updated", result.actionType());
    }

    @Test
    void testParseInvalidUuidInIdField() throws Exception {
        // Test that an invalid UUID in "id" field falls back to nil-UUID gracefully
        String routingKey = "location.updated";

        JsonNode body = objectMapper.readTree(
            """
            {
              "id": "not-a-valid-uuid",
              "created_at": "2026-01-15T12:00:00Z"
            }
            """
        );

        InboundDomainEvent result = parser.parse(routingKey, body);

        assertEquals("location", result.entityType());
        assertEquals("updated", result.actionType());
        assertEquals(NIL_UUID, result.entityId()); // Falls back to nil-UUID
    }

    @Test
    void testParseInvalidTimestampFormat() throws Exception {
        // Test that an invalid timestamp falls back to fallback chain gracefully
        String routingKey = "settings.updated";
        UUID entityId = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        Instant beforeCall = Instant.now();

        JsonNode body = objectMapper.readTree(String.format(
            """
            {
              "id": "%s",
              "updated_by": "%s",
              "updated_at": "not-a-valid-timestamp"
            }
            """,
            entityId, updatedBy
        ));

        InboundDomainEvent result = parser.parse(routingKey, body);
        Instant afterCall = Instant.now();

        assertEquals(entityId, result.entityId());
        assertEquals(updatedBy, result.actorId());
        // Should fall back to now() since the timestamp was invalid
        assertTrue(result.occurredAt().isAfter(beforeCall) && result.occurredAt().isBefore(afterCall.plusSeconds(1)));
    }
}
