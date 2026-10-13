# Test Stub: OQ-02 — Conflict Enforcement

**Open Question:** Conflict detection enforcement: hard-block for all users vs. soft-warning for approvers vs. legacy's always-non-blocking?

**open-questions.md item:** #2

**Traceability findings:** BF-028, BF-016 (conflict findings)

**Decision Required:** Choose ONE:
- **Option A:** Hard-block all users (strict integrity — new design, not legacy parity)
- **Option B:** Soft-warning for `allowApproveBooking` holders (permission-conditional split)
- **Option C:** Always non-blocking (legacy parity — `Bookings.check()` is informational only)

**Note from Phase 5:** The implementation currently follows the ROADMAP's permission-conditional split (hard-block without role_booking_approver, soft-warning with it). OQ-02 requires a formal product decision to confirm or override this implementation.

---

## Test Skeleton

**File:** `services/booking-service/src/test/java/com/bookinghub/booking/ConflictEnforcementTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConflictEnforcementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    private static final UUID LOCATION_A_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String conflictingBookingJson = """
        {
          "locationId": "00000000-0000-0000-0000-000000000001",
          "startTime": "2026-10-15T10:30:00",
          "endTime": "2026-10-15T11:30:00",
          "title": "Overlapping Meeting"
        }
        """;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        // Seed: existing booking in locationA, 10:00-11:00
        Booking existing = Booking.builder()
            .locationId(LOCATION_A_ID)
            .startTime(LocalDateTime.parse("2026-10-15T10:00:00"))
            .endTime(LocalDateTime.parse("2026-10-15T11:00:00"))
            .status("approved")
            .build();
        bookingRepository.save(existing);
    }

    // ===================================================================
    // DECISION-SPECIFIC LOGIC: UNCOMMENT tests for the chosen option
    // ===================================================================

    // --- Option A: Hard-block ALL users (including approvers) ----------

    @Test
    void createBooking_withConflict_blocksRegularUsers() throws Exception {
        mockMvc.perform(post("/api/bookings")
                .with(jwt().authorities(
                    new SimpleGrantedAuthority("role_calendar_viewer"),
                    new SimpleGrantedAuthority("role_booking_user")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(conflictingBookingJson))
            .andExpect(status().isConflict())  // 409
            .andExpect(jsonPath("$.error_code").value("BOOKING_CONFLICT"));
        assertThat(bookingRepository.findAll()).hasSize(1); // only existing
    }

    @Test
    void createBooking_withConflict_blocksEvenApprovers_OptionA() throws Exception {
        mockMvc.perform(post("/api/bookings")
                .with(jwt().authorities(
                    new SimpleGrantedAuthority("role_booking_approver")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(conflictingBookingJson))
            .andExpect(status().isConflict());  // 409, NOT 201 or 200 with warning
        assertThat(bookingRepository.findAll()).hasSize(1);
    }

    // --- Option B: Soft-warning for approvers (current implementation) --

    @Test
    void createBooking_withConflict_blocksNonApprovers_OptionB() throws Exception {
        mockMvc.perform(post("/api/bookings")
                .with(jwt().authorities(
                    new SimpleGrantedAuthority("role_booking_user")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(conflictingBookingJson))
            .andExpect(status().isConflict());  // 409 for non-approvers
    }

    @Test
    void createBooking_withConflict_allowsApproversWithWarning_OptionB() throws Exception {
        mockMvc.perform(post("/api/bookings")
                .with(jwt().authorities(
                    new SimpleGrantedAuthority("role_booking_approver")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(conflictingBookingJson))
            .andExpect(status().isCreated())  // 201 for approvers
            .andExpect(jsonPath("$.conflict_warning").value(true));
        assertThat(bookingRepository.findAll()).hasSize(2); // both bookings exist
    }

    // --- Option C: Always non-blocking (legacy parity) ------------------

    @Test
    void createBooking_withConflict_alwaysAllowsAnyUser_OptionC() throws Exception {
        mockMvc.perform(post("/api/bookings")
                .with(jwt().authorities(
                    new SimpleGrantedAuthority("role_booking_user")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(conflictingBookingJson))
            .andExpect(status().isCreated());  // 201 always
        assertThat(bookingRepository.findAll()).hasSize(2);
    }
}
```

---

## Steps to Activate This Stub

1. **Product owner decides:** Choose Option A, B, or C
2. **Update open-questions.md item #2:** Change status to RESOLVED, add Decision + Rationale
3. **Copy skeleton above** to the test file path shown
4. **Keep only the tests matching the chosen option** (delete the other options)
5. **Run tests** — they should FAIL if the implementation is wrong, PASS if correct
6. **Update traceability matrix:**
   ```csv
   # Find: grep 'BF-028\|BF-016' .planning/traceability-matrix.csv
   # Update test_ref and status columns for each matching row
   ```
7. **Delete this stub file** (it is fulfilled)
