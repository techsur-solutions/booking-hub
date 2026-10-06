### Flow 5: Onboarding a New Bookable Room

**Trigger:** A newly renovated room needs to become bookable with correct colour and building grouping.
**User Story:** US-4.1, US-4.3, US-4.4
**Journey Reference:** JRN-03.1

```
[App shell] ──▶ [Sidebar: Admin > Locations]
                        │
                        ▼
          [Locations Admin /admin/locations]
                        │
                        ▼
          [Click "+ New Location"]
                        │
                        ▼
          [Single-page Location Form:
           name, colour swatch picker, building dropdown,
           optional layout metadata]
                        │
                        ▼
          [Click "Save"]
                        │
             ┌──────────┴──────────┐
             ▼                     ▼
      [Missing name:         [Valid: Save succeeds]
       inline error,                 │
       no navigation away]           ▼
                           [Success banner: "Location saved —
                            available for booking now" with a
                            direct "Verify in booking form" link]
                                     │
                                     ▼
                           [Click verify link ──▶ Booking Create
                            Form opens, new location already
                            present in the location dropdown,
                            rendered with its configured colour]
                                     │
                                     ▼
                           [Return to Audit Log Viewer — a
                            location.created entry is already
                            present, actor = Priya, timestamped]
```

**Steps:**
1. Priya navigates to Locations Admin via the sidebar (US-4.1) — a single modern list+form screen replacing the legacy multi-screen juggling pain point.
2. She clicks "+ New Location," which opens a single-page form capturing name, calendar colour (swatch picker, not a raw CSS class text field, for discoverability), building grouping, and optional layout metadata — all in one pass (JRN-03.1 Create stage).
3. Submitting with an empty name produces an inline, non-blocking-navigation error (US-4.1: "rejects a missing or empty name with a clear error") — she does not lose her other entered field values.
4. On success, a persistent success banner appears (not a toast that vanishes in 3 seconds) with a direct "Verify in booking form" link — this directly answers JRN-03.1's "did that save correctly?" anxiety.
5. Clicking the verify link opens the Booking Create Form; the new location is already present and selectable in the location dropdown with zero propagation delay (US-4.1: "immediately available to the Booking Service"), rendered using its configured colour (US-4.4).
6. Priya checks the Audit Log Viewer and confirms a `location.created` entry already exists with her as actor and a timestamp (US-4.3, F11) — closing the loop with zero manual cross-service verification.

**Exit point:** New location fully bookable, colour-coded, and audit-logged in under 5 minutes (JRN-03.1 success measure).

---
