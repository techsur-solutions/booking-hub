## F0: Legacy Functional Audit

**Description:** F0 is a documentation-producing feature, not a running service: it is the end-to-end review of the OxAlto RoomBooking ColdFusion/cfWheels codebase (~307 files) that produces the authoritative functional specification all other FRD chunks in this document are grounded in. Every subsequent feature chunk (F1–F13) either cites a confirmed F0 finding or carries an explicit `[OPEN QUESTION — deferred to F0]` flag pending that finding. F0 has no runtime API or schema of its own; its "output" is the Open Questions resolution log and the audit findings document that feeds F13's traceability matrix.

**Terminology:**
- **Audit finding:** A single documented fact about legacy behavior (a validation rule, workflow transition, permission gate, etc.), sourced from code, legacy docs, or the public demo site, with a citation (file/function/line or doc section).
- **Open Question:** A specific legacy behavior that cannot be confidently determined from available sources; logged per PRD Section 10's resolution process, never silently resolved.
- **Key Decision:** The explicit, recorded resolution of an Open Question that could not be determined from legacy evidence, requiring product sign-off before the dependent feature is implemented.

**Sub-features:**
- Controller-by-controller review: `Api`, `Bookings`, `Customfields`, `Eventdata`, `Locations`, `Logfiles`, `PasswordResets`, `Permissions`, `Resources`, `Sessions`, `Settings`, `Users`
- Model-by-model review: `Event`, `Eventresource`, `Location`, `Resource`, `Customfield`, `Customfieldjoin`, `Customfieldvalue`, `User`, `Settings`
- Route inventory (`config/routes.cfm`) mapped to controller actions
- CF application lifecycle event handler review: `onApplicationStart/End`, `onSessionStart/End`, `onRequestStart/End`, `onError`, `onMaintenance`, `onMissingTemplate`
- Per-environment settings behavior review (development/production/testing/design/maintenance)
- Open Questions list production and maintenance
- Baseline inventory handoff to F13 for traceability matrix construction

**Process:**
1. Auditor clones/reads the public OxAlto RoomBooking GitHub repository (reference only — this repository is not branched from or executed).
2. For each controller in scope, auditor documents every action, its permission gate(s), its request/response shape, and any embedded business rule (validation, side effect, email trigger).
3. For each model in scope, auditor documents every field, validation rule, association, and callback/lifecycle hook.
4. Auditor cross-references `config/routes.cfm` to confirm every controller action is reachable and to capture any route-level behavior (e.g., default actions, route constraints) not visible from the controller alone.
5. Auditor reviews CF application lifecycle event handlers for any global behavior affecting every request (e.g., session timeout, maintenance-mode interception).
6. Auditor consults `roombooking.readme.io` docs and the `roombooking.oxalto.co.uk` demo site to corroborate or disambiguate code-derived findings.
7. For any behavior that remains ambiguous after steps 2–6, auditor adds it to the Open Questions list with a precise description of what is unknown and why (per PRD Section 10 format).
8. Auditor compiles all findings into a structured functional spec document (the authoritative source this FRD's `[OPEN QUESTION]` flags will be resolved against) and hands off the baseline inventory to F13.
9. Product stakeholders review the Open Questions list; each question is either resolved via an explicit Key Decision (recorded in `PROJECT.md` Key Decisions table) or formally accepted as out-of-scope with sign-off, per PRD Section 7 success metric ("Zero open P0/P1 functional-parity gaps at release").
10. As each Open Question is resolved, the corresponding FRD chunk(s) in this document are updated to remove the `[OPEN QUESTION]` flag and restate the confirmed rule, and F13 adds a corresponding test.

**Inputs:**
- `legacy_repository_url` (string, required): Public GitHub URL of OxAlto RoomBooking.
- `legacy_docs_url` (string, required): `roombooking.readme.io`.
- `legacy_demo_url` (string, required): `roombooking.oxalto.co.uk`.
- `controller_list` (array of string, required): The 12 controllers named in PRD Section 5 F0.
- `model_list` (array of string, required): The 7 model groups named in PRD Section 5 F0.

**Outputs:**
- `legacy-audit-findings.md` (or equivalent structured document): one section per controller/model with cited findings.
- `open-questions.md`: numbered list matching PRD Section 10 format (at minimum the 10 seed questions, expanded as needed).
- Baseline inventory record consumed by F13 to seed the traceability matrix (one row per audit finding).

**Validation:**
- Every controller named in PRD Section 5 F0 has at least one corresponding audit-findings section before F0 is marked complete.
- Every model named in PRD Section 5 F0 has at least one corresponding audit-findings section before F0 is marked complete.
- Every PRD Section 10 seed Open Question (1–10) has either a cited resolution (with evidence reference) or remains logged as open with a clear "unknown because..." statement — none may be silently dropped.
- Every audit finding has a citation (source file/section/URL) traceable by a reviewer without re-doing the research.
- F0 is not marked complete (and no F1–F13 implementation work proceeds, per PRD priority "P0 — blocking") until the above three conditions hold.

**Error States:**
| Scenario | Impact | Required Action |
|---|---|---|
| A controller/model in scope has no corresponding audit section | F0 incomplete; downstream FRD chunks for related features remain speculative | Block sign-off; auditor must complete the missing section before F0 close-out |
| An Open Question is resolved by assumption rather than evidence or explicit Key Decision | Silent feature loss risk (violates PRD Section 6 "Ambiguity handling") | Reject the resolution; require either code/doc/demo citation or a recorded Key Decision with product sign-off |
| F0 findings document is produced but F13 traceability matrix seeding is skipped | "No functionality lost" becomes unverifiable (PRD Section 7 metric fails) | Block F13 kickoff; require baseline inventory handoff before any service implementation begins |

**API Surface (this feature):** None — F0 produces documentation artifacts, not a running service. No entry in `Y1-api.md`.

**Schema Surface (this feature):** None — F0 has no persisted runtime data. The baseline inventory it produces is consumed (not stored) by F13's traceability tooling, which may persist its own tracking schema (see `Y0-schema.md` if F13 requires a dedicated store; not required for F0 itself).
