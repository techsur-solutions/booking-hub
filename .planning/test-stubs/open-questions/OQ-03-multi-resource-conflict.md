# Test Stub: OQ-03 — Multi-Resource Conflict Scope

**Open Question:** When a booking has multiple resources and one conflicts, does the whole booking get rejected or only that resource?

**open-questions.md item:** #3

**Traceability findings:** BF-029, BF-030

**Decision Required:** Choose ONE:
- **Option A:** ANY resource conflict blocks the whole booking (strict integrity)
- **Option B:** Resource-level granular rejection — booking succeeds, conflicting resource stripped/warned

**Context:** The legacy `Resources.cfc::checkavailability` checks per-resource-id independently, returning a single 0/1 flag. Whether the UI aggregates into a booking-level verdict was unconfirmed from source alone. The new system must make an explicit choice.

---

## Test Skeleton

**File:** `services/booking-service/src/test/java/com/bookinghub/booking/MultiResourceConflictTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MultiResourceConflictTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    private static final UUID RESOURCE_R1 = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID RESOURCE_R2 = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID RESOURCE_R3 = UUID.fromString("00000000-0000-0000-0000-000000000013");

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        // R2 is already booked 10:00-11:00 (conflicts with our test window)
        Booking existing = Booking.builder()
            .resourceIds(List.of(RESOURCE_R2))
            .startTime(LocalDateTime.parse("2026-10-15T10:00:00"))
            .endTime(LocalDateTime.parse("2026-10-15T11:00:00"))
            .status("approved")
            .build();
        bookingRepository.save(existing);
    }

    private static final String threeResourceBookingJson = """
        {
          "locationId": "00000000-0000-0000-0000-000000000001",
          "resourceIds": [
            "00000000-0000-0000-0000-000000000011",
            "00000000-0000-0000-0000-000000000012",
            "00000000-0000-0000-0000-000000000013"
          ],
          "startTime": "2026-10-15T10:30:00",
          "endTime": "2026-10-15T11:30:00",
          "title": "Multi-Resource Meeting"
        }
        """;

    // ===================================================================
    // DECISION-SPECIFIC LOGIC: UNCOMMENT tests for the chosen option
    // ===================================================================

    // --- Option A: ANY conflict blocks whole booking --------------------

    @Test
    void createBooking_withMultipleResources_oneConflicts_rejectsEntireBooking_OptionA() throws Exception {
        // GIVEN: R1 available, R2 conflicts, R3 available
        // WHEN: create booking requesting R1 + R2 + R3
        // THEN: entire booking is rejected
        mockMvc.perform(post("/api/bookings")
                .with(jwt().authorities(new SimpleGrantedAuthority("role_booking_user")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(threeResourceBookingJson))
            .andExpect(status().isConflict())  // 409
            .andExpect(jsonPath("$.error_code").value("BOOKING_CONFLICT"))
            .andExpect(jsonPath("$.conflicting_resources[0]").value(RESOURCE_R2.toString()));

        // No new booking was created
        assertThat(bookingRepository.findAll()).hasSize(1); // only existing
    }

    // --- Option B: Per-resource granular (conflicting resource stripped) --

    @Test
    void createBooking_withMultipleResources_oneConflicts_createsWithAvailableOnly_OptionB() throws Exception {
        // GIVEN: R1 available, R2 conflicts, R3 available
        // WHEN: create booking requesting R1 + R2 + R3
        // THEN: booking created with only R1 + R3; R2 omitted with warning
        mockMvc.perform(post("/api/bookings")
                .with(jwt().authorities(new SimpleGrantedAuthority("role_booking_user")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(threeResourceBookingJson))
            .andExpect(status().isCreated())  // 201
            .andExpect(jsonPath("$.conflict_warning").value(true))
            .andExpect(jsonPath("$.unavailable_resources[0]").value(RESOURCE_R2.toString()));

        // New booking created but with fewer resources
        assertThat(bookingRepository.findAll()).hasSize(2);
        Booking created = bookingRepository.findAll().stream()
            .filter(b -> b.getTitle().equals("Multi-Resource Meeting"))
            .findFirst().orElseThrow();
        assertThat(created.getResourceIds()).containsExactlyInAnyOrder(RESOURCE_R1, RESOURCE_R3);
        assertThat(created.getResourceIds()).doesNotContain(RESOURCE_R2);
    }
}
```

---

## Steps to Activate This Stub

1. **Product owner decides:** Choose Option A or B
2. **Update open-questions.md item #3:** Change status to RESOLVED, add Decision + Rationale
3. **Copy skeleton above** to the test file path shown
4. **Keep only the test(s) matching the chosen option**
5. **Run tests** — should FAIL until implementation matches
6. **Update traceability matrix** for BF-029, BF-030
7. **Delete this stub file** (it is fulfilled)
