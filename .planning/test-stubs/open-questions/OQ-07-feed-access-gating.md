# Test Stub: OQ-07 — Feed Access Gating

**Open Question:** Are feeds token-gated (legacy per-user API token) or fully public (no auth)?

**open-questions.md item:** #7 (RESOLVED in open-questions.md, but note below)

**Traceability findings:** BF-093, BF-094

**Resolution status note:** open-questions.md item #7 is technically marked RESOLVED (legacy is token-gated), but the PROJECT.md interim decision was "fully public" and Phase 7's implementation chose to make feeds fully public (no auth) per F9.6. The forward product decision must be formally recorded.

**Decision Required:** Choose ONE:
- **Option A:** Token required (legacy parity — per-user `apitoken` header or query param)
- **Option B:** Fully public, no auth (current implementation — Phase 7 choice)

---

## Test Skeleton

**File:** `services/feeds-service/src/test/java/com/bookinghub/feeds/FeedAuthorizationTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class FeedAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    // ===================================================================
    // DECISION-SPECIFIC LOGIC: UNCOMMENT tests for the chosen option
    // ===================================================================

    // --- Option A: Token required (legacy parity) -----------------------

    @Test
    void getRss2Feed_withoutToken_returns401_OptionA() throws Exception {
        mockMvc.perform(get("/api/feeds/rss2"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getICalFeed_withoutToken_returns401_OptionA() throws Exception {
        mockMvc.perform(get("/api/feeds/ical"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getJsonFeed_withoutToken_returns401_OptionA() throws Exception {
        mockMvc.perform(get("/api/feeds/json"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getDisplayBoardFeed_withoutToken_returns401_OptionA() throws Exception {
        mockMvc.perform(get("/api/feeds/display"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getRss2Feed_withValidToken_returns200_OptionA() throws Exception {
        mockMvc.perform(get("/api/feeds/rss2")
                .header("X-Api-Token", "valid-user-api-token-value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/rss+xml"));
    }

    // --- Option B: Fully public, no auth (current Phase 7 implementation) --

    @Test
    void getRss2Feed_withoutAuth_returns200_OptionB() throws Exception {
        mockMvc.perform(get("/api/feeds/rss2"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/rss+xml;charset=UTF-8"));
    }

    @Test
    void getICalFeed_withoutAuth_returns200_OptionB() throws Exception {
        mockMvc.perform(get("/api/feeds/ical"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/calendar;charset=UTF-8"));
    }

    @Test
    void getJsonFeed_withoutAuth_returns200_OptionB() throws Exception {
        mockMvc.perform(get("/api/feeds/json"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getDisplayBoardFeed_withoutAuth_returns200_OptionB() throws Exception {
        mockMvc.perform(get("/api/feeds/display"))
            .andExpect(status().isOk());
    }
}
```

---

## Steps to Activate This Stub

1. **Product owner decides:** Choose Option A (token-gated) or Option B (fully public)
2. **Update open-questions.md item #7 or PROJECT.md:** Add forward product decision
3. **Copy skeleton above** to the test file path shown
4. **Keep only the tests matching the chosen option**
5. **Run tests** — should PASS if implementation already matches (Phase 7 implements Option B)
   - If Option A chosen: Phase 7 implementation must be updated to add token auth
6. **Update traceability matrix** for BF-093, BF-094
7. **Delete this stub file** (it is fulfilled)

## Impact Note

Choosing Option A (token-gated) requires modifying Phase 7's feeds-service to add token validation logic — feeds-service currently has NO spring-security/oauth2 dependency by design (F9.6 decision). A token-gate would require adding auth infrastructure to this service.
