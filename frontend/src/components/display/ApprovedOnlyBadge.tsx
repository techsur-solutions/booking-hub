/**
 * ApprovedOnlyBadge
 *
 * Fixed-position watermark in the top-right corner of the Display Board,
 * guaranteeing that only approved bookings are shown.
 *
 * Non-dismissible — always visible while the Display Board is mounted.
 */
export function ApprovedOnlyBadge() {
  return (
    <div
      data-testid="approved-only-badge"
      style={{
        position: 'fixed',
        top: 'var(--spacing-md)',
        right: 'var(--spacing-md)',
        display: 'flex',
        alignItems: 'center',
        gap: 'var(--spacing-xs)',
        padding: 'var(--spacing-xs) var(--spacing-sm)',
        background: 'rgba(16, 185, 129, 0.85)', // --color-success with alpha
        color: '#ffffff',
        fontSize: 'var(--font-size-sm)',
        fontWeight: 'var(--font-weight-semibold)',
        fontFamily: 'var(--font-family)',
        borderRadius: 'var(--border-radius)',
        boxShadow: 'var(--shadow-md)',
        backdropFilter: 'blur(4px)',
        zIndex: 50,
        userSelect: 'none',
        pointerEvents: 'none',
      }}
      aria-label="This display shows approved bookings only"
    >
      <span aria-hidden="true">✓</span>
      <span>Approved Only</span>
    </div>
  );
}
