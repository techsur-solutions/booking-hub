/**
 * InlineHelp
 *
 * Renders a small help text block below a form field.
 * Lighter-weight alternative to HelpTooltip for descriptive copy that should
 * always be visible (not hidden behind a hover).
 *
 * Usage:
 *   <InlineHelp text="Leave empty to allow this resource in all locations." />
 */
export interface InlineHelpProps {
  text: string;
  className?: string;
}

export function InlineHelp({ text, className }: InlineHelpProps) {
  return (
    <small
      className={`inline-help${className ? ` ${className}` : ''}`}
      data-testid="inline-help"
      style={{
        display: 'block',
        marginTop: '0.25em',
        fontSize: 'var(--font-size-xs, 0.75rem)',
        color: 'var(--color-text-light, #6b7280)',
        lineHeight: 1.4,
      }}
    >
      {text}
    </small>
  );
}
