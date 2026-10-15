import { useState, useRef, useEffect, useId } from 'react';

export interface HelpTooltipProps {
  /** The help text to display in the popover */
  content: string;
  /** Preferred popover position relative to the trigger icon */
  position?: 'top' | 'right' | 'bottom' | 'left';
  /** Additional class name for the wrapper span */
  className?: string;
}

/**
 * HelpTooltip
 *
 * Renders a circular question-mark icon (ⓘ) that shows a popover with help
 * text on mouse hover or keyboard focus. Closes on Escape key press.
 *
 * No external icon library required — uses a CSS-styled span with a '?' character.
 *
 * Usage:
 *   <label>
 *     Is Unique
 *     <HelpTooltip content="When checked, this resource can only be used by one booking at a time." />
 *   </label>
 *
 * Accessibility:
 *   - The trigger button has aria-describedby pointing at the tooltip content
 *   - role="tooltip" on the popover
 *   - Keyboard: Tab to focus, Escape to close, blur to close
 */
export function HelpTooltip({ content, position = 'right', className }: HelpTooltipProps) {
  const [visible, setVisible] = useState(false);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const tooltipId = useId();

  // Close on Escape key
  useEffect(() => {
    if (!visible) return;

    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') {
        setVisible(false);
        buttonRef.current?.focus();
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [visible]);

  const positionClass = `help-tooltip__popover--${position}`;

  return (
    <span
      className={`help-tooltip-wrapper${className ? ` ${className}` : ''}`}
      style={{ position: 'relative', display: 'inline-flex', alignItems: 'center', marginLeft: '0.35em' }}
      data-testid="help-tooltip-wrapper"
    >
      <button
        ref={buttonRef}
        type="button"
        aria-label="Help"
        aria-describedby={visible ? tooltipId : undefined}
        data-testid="help-tooltip-trigger"
        className="help-tooltip__trigger"
        onMouseEnter={() => setVisible(true)}
        onMouseLeave={() => setVisible(false)}
        onFocus={() => setVisible(true)}
        onBlur={() => setVisible(false)}
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: '1.1em',
          height: '1.1em',
          borderRadius: '50%',
          border: '1.5px solid currentColor',
          background: 'transparent',
          color: 'var(--color-text-light, #6b7280)',
          fontSize: '0.75em',
          fontWeight: 700,
          cursor: 'help',
          padding: 0,
          lineHeight: 1,
          flexShrink: 0,
        }}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            setVisible((v) => !v);
          }
        }}
      >
        ?
      </button>

      {visible && (
        <div
          id={tooltipId}
          role="tooltip"
          data-testid="help-tooltip-popover"
          className={`help-tooltip__popover ${positionClass}`}
          style={{
            position: 'absolute',
            zIndex: 1000,
            maxWidth: 280,
            minWidth: 160,
            padding: '0.5em 0.75em',
            background: 'var(--color-text, #111827)',
            color: 'var(--color-bg, #ffffff)',
            borderRadius: 'var(--border-radius, 4px)',
            fontSize: 'var(--font-size-xs, 0.75rem)',
            lineHeight: 1.5,
            boxShadow: '0 4px 12px rgba(0,0,0,0.18)',
            pointerEvents: 'none',
            whiteSpace: 'normal',
            wordBreak: 'break-word',
            // Positioning — default right
            ...(position === 'right' && {
              left: 'calc(100% + 0.5em)',
              top: '50%',
              transform: 'translateY(-50%)',
            }),
            ...(position === 'left' && {
              right: 'calc(100% + 0.5em)',
              top: '50%',
              transform: 'translateY(-50%)',
            }),
            ...(position === 'top' && {
              bottom: 'calc(100% + 0.5em)',
              left: '50%',
              transform: 'translateX(-50%)',
            }),
            ...(position === 'bottom' && {
              top: 'calc(100% + 0.5em)',
              left: '50%',
              transform: 'translateX(-50%)',
            }),
          }}
        >
          {content}
        </div>
      )}
    </span>
  );
}
