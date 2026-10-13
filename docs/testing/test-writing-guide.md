# Test Writing Guide: Legacy Parity Verification

**Audience:** Developers implementing features in Phases 4-7
**Purpose:** Write tests that verify legacy behavior equivalence per F0 audit findings

## Test Strategy by Layer

### Backend Integration Tests (Spring Boot)

**Pattern:** `@SpringBootTest` + `MockMvc` + WireMock for external services (Keycloak)

**File location:** `services/{service-name}/src/test/java/.../controller/*IntegrationTest.java`

**Template:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MyFeatureIntegrationTest {
    
    @org.junit.jupiter.api.extension.RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("keycloak.server-url", () -> wireMock.baseUrl());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MyRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        // Stub Keycloak responses
        wireMock.stubFor(post(urlPathMatching("/realms/.*/protocol/openid-connect/token"))
            .willReturn(aResponse().withStatus(200).withBody("{\"access_token\":\"test\"}")));
    }

    @Test
    void testFeatureBehavior() throws Exception {
        // GIVEN: setup test data
        // WHEN: perform request with JWT mock
        mockMvc.perform(post("/api/resource")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"field\":\"value\"}"))
            // THEN: assert response and side effects
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
        
        // Assert DB state
        assertThat(repository.findAll()).hasSize(1);
    }
}
```

**Key practices:**
- Use WireMock for Keycloak Admin API stubs (see `UserControllerIntegrationTest` example)
- Test against real Postgres from docker-compose (not Testcontainers — Docker API compatibility issue)
- Use `.with(jwt().authorities(...))` for authentication
- Verify both response contract AND database state
- Test happy path + one failure case per endpoint

### Frontend E2E Tests (Playwright)

**Pattern:** Full-browser testing with VITE_E2E_MODE bypass for Keycloak

**File location:** `frontend/e2e/{feature}.spec.ts`

**Template:**
```typescript
import { test, expect } from '@playwright/test';

test.describe('Feature Name', () => {
  test.beforeEach(async ({ page }) => {
    // E2E mode bypasses Keycloak auth
    await page.goto('/feature-path');
  });

  test('user can perform action', async ({ page }) => {
    // GIVEN: navigate to feature page
    await expect(page.locator('h1')).toHaveText('Feature Page');
    
    // WHEN: interact with UI
    await page.fill('input[name="field"]', 'value');
    await page.click('button[type="submit"]');
    
    // THEN: verify outcome
    await expect(page.locator('.success-message')).toBeVisible();
  });
});
```

**Key practices:**
- Tests run with `VITE_E2E_MODE=true` (bypasses Keycloak, see `playwright.config.ts`)
- Test navigation flow, form submission, data rendering
- Use accessibility-friendly selectors (role/label/text, not brittle classes)
- Keep tests focused (one user story per test file)

## Mapping F0 Findings to Tests

**Example:** BF-001 (Permission gates on Bookings controller)

```
Finding: "accesscalendar (all actions), allowRoomBooking (except index/list/day)"
Test coverage needed:
  1. Without accesscalendar → 403 on any booking route
  2. With accesscalendar but no allowRoomBooking → 403 on POST /api/bookings
  3. With both permissions → 201 on POST /api/bookings
```

**Test class:** `BookingControllerIntegrationTest.java`
```java
@Test
void createBooking_withoutAccessCalendar_returns403() {
    mockMvc.perform(post("/api/bookings")
            .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_user")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(validBookingJson))
        .andExpect(status().isForbidden());
}

@Test
void createBooking_withAccessCalendarButNoAllowRoomBooking_returns403() {
    mockMvc.perform(post("/api/bookings")
            .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_accessCalendar")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(validBookingJson))
        .andExpect(status().isForbidden());
}

@Test
void createBooking_withBothPermissions_returns201() {
    mockMvc.perform(post("/api/bookings")
            .with(jwt().authorities(
                new SimpleGrantedAuthority("SCOPE_accessCalendar"),
                new SimpleGrantedAuthority("SCOPE_allowRoomBooking")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(validBookingJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());
}
```

## High-Priority Edge Cases (F13.3)

### Conflict Detection (F2)

**Findings covered:** BF-012, BF-016, BF-017, BF-028, BF-112, BF-113

**Test scenarios:**
1. **Half-open interval semantics:** Booking A ends at 10:00, Booking B starts at 10:00 → NO conflict
2. **Location overlap:** Same location, overlapping times → conflict detected
3. **Resource overlap:** Different locations, shared resource, overlapping times → conflict detected
4. **Exclude own prior state:** Editing Booking A to new time that overlaps A's old time → NO self-conflict
5. **Exclude deleted/denied:** Active booking overlaps a denied booking → NO conflict (denied doesn't block)

**Test class:** `ConflictDetectionEngineTest.java`
```java
@Test
void halfOpenInterval_noConflict() {
    // Event A: 09:00-10:00
    // Event B: 10:00-11:00
    // Expected: no conflict (10:00 start == 10:00 end is boundary, not overlap)
    boolean conflict = conflictEngine.detectConflict(locationId, startB, endB, excludeBookingId);
    assertThat(conflict).isFalse();
}

@Test
void zeroDurationBooking_isAllowed() {
    // BF-020: end==start passes validation in legacy — new system must match
    // Creating a booking where start==end should not be rejected
    mockMvc.perform(post("/api/bookings")
            .with(validJwt())
            .content(bookingWithEqualStartEnd))
        .andExpect(status().isCreated());
}

@Test
void deniedBooking_doesNotCauseConflict() {
    // BF-016: denied bookings must not block new bookings in the same slot
    BookingEntity denied = createBookingWithStatus(location, slot, Status.DENIED);
    boolean conflict = conflictEngine.detectConflict(location.getId(), slot.start, slot.end, null);
    assertThat(conflict).isFalse();
}
```

### Approval Workflow (F3)

**Findings covered:** BF-006, BF-007, BF-009, BF-010, BF-022

**Test scenarios:**
1. **Auto-approve off:** New booking → `pending` status
2. **Auto-approve on:** New booking → `approved` status
3. **One-way approve:** `pending` → `approved` is allowed, `approved` → `pending` is rejected
4. **Permission gate:** User without `allowApproveBooking` cannot call `/api/bookings/{id}/approve`
5. **No re-validation on edit:** BF-009 — updating a booking does NOT change its approval status

```java
@Test
void createBooking_whenAutoApproveOff_returnsPendingStatus() {
    settingsService.setApproveBooking(true); // true = approval required
    mockMvc.perform(post("/api/bookings").with(validJwt()).content(validBooking))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("pending"));
}

@Test
void createBooking_whenAutoApproveOn_returnsApprovedStatus() {
    settingsService.setApproveBooking(false); // false = auto-approve
    mockMvc.perform(post("/api/bookings").with(validJwt()).content(validBooking))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("approved"));
}

@Test
void approveBooking_withoutAllowApproveBooking_returns403() {
    // BF-001, BF-006: allowApproveBooking required for approve/deny
    mockMvc.perform(post("/api/bookings/{id}/approve", bookingId)
            .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_accessCalendar"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("APPROVAL_FORBIDDEN"));
}
```

### Recurring Booking Scoping (F1.3)

**Findings covered:** BF-011, BF-015, BF-017

**Test scenarios:**
1. **Independent siblings:** Creating 3-occurrence recurring booking produces 3 independent DB rows with no shared series ID
2. **Single-occurrence delete:** Deleting one occurrence leaves others intact
3. **No series-aware edit:** Editing one occurrence does not propagate to siblings
4. **repeatno upper bound:** No server-side upper bound (beyond GTE 1 validation)

```java
@Test
void createRecurringBooking_producesIndependentSiblings() {
    // BF-011: repeat loop creates independent rows, no series id
    mockMvc.perform(post("/api/bookings")
            .with(validJwt())
            .content(bookingWithRepeat(3)))
        .andExpect(status().isCreated());
    
    List<BookingEntity> siblings = repository.findAll();
    assertThat(siblings).hasSize(3);
    // No series linkage — each row is independent
    siblings.forEach(b -> assertThat(b.getSeriesId()).isNull());
}

@Test
void deleteOneOccurrence_leavesOthersIntact() {
    // BF-015: single-row delete, no cascade
    List<BookingEntity> siblings = createRecurring(3);
    mockMvc.perform(delete("/api/bookings/{id}", siblings.get(0).getId()).with(validJwt()))
        .andExpect(status().isNoContent());
    assertThat(repository.findAll()).hasSize(2);
}
```

### Permission Matrix (F7)

**Findings covered:** BF-001, BF-018, BF-024, BF-026, BF-069, BF-070, BF-080, BF-081, BF-091

**Test scenarios:**
1. **17-flag coverage:** All 17 permission flags from BF-080 have at least one gated action tested
2. **Deny-by-default:** Undefined flag returns 403 (not 500, not allow)
3. **Tier1/Tier2 consistency:** Gateway and backend service agree on permission requirements (see `Tier1Tier2ConsistencyTest`)

```java
@Test
void allSeventeenPermissionFlags_haveAtLeastOneGatedAction() {
    // BF-080: 17 total permission flags (15 active, 2 dead: allowiCal, allowRSS)
    // This test is a documentation/inventory check — see Tier1Tier2ConsistencyTest
    // for actual enforcement verification
    List<String> activeFlags = permissionService.getActivePermissionFlags();
    assertThat(activeFlags).hasSize(15); // 17 total - 2 dead (allowiCal, allowRSS)
}

@Test
void unknownPermission_denyByDefault() {
    // BF-078: unrecognized role = no permissions granted
    mockMvc.perform(get("/api/bookings")
            .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_unknownPermission"))))
        .andExpect(status().isForbidden());
}
```

**Reference:** `services/users-permissions-service/src/test/java/.../security/Tier1Tier2ConsistencyTest.java`
proves Gateway (Tier-1) and backend (Tier-2) agree on permission requirements.

## Additional Coverage Areas

### User Management (F6)

**Findings covered:** BF-053, BF-054, BF-055, BF-056, BF-057, BF-060, BF-074, BF-075

```java
@Test
void updateAccount_cannotEscalateRole() {
    // BF-053: self-service update strips role from params server-side
    mockMvc.perform(put("/api/users/me")
            .with(validUserJwt())
            .content("{\"role\":\"admin\", \"firstName\":\"John\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("user")); // role unchanged
}

@Test
void passwordComplexity_minimum6charsWithDigitAndLowercase() {
    // BF-074: >=6 chars, >=1 digit, >=1 lowercase
    mockMvc.perform(post("/api/users")
            .with(adminJwt())
            .content(userWithPassword("SHORT"))) // too short
        .andExpect(status().isBadRequest());
    
    mockMvc.perform(post("/api/users")
            .with(adminJwt())
            .content(userWithPassword("validpass1"))) // meets requirements
        .andExpect(status().isCreated());
}
```

### Settings (F10)

**Findings covered:** BF-082, BF-083, BF-084, BF-085, BF-086

```java
@Test
void settingsUpdate_withEditableFalse_isRejected() {
    // BF-083: editable=0 blocks update even for authorized users
    mockMvc.perform(put("/api/settings/isDemoMode")
            .with(adminJwt())
            .content("{\"value\":\"1\"}"))
        .andExpect(status().isForbidden()); // editable=0
}

@Test
void approveBookingSetting_affectsNewBookingStatus() {
    // BF-085, BF-022: approveBooking setting drives auto-approve behavior
    // Covered by approval workflow tests above
}
```

## Updating Traceability Matrix

After writing a test:

1. Add test reference to matrix:
   ```bash
   # Find the finding row
   grep 'BF-001' .planning/traceability-matrix.csv
   
   # Update test_ref column (column 4) and status (column 5)
   # Old: BF-001,F7.1,,,not_started,
   # New: BF-001,F7.1,services/booking-service/.../BookingController.java,BookingControllerIntegrationTest::createBooking_permissionGates,tested,
   ```

2. Re-run coverage analysis:
   ```bash
   bash scripts/traceability/analyze-coverage.sh
   ```

3. Verify test runs in CI (once 08-03 CI pipeline is set up)

## References

- **Existing test examples:** `services/users-permissions-service/src/test/java/`
- **F0 audit findings:** `.planning/phases/01-legacy-functional-audit/baseline-inventory.md`
- **Traceability matrix:** `.planning/traceability-matrix.csv`
- **Coverage gaps:** `docs/testing/test-gap-report.md` (regenerate after adding tests)

---
*Keep this guide updated as new test patterns emerge.*
