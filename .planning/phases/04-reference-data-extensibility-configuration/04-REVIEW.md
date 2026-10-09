---
phase: 04
status: clean
blockers: 0
warnings: 0
files_reviewed: 3
files_reviewed_list:
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/service/CustomFieldService.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldRepository.java
  - services/settings-service/src/main/java/com/bookinghub/settings/service/SettingsService.java
reviewed_at: 2026-10-09T16:45:12Z
iteration: 2
---

# Phase 04 Code Review — Iteration 2

## Re-review scope

Verified fixer's changes in commits 7a67a50 (W1 fix) and e40e7c3 (W2 fix), and re-examined the disputed B1 finding.

## Previous findings resolution

### B1: CustomFieldService.update() save-before-outbox — CONFIRMED DISPUTED ✅
**File:** services/custom-field-service/src/main/java/com/bookinghub/customfield/service/CustomFieldService.java:160

**Original claim:** Line 170 had no explicit save before outbox event at line 172.

**Verification:** Line 160 contains `customFieldRepository.save(field);` immediately after the setter calls (lines 154-159) and BEFORE the outbox event write (line 162-166). This exactly matches the LocationService/ResourceService pattern. The original finding incorrectly identified line 170 as the save location, but the actual save is at line 160, in the correct position.

**Conclusion:** B1 was not a real defect. The code was correct in iteration 1.

### W1: SettingsService.update() defense-in-depth validation — FIXED ✅
**File:** services/settings-service/src/main/java/com/bookinghub/settings/service/SettingsService.java:134-138

**Fix verification (commit 7a67a50):** Comment added at lines 134-135:
```java
// Defense-in-depth validation: redundant with DB CHECK constraint (calendar_slot_size > 0)
// but catches constraint violations early with clearer error message
```

The comment correctly documents that the validation at line 136 (`if (resultingSlotSize <= 0)`) is intentionally redundant with the database CHECK constraint, serving as defense-in-depth to provide clearer error messages. This addresses the original warning's request to document the defensive redundancy.

**Conclusion:** W1 correctly fixed.

### W2: CustomFieldService.listApplicableToContext() N+1 query — FIXED ✅
**Files:**
- services/custom-field-service/src/main/java/com/bookinghub/customfield/service/CustomFieldService.java:95-98
- services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldRepository.java:18-30

**Fix verification (commit e40e7c3):**

**Old code (iteration 1, lines 95-108):**
```java
List<CustomFieldTemplate> applicableTemplates =
    customFieldTemplateRepository.findApplicableToContext(contextId);

Set<UUID> fieldIds = new LinkedHashSet<>();
for (CustomFieldTemplate template : applicableTemplates) {
    customFieldJoinRepository.findByCustomFieldTemplateId(template.getId())
        .forEach(join -> fieldIds.add(join.getCustomFieldId()));
}

return fieldIds.stream()
    .map(customFieldRepository::findByIdAndDeletedAtIsNull)
    .flatMap(java.util.Optional::stream)
    .map(this::toResponse)
    .toList();
```
This issued 1 query for templates + N queries for joins (one per template) + M queries for fields (one per distinct fieldId).

**New code (iteration 2, lines 95-98):**
```java
// Optimized single-query fetch to avoid N+1 problem (W2 fix)
return customFieldRepository.findApplicableToContext(contextId).stream()
    .map(this::toResponse)
    .toList();
```

**New repository method (CustomFieldRepository.java lines 24-30):**
```java
@Query(value = "SELECT DISTINCT cf.* FROM custom_fields cf " +
       "JOIN custom_field_joins cfj ON cfj.custom_field_id = cf.id " +
       "JOIN custom_field_templates cft ON cft.id = cfj.custom_field_template_id " +
       "WHERE (cft.context_id = :contextId OR cft.context_id IS NULL) " +
       "AND cf.deleted_at IS NULL", 
       nativeQuery = true)
List<CustomField> findApplicableToContext(@Param("contextId") UUID contextId);
```

**Analysis:**
1. The native SQL query performs a single database roundtrip, joining all three tables (`custom_fields` ← `custom_field_joins` ← `custom_field_templates`) in one query.
2. The WHERE clause correctly implements the applicability logic: `cft.context_id = :contextId OR cft.context_id IS NULL` (context-specific + global templates).
3. The `DISTINCT` keyword prevents duplicate fields if the same field appears in multiple applicable templates.
4. The `cf.deleted_at IS NULL` filter ensures soft-deleted fields are excluded.
5. The `@Param("contextId")` parameterization prevents SQL injection.
6. The service-layer null check (line 89) ensures this method is never called with NULL contextId, so the NULL-handling edge case doesn't arise in practice.

**Test coverage:** `CustomFieldControllerIntegrationTest.applicabilityQuery_returnsGlobalAndContextSpecificFields()` (lines 177-210) validates the exact scenario: global template with field A, scoped template with field B, query with contextId returns both A and B. The phase 04 gate shows all tests passed (gate_status: passed, last_updated: 2026-10-09T00:22:36Z).

**Query correctness verification:**
- Table/column names match V1 schema ✅
- JOIN direction (custom_fields ← joins ← templates) is correct ✅
- WHERE logic matches original service code's intent ✅
- DISTINCT handles the same field in multiple templates ✅
- Native query result mapping to `CustomField` entity works (tests pass) ✅

**Conclusion:** W2 correctly fixed. The N+1 query pattern has been eliminated with a functionally equivalent single-JOIN native query.

## New issues found

None.

## Cross-file seams re-checked

Re-verified the three modified files' integration points:

**CustomFieldService.listApplicableToContext() ↔ CustomFieldController.list():**
- Controller passes `contextId` from query param (can be null) → service checks null at line 89 → branches to `findAllByDeletedAtIsNull()` or `findApplicableToContext(contextId)`: OK, null-safety preserved across refactor.

**CustomFieldRepository.findApplicableToContext() ↔ CustomFieldService:**
- Service calls with non-null UUID (guarded by line 89 check) → repository executes native query with parameterized contextId: OK, contract is clear and safe.

**SettingsService.update() validation logic:**
- Lines 130-132 compute `resultingSlotSize` post-merge → lines 136-138 validate > 0 → lines 147-148 conditionally apply the request field: OK, validation logic unchanged, only comment added.

All seams remain correct after the fixes.

## Summary

Iteration 2 review confirms:
1. **B1 was never a real defect** — the save call was already present at the correct location in iteration 1. The disputed finding is closed as invalid.
2. **W1 fixed correctly** — documentation comment added per the requested fix direction.
3. **W2 fixed correctly** — N+1 query pattern eliminated with a single native SQL JOIN query that passes all existing tests and correctly implements the applicability logic.
4. **No new issues introduced** by the fixes.

**Phase 04 status: CLEAN** — zero blockers, zero warnings. The phase is ready to ship.
