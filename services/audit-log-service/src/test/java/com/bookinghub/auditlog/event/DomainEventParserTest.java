package com.bookinghub.auditlog.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain unit test (no Spring context) for {@link DomainEventParser}.
 *
 * Proves the two guarantees this plan's parser rests on:
 * 1. The routing key alone correctly derives entity_type/action_type for every shape of
 *    key bound to this service's 7 queues.
 * 2. The entity_id/actor_id/occurred_at fallback chains degrade gracefully all the way to
 *    documented placeholders — the parser NEVER throws, no matter how sparse the body
 *    (threat T-06-04-01: the broker→consumer boundary is the widest fan-in in this
 *    architecture, and every upstream publisher is implicitly trusted input).
 */
class DomainEventParserTest {

    private final DomainEventParser parser = new DomainEventParser();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("booking.approved with full payload resolves every field from the payload")
    void bookingApproved_fullPayload_resolvesAllFields() throws Exception {
        UUID bookingId = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        Instant approvedAt = Instant.parse("2026-01-01T00:00:00Z");

        JsonNode body = json("""
                {
                  "id": "%s",
                  "approved_by": "%s",
                  "approved_at": "2026-01-01T00:00:00Z",
                  "title": "Quarterly planning"
                }
                """.formatted(bookingId, approverId));

        InboundDomainEvent event = parser.parse("booking.approved", body);

        assertThat(event.entityType()).isEqualTo("booking");
        assertThat(event.actionType()).isEqualTo("approved");
        assertThat(event.entityId()).isEqualTo(bookingId);
        // approved_by resolved via the action-specific branch (no actor_id/performed_by present)
        assertThat(event.actorId()).isEqualTo(approverId);
        // approved_at resolved via the {action}_at branch (no occurred_at/timestamp present)
        assertThat(event.occurredAt()).isEqualTo(approvedAt);
        assertThat(event.rawBody()).isSameAs(body);
    }

    @Test
    @DisplayName("role.assigned with id only degrades to nil actor and receipt-time occurred_at")
    void roleAssigned_idOnly_fallsBackToPlaceholders() throws Exception {
        UUID roleAssignmentId = UUID.randomUUID();
        Instant before = Instant.now().minus(5, ChronoUnit.SECONDS);

        JsonNode body = json("""
                { "id": "%s" }
                """.formatted(roleAssignmentId));

        InboundDomainEvent event = parser.parse("role.assigned", body);

        assertThat(event.entityType()).isEqualTo("role");
        assertThat(event.actionType()).isEqualTo("assigned");
        assertThat(event.entityId()).isEqualTo(roleAssignmentId);
        // No actor field of any kind → documented system/unknown-actor placeholder, not an exception
        assertThat(event.actorId()).isEqualTo(DomainEventParser.NIL_UUID);
        // No timestamp field of any kind → receipt time
        assertThat(event.occurredAt()).isAfterOrEqualTo(before);
        assertThat(event.occurredAt()).isBeforeOrEqualTo(Instant.now().plus(5, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("template.updated resolves actor/timestamp via the generic (non-approved/denied) branch")
    void templateUpdated_resolvesViaGenericFallbackBranch() throws Exception {
        UUID templateId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();

        JsonNode body = json("""
                {
                  "id": "%s",
                  "updated_by": "%s",
                  "updated_at": "2026-03-15T09:30:00Z",
                  "name": "Standard event fields"
                }
                """.formatted(templateId, editorId));

        InboundDomainEvent event = parser.parse("template.updated", body);

        // template.* is bound to audit.customfield.q — the entity_type still comes from the
        // routing key itself, NOT from the queue it arrived on. That is exactly why this
        // parser needs no per-queue special-casing.
        assertThat(event.entityType()).isEqualTo("template");
        assertThat(event.actionType()).isEqualTo("updated");
        assertThat(event.entityId()).isEqualTo(templateId);
        // updated_by resolved via the generic branch's owner_id → updated_by → created_by chain
        assertThat(event.actorId()).isEqualTo(editorId);
        assertThat(event.occurredAt()).isEqualTo(Instant.parse("2026-03-15T09:30:00Z"));
    }

    @Test
    @DisplayName("customfield.created with a completely empty body still returns a full event, never throws")
    void customfieldCreated_emptyBody_neverThrows() throws Exception {
        Instant before = Instant.now().minus(5, ChronoUnit.SECONDS);

        JsonNode body = json("{}");

        InboundDomainEvent event = parser.parse("customfield.created", body);

        assertThat(event.entityType()).isEqualTo("customfield");
        assertThat(event.actionType()).isEqualTo("created");
        assertThat(event.entityId()).isEqualTo(DomainEventParser.NIL_UUID);
        assertThat(event.actorId()).isEqualTo(DomainEventParser.NIL_UUID);
        assertThat(event.occurredAt()).isAfterOrEqualTo(before);
        assertThat(event.rawBody()).isNotNull();
    }

    @Test
    @DisplayName("occurred_at/actor_id take precedence over the action-specific fallbacks")
    void genericFieldsTakePrecedenceOverActionSpecificOnes() throws Exception {
        UUID entityId = UUID.randomUUID();
        UUID genericActor = UUID.randomUUID();
        UUID actionSpecificActor = UUID.randomUUID();

        JsonNode body = json("""
                {
                  "id": "%s",
                  "actor_id": "%s",
                  "approved_by": "%s",
                  "occurred_at": "2026-05-05T12:00:00Z",
                  "approved_at": "2026-01-01T00:00:00Z"
                }
                """.formatted(entityId, genericActor, actionSpecificActor));

        InboundDomainEvent event = parser.parse("booking.approved", body);

        assertThat(event.actorId()).isEqualTo(genericActor);
        assertThat(event.occurredAt()).isEqualTo(Instant.parse("2026-05-05T12:00:00Z"));
    }

    @Test
    @DisplayName("unparseable UUID/timestamp values are treated as absent, not as a failure")
    void malformedValuesDegradeToPlaceholders() throws Exception {
        JsonNode body = json("""
                {
                  "id": "not-a-uuid",
                  "actor_id": "also-not-a-uuid",
                  "occurred_at": "yesterday"
                }
                """);

        InboundDomainEvent event = parser.parse("settings.updated", body);

        assertThat(event.entityType()).isEqualTo("settings");
        assertThat(event.actionType()).isEqualTo("updated");
        assertThat(event.entityId()).isEqualTo(DomainEventParser.NIL_UUID);
        assertThat(event.actorId()).isEqualTo(DomainEventParser.NIL_UUID);
        assertThat(event.occurredAt()).isNotNull();
    }

    private JsonNode json(String raw) throws Exception {
        return objectMapper.readTree(raw);
    }
}
