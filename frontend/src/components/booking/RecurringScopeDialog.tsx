import { createPortal } from 'react-dom';
import { Button } from '../Button';

export type RecurringScope = 'this-only' | 'entire-series';

export interface RecurringScopeDialogProps {
  onScopeSelect: (scope: RecurringScope) => void;
}

/**
 * Non-dismissible modal shown when editing a recurring booking.
 * User MUST choose between "This occurrence only" and "Entire series"
 * before proceeding to the edit form.
 *
 * Design principle (UI-BOOK-02): "Boundary/scope decisions surfaced before, not after."
 *
 * Non-dismissible: no X button, no backdrop click-to-close, no Escape key close.
 */
export function RecurringScopeDialog({ onScopeSelect }: RecurringScopeDialogProps) {
  return createPortal(
    <div
      className="modal-backdrop"
      role="presentation"
      // No onClick — clicking outside does nothing (non-dismissible)
    >
      <div
        className="modal recurring-scope-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="recurring-scope-title"
        data-testid="recurring-scope-dialog"
      >
        <div className="modal__header">
          <h2 id="recurring-scope-title">Edit Recurring Booking</h2>
          {/* No close button — scope selection is required */}
        </div>

        <div className="modal__body recurring-scope-dialog__body">
          <p className="recurring-scope-dialog__prompt">
            This is a recurring booking. How do you want to edit it?
          </p>

          <div className="recurring-scope-dialog__options">
            <Button
              variant="secondary"
              size="large"
              className="recurring-scope-dialog__option"
              onClick={() => onScopeSelect('this-only')}
              data-testid="scope-this-only"
            >
              This occurrence only
            </Button>

            <Button
              variant="secondary"
              size="large"
              className="recurring-scope-dialog__option"
              onClick={() => onScopeSelect('entire-series')}
              data-testid="scope-entire-series"
            >
              Entire series
            </Button>
          </div>
        </div>

        <div className="modal__footer recurring-scope-dialog__footer">
          <small className="recurring-scope-dialog__warning">
            This choice cannot be changed after saving.
          </small>
        </div>
      </div>
    </div>,
    document.body
  );
}
