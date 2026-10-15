import type { ReactNode } from 'react';
import { useEffect, useCallback, useId } from 'react';
import { createPortal } from 'react-dom';

export interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
  footer?: ReactNode;
  size?: 'sm' | 'default' | 'lg' | 'xl';
  /** Prevent closing on backdrop click (default: false — clicking outside closes) */
  disableBackdropClose?: boolean;
}

/**
 * Full-featured modal rendered into document.body via React portal.
 *
 * - Fixed header + fixed footer; content area scrolls independently (UI-MODAL-01).
 * - Click-outside-to-close via backdrop click handler.
 * - Escape key closes the modal (keyboard navigation).
 * - ARIA: role="dialog", aria-modal="true", aria-labelledby when title is set.
 * - Focus ring on close button via CSS --focus-ring token.
 */
export function Modal({
  isOpen,
  onClose,
  title,
  children,
  footer,
  size = 'default',
  disableBackdropClose = false,
}: ModalProps) {
  // Close on Escape key
  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    },
    [onClose]
  );

  useEffect(() => {
    if (isOpen) {
      document.addEventListener('keydown', handleKeyDown);
      // Prevent body scroll when modal is open
      document.body.style.overflow = 'hidden';
    }
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = '';
    };
  }, [isOpen, handleKeyDown]);

  // Generate a stable per-instance ID so concurrent titled modals each have
  // a unique aria-labelledby target (W5 — fixes duplicate id="modal-title")
  const instanceId = useId();

  if (!isOpen) return null;

  const titleId = title ? `modal-title-${instanceId}` : undefined;

  const sizeClass = size !== 'default' ? ` modal--${size}` : '';

  return createPortal(
    <div
      className="modal-backdrop"
      onClick={disableBackdropClose ? undefined : onClose}
      role="presentation"
    >
      <div
        className={`modal${sizeClass}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onClick={(e) => e.stopPropagation()}
      >
        {title && (
          <div className="modal__header">
            <h2 id={titleId}>{title}</h2>
            <button
              type="button"
              className="modal__close"
              onClick={onClose}
              aria-label="Close dialog"
            >
              &times;
            </button>
          </div>
        )}
        <div className="modal__body">{children}</div>
        {footer && <div className="modal__footer">{footer}</div>}
      </div>
    </div>,
    document.body
  );
}
