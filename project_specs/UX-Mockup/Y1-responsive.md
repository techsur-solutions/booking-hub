## Responsive Considerations

### Desktop (>1024px)

- Full sidebar + header app shell persists across all authenticated screens; Calendar/Day views render multi-column grids (multiple locations side by side in Day View).
- Admin screens (Locations, Resources, Custom Fields, Users, Roles, Settings, Audit Log) use inline drawers/side panels for create/edit forms rather than full-page navigation, preserving list context (supports the "single-pass, under 5 minutes" onboarding goal, JRN-03.1).
- Display Board is designed primarily for desktop-class kiosk hardware/large-format displays; "NOW"/"UP NEXT" cards render at a 2–3 column grid.

### Tablet (768px–1024px)

- Sidebar collapses to an icon-only rail with flyout labels on hover/tap; Admin section becomes a single expandable group.
- Day View's per-location columns become horizontally scrollable with sticky time-axis on the left, rather than compressing illegibly.
- Booking Create/Edit Form and admin drawers become full-screen overlays instead of side panels, preserving touch-target sizing.
- Approval Queue rows stack the Approve/Deny buttons below the booking summary rather than right-aligned inline, to avoid accidental mis-taps.

### Mobile (<768px)

- Sidebar becomes a bottom nav bar or hamburger menu; "Calendar / Day / List" view switcher becomes a dropdown rather than a tab row.
- Calendar (month grid) view is de-emphasized in favor of List View as the practical default on small screens — month grids with colour blocks are not legible at this size; the view switcher still allows access to Calendar/Day for users who want it.
- Booking Detail Modal and Create/Edit Form become full-screen single-column layouts; the Scope Confirmation Dialog and conflict banner remain fully blocking (no bottom-sheet dismiss-by-swipe for a decision that requires an explicit choice, since accidental dismissal must never silently default to a scope).
- Audit Log Viewer's table collapses to a card-per-entry layout (Timestamp/Actor/Action stacked vertically) rather than a horizontally scrolling table.
- Display Board is not optimized for mobile as a primary target (it is a kiosk/signage surface) but remains functional and legible if viewed on a phone for spot-checking.
- Feed Subscription page's Copy buttons use the native share sheet on mobile where available, in addition to clipboard copy.

---
