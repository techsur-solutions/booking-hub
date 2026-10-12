package com.bookinghub.notifications.controller;

import com.bookinghub.notifications.domain.NotificationDelivery;
import com.bookinghub.notifications.dto.NotificationDtos;
import com.bookinghub.notifications.repository.NotificationDeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for NotificationStatusController.
 *
 * Tests the two admin-only endpoints:
 * - GET /notifications/delivery-status — with optional event_type/status filters
 * - GET /notifications/dead-letter — implicit status='dead_lettered'
 *
 * Also verifies role-based access control: role_audit_viewer required.
 */
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationStatusControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationDeliveryRepository notificationDeliveryRepository;

    private UUID testId1;
    private UUID testId2;
    private UUID testId3;

    @BeforeEach
    void setUp() {
        notificationDeliveryRepository.deleteAll();

        testId1 = UUID.randomUUID();
        testId2 = UUID.randomUUID();
        testId3 = UUID.randomUUID();

        // Create test deliveries with varying event_type and status
        NotificationDelivery sent = new NotificationDelivery();
        sent.setIdempotencyKey(UUID.randomUUID().toString());
        sent.setEventType("booking.created");
        sent.setEntityId(testId1);
        sent.setStatus("sent");
        sent.setAttemptCount(1);
        sent.setLastAttemptedAt(Instant.now());
        notificationDeliveryRepository.save(sent);

        NotificationDelivery deadLettered1 = new NotificationDelivery();
        deadLettered1.setIdempotencyKey(UUID.randomUUID().toString());
        deadLettered1.setEventType("booking.approved");
        deadLettered1.setEntityId(testId2);
        deadLettered1.setStatus("dead_lettered");
        deadLettered1.setAttemptCount(4);
        deadLettered1.setLastAttemptedAt(Instant.now());
        notificationDeliveryRepository.save(deadLettered1);

        NotificationDelivery deadLettered2 = new NotificationDelivery();
        deadLettered2.setIdempotencyKey(UUID.randomUUID().toString());
        deadLettered2.setEventType("password.reset.requested");
        deadLettered2.setEntityId(testId3);
        deadLettered2.setStatus("dead_lettered");
        deadLettered2.setAttemptCount(0);
        deadLettered2.setLastAttemptedAt(Instant.now());
        notificationDeliveryRepository.save(deadLettered2);
    }

    @Test
    @WithMockUser(authorities = "role_audit_viewer")
    void getDeliveryStatus_withoutFilters() throws Exception {
        mockMvc.perform(get("/notifications/delivery-status")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].event_type", hasItems("booking.created", "booking.approved", "password.reset.requested")))
            .andExpect(jsonPath("$[*].status", hasItems("sent", "dead_lettered", "dead_lettered")));
    }

    @Test
    @WithMockUser(authorities = "role_audit_viewer")
    void getDeliveryStatus_filterByStatus() throws Exception {
        mockMvc.perform(get("/notifications/delivery-status")
                .param("status", "dead_lettered")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].status", everyItem(equalTo("dead_lettered"))));
    }

    @Test
    @WithMockUser(authorities = "role_audit_viewer")
    void getDeliveryStatus_filterByEventType() throws Exception {
        mockMvc.perform(get("/notifications/delivery-status")
                .param("event_type", "booking.created")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].event_type", equalTo("booking.created")))
            .andExpect(jsonPath("$[0].status", equalTo("sent")));
    }

    @Test
    @WithMockUser(authorities = "role_audit_viewer")
    void getDeliveryStatus_filterByBoth() throws Exception {
        mockMvc.perform(get("/notifications/delivery-status")
                .param("event_type", "booking.approved")
                .param("status", "dead_lettered")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].event_type", equalTo("booking.approved")))
            .andExpect(jsonPath("$[0].status", equalTo("dead_lettered")));
    }

    @Test
    @WithMockUser(authorities = "role_audit_viewer")
    void getDeadLetter() throws Exception {
        mockMvc.perform(get("/notifications/dead-letter")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].status", everyItem(equalTo("dead_lettered"))))
            .andExpect(jsonPath("$[*].event_type", hasItems("booking.approved", "password.reset.requested")));
    }

    @Test
    @WithMockUser(authorities = "role_calendar_viewer")
    void getDeliveryStatus_forbiddenWithWrongRole() throws Exception {
        mockMvc.perform(get("/notifications/delivery-status")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void getDeliveryStatus_unauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/notifications/delivery-status")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "role_calendar_viewer")
    void getDeadLetter_forbiddenWithWrongRole() throws Exception {
        mockMvc.perform(get("/notifications/dead-letter")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void getDeadLetter_unauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/notifications/dead-letter")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }
}
