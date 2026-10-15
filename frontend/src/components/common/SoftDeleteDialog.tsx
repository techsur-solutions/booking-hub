export interface SoftDeleteDialogProps {
  open: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  entityName: string;
  contextMessage?: string;
}

export function SoftDeleteDialog({
  open,
  onConfirm,
  onCancel,
  entityName,
  contextMessage,
}: SoftDeleteDialogProps) {
  if (!open) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="soft-delete-dialog-title"
      data-testid="soft-delete-dialog"
      className="soft-delete-dialog-overlay"
      onClick={(e) => {
        // Close on backdrop click
        if (e.target === e.currentTarget) onCancel();
      }}
    >
      <div className="soft-delete-dialog">
        <h2 id="soft-delete-dialog-title" className="soft-delete-dialog__title">
          Delete {entityName}?
        </h2>

        {contextMessage && (
          <p
            className="soft-delete-dialog__context"
            data-testid="soft-delete-context-message"
          >
            {contextMessage}
          </p>
        )}

        <div className="soft-delete-dialog__actions">
          <button
            type="button"
            onClick={onCancel}
            className="soft-delete-dialog__cancel"
            data-testid="soft-delete-cancel"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="soft-delete-dialog__confirm soft-delete-dialog__confirm--danger"
            data-testid="soft-delete-confirm"
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  );
}
