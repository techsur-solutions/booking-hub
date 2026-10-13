# Test Stubs: Remaining Open Questions

Brief stubs for the remaining 10 open questions not covered by the detailed stub files.
Full context in `.planning/phases/01-legacy-functional-audit/open-questions.md`.

For each: implement the test in the Destination path, update open-questions.md, update traceability matrix.

---

## OQ-14: Location/Resource Deletion Policy (item #14)

**open-questions.md item:** #14  
**Traceability:** BF-036, BF-037 (deletion-related findings)  
**Destination:** `services/locations-resources-service/.../DeletionPolicyTest.java`

**Decision Required:**
- **Option A:** Block delete if referenced by existing bookings (returns 409 LOCATION_IN_USE / RESOURCE_IN_USE)
- **Option B:** Allow delete and permit orphaned references (legacy parity — hard delete, no guard)
- **Option C:** Soft-delete (set deletedAt, references remain intact)

```java
// Option A: Block deletion
@Test
void deleteLocation_withExistingBookings_returns409() throws Exception {
    // Seed: location with an existing booking
    mockMvc.perform(delete("/api/locations/{id}", locationWithBookingsId)
            .with(jwt().authorities(new SimpleGrantedAuthority("role_calendar_admin"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error_code").value("LOCATION_IN_USE"));
}

// Option B: Allow orphan (legacy parity)
@Test
void deleteLocation_withExistingBookings_succeeds_OptionB() throws Exception {
    mockMvc.perform(delete("/api/locations/{id}", locationWithBookingsId)
            .with(jwt().authorities(new SimpleGrantedAuthority("role_calendar_admin"))))
        .andExpect(status().isNoContent());
    // Bookings still exist but locationId is orphaned
    assertThat(bookingRepository.findByLocationId(locationWithBookingsId)).isNotEmpty();
}
```

---

## OQ-15: checkavailability Permission Gate (item #15)

**open-questions.md item:** #15  
**Traceability:** BF-040 (resource availability permission finding)  
**Destination:** `services/locations-resources-service/.../ResourceAvailabilityTest.java`

**Decision Required:**
- **Option A:** checkavailability accessible to any booking user (fix legacy admin-only quirk)
- **Option B:** checkavailability remains admin-only (legacy parity — confirmed security smell)

```java
// Option A: Any booking user can check availability
@Test
void checkAvailability_withBookingUserRole_returns200_OptionA() throws Exception {
    mockMvc.perform(get("/api/resources/{id}/availability", resourceId)
            .param("start", "2026-10-15T10:00:00")
            .param("end", "2026-10-15T11:00:00")
            .with(jwt().authorities(new SimpleGrantedAuthority("role_booking_user"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.available").isBoolean());
}

// Option B: Admin-only (legacy parity)
@Test
void checkAvailability_withBookingUserRole_returns403_OptionB() throws Exception {
    mockMvc.perform(get("/api/resources/{id}/availability", resourceId)
            .with(jwt().authorities(new SimpleGrantedAuthority("role_booking_user"))))
        .andExpect(status().isForbidden());
}
```

---

## OQ-17: Session Timeout Values (item #17)

**open-questions.md item:** #17  
**Decision:** Already implemented in Phase 3 (30 min idle / 10 hours max — see STATE.md decisions)  
**Status:** F0 Open Question #17 RESOLVED by Phase 3 implementation (see STATE.md). This stub is for test coverage.  
**Destination:** Keycloak realm config test or integration test

```java
// Verify Keycloak realm timeout configuration
@Test
void keycloakRealm_sessionIdleTimeout_is30Minutes() {
    // Verify realm configuration via Keycloak Admin API
    RealmRepresentation realm = keycloakAdminClient.realm("bookinghub").toRepresentation();
    assertThat(realm.getSsoSessionIdleTimeout()).isEqualTo(1800); // 30 min in seconds
}

@Test
void keycloakRealm_sessionMaxLifespan_is10Hours() {
    RealmRepresentation realm = keycloakAdminClient.realm("bookinghub").toRepresentation();
    assertThat(realm.getSsoSessionMaxLifespan()).isEqualTo(36000); // 10 hours in seconds
}
```

---

## OQ-18: Permission Change Cache Invalidation (item #18)

**open-questions.md item:** #18  
**Decision:** RESOLVED in Phase 3 — no in-memory cache, changes take effect on next JWT issue (see STATE.md)  
**Status:** F0 Open Question #18 RESOLVED by Phase 3 implementation. This stub is for test coverage.  
**Destination:** `services/users-permissions-service/.../PermissionCacheTest.java`

```java
// Verify no caching — permission change takes effect on next JWT issue (not restart)
@Test
void permissionChange_takesEffectOnNextTokenIssuance_noRestartRequired() throws Exception {
    // GIVEN: user has role_booking_user but NOT role_calendar_admin
    // WHEN: admin grants role_calendar_admin to user
    mockMvc.perform(put("/api/users/{id}/permissions", userId)
            .with(jwt().authorities(new SimpleGrantedAuthority("role_admin")))
            .content("""{"role": "role_calendar_admin", "grant": true}""")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // THEN: new token issued for user includes role_calendar_admin (no restart needed)
    // Simulate new JWT fetch and verify role inclusion
    String newToken = fetchNewTokenForUser(userId);
    assertThat(decodeRoles(newToken)).contains("role_calendar_admin");
}
```

---

## OQ-19: Password Reset Token Re-Check at Submit (item #19)

**open-questions.md item:** #19  
**Decision:** RESOLVED in Phase 3 — token re-validated at submit time (improvement over legacy gap — see STATE.md)  
**Status:** F0 Open Question #19 CLOSED by Phase 3 implementation. This stub is for test coverage.  
**Destination:** `services/users-permissions-service/.../PasswordResetTokenTest.java`

```java
@Test
void resetPassword_withExpiredToken_atSubmitTime_returns400() throws Exception {
    // GIVEN: token that was valid when form was loaded but has since expired
    String expiredToken = createTokenExpiredJustNow();

    // WHEN: user submits new password with this now-expired token
    mockMvc.perform(post("/api/auth/reset-password")
            .content("""{"token": "%s", "newPassword": "NewPass1!"}""".formatted(expiredToken))
            .contentType(MediaType.APPLICATION_JSON))
        // THEN: rejected (not accepted despite valid form-load token)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error_code").value("RESET_TOKEN_EXPIRED"));
}
```

---

## OQ-20: Email Enumeration Inconsistency (item #20)

**open-questions.md item:** #20  
**Decision:** RESOLVED in Phase 3 — identical 202 response regardless of email existence (improvement over legacy inconsistency — see STATE.md)  
**Status:** F0 Open Question #20 CLOSED by Phase 3 implementation. This stub is for test coverage.  
**Destination:** `services/users-permissions-service/.../PasswordResetSecurityTest.java`

```java
@Test
void passwordResetRequest_existingEmail_returnsIdenticalResponseAs_unknownEmail() throws Exception {
    // GIVEN: request for existing user email
    MvcResult existingResult = mockMvc.perform(post("/api/auth/reset-password-request")
            .content("""{"email": "known-user@example.com"}""")
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    // AND: request for non-existing email
    MvcResult unknownResult = mockMvc.perform(post("/api/auth/reset-password-request")
            .content("""{"email": "no-such-user@example.com"}""")
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    // THEN: both return the same status (202 Accepted) and body (no email enumeration)
    assertThat(existingResult.getResponse().getStatus()).isEqualTo(202);
    assertThat(unknownResult.getResponse().getStatus()).isEqualTo(202);
    assertThat(existingResult.getResponse().getContentAsString())
        .isEqualTo(unknownResult.getResponse().getContentAsString());
}
```

---

## OQ-21: Email Uniqueness Collation (item #21)

**open-questions.md item:** #21  
**Traceability:** BF-060 (email uniqueness finding)  
**Destination:** `services/users-permissions-service/.../UserEmailUniquenessTest.java`

**Decision Required:**
- **Option A:** Case-insensitive (user@example.com == USER@EXAMPLE.COM)
- **Option B:** Case-sensitive (different users)

```java
// Option A: Case-insensitive (recommended for email)
@Test
void createUser_withDuplicateEmailDifferentCase_returns409_OptionA() throws Exception {
    // Create first user with lowercase email
    createUser("user@example.com");

    // Attempt to create with uppercase — should conflict
    mockMvc.perform(post("/api/users")
            .with(adminAuth())
            .content("""{"email": "USER@EXAMPLE.COM", "name": "Another User"}""")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error_code").value("EMAIL_ALREADY_EXISTS"));
}
```

---

## OQ-22: Admin-Cannot-Assume-Admin Server Enforcement (item #22)

**open-questions.md item:** #22  
**Traceability:** BF-065 (assume-user security gap)  
**Destination:** `services/users-permissions-service/.../AssumeUserTest.java`

**Decision Required:**
- **Option A:** Server-side enforcement — reject assume-admin-by-admin API call (recommended improvement)
- **Option B:** UI-only hide (legacy parity — confirmed security gap)

```java
// Option A: Server-side enforcement (recommended)
@Test
void assumeUser_targetIsAdmin_returns403_OptionA() throws Exception {
    // GIVEN: requester has accessUsers permission (can assume non-admins)
    // BUT: target is also an admin
    mockMvc.perform(post("/api/users/{id}/assume", adminTargetId)
            .with(jwt().authorities(
                new SimpleGrantedAuthority("role_admin"),
                new SimpleGrantedAuthority("SCOPE_accessUsers"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error_code").value("CANNOT_ASSUME_ADMIN"));
}
```

---

## OQ-23: Event/Eventresource DB Column List (item #23)

**open-questions.md item:** #23  
**Note:** This is a schema completeness question, not a behavior decision. Relevant only if Phase 5 discovered schema gaps.  
**Action:** Review Phase 5 implementation and confirm bookings/eventresources tables are complete per legacy equivalence. Add test if a column was omitted.

```java
// Verify all expected booking fields are stored and retrievable
@Test
void createBooking_allLegacyFields_persistedAndReturned() throws Exception {
    String fullBookingJson = """
        {
          "title": "Test Meeting",
          "locationId": "...",
          "startTime": "2026-10-15T10:00:00",
          "endTime": "2026-10-15T11:00:00",
          "description": "Meeting description",
          "emailContact": true,
          "contactName": "John Doe",
          "contactEmail": "john@example.com",
          "resourceIds": [],
          "customFieldValues": []
        }
        """;
    // Verify all fields round-trip correctly
    mockMvc.perform(post("/api/bookings").content(fullBookingJson)...)
        .andExpect(jsonPath("$.title").value("Test Meeting"))
        .andExpect(jsonPath("$.contactName").value("John Doe"))
        // ... etc for all fields
}
```

---

## OQ-24: Error Email Feature (item #24)

**open-questions.md item:** #24  
**Note:** Low-priority — whether legacy had error-email sending is unclear. Confirm if new system should implement or explicitly skip.  
**Action:** Either document "not implemented by design" in error-handling docs, or add a test for the feature if it is implemented.

```java
// If implemented: verify error notification is sent to configured admin email
// If not implemented: document in application error-handling README

// Example if implemented:
@Test
void applicationError_sendsNotificationEmail_toConfiguredAdminAddress() {
    // Trigger a known error condition
    // Verify email was sent via mail service mock
}
```

---

## OQ-25: `ajax` Log Type Write-Site (item #25)

**open-questions.md item:** #25  
**Traceability:** BF-099 (audit log type finding)  
**Destination:** `services/audit-log-service/.../AuditLogTypeTest.java`  
**Note:** If `ajax` log type is not replicated in the new system, document explicitly.

```java
// If ajax-type logging IS implemented:
@Test
void ajaxEndpointCall_createsAuditLogEntry_withTypeAjax() {
    // Trigger an AJAX-style API call
    // Verify audit_log_entries contains a row with entity_type or action_type for ajax
}

// If ajax-type logging is NOT replicated (deliberate):
// Document in audit-log-service README: "ajax log type is not replicated — 
// legacy's ajax-type was incidental, new system uses structured entity/action routing-key audit"
```

---

*Copy relevant section when resolving, implement full test per the resolution protocol.*
*See `docs/testing/open-question-resolution-protocol.md` for the 6-step workflow.*
