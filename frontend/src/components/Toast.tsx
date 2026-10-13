import type { ReactNode } from 'react';
import { useCallback, useEffect, useState, createContext, useContext } from 'react';
import { createPortal } from 'react-dom';

/* =============================================================================
   Toast notification system.
   Wraps StateConfirmationBanner's auto-dismiss + role=alert pattern.
   Provides:
     1. <ToastProvider> — mounts the toast container in the DOM
     2. useToast() — hook to show toasts from any component
     3. <Toast> — individual toast item (used internally)
   ============================================================================= */

export type ToastVariant = 'default' | 'success' | 'warning' | 'danger';

export interface ToastItem {
  id: string;
  message: string;
  variant: ToastVariant;
  autoDismiss: number;
}

/* ---------------------------------------------------------------------------
   Context
   --------------------------------------------------------------------------- */
interface ToastContextValue {
  showToast: (message: string, options?: { variant?: ToastVariant; autoDismiss?: number }) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

/* ---------------------------------------------------------------------------
   Provider
   --------------------------------------------------------------------------- */
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const showToast = useCallback(
    (message: string, options?: { variant?: ToastVariant; autoDismiss?: number }) => {
      const id = `toast-${Date.now()}-${Math.random().toString(36).slice(2)}`;
      setToasts((prev) => [
        ...prev,
        {
          id,
          message,
          variant: options?.variant ?? 'default',
          autoDismiss: options?.autoDismiss ?? 5000,
        },
      ]);
    },
    []
  );

  const dismiss = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      {toasts.length > 0 &&
        createPortal(
          <div className="toast-container" aria-live="polite" aria-atomic="false">
            {toasts.map((t) => (
              <ToastItem key={t.id} item={t} onDismiss={dismiss} />
            ))}
          </div>,
          document.body
        )}
    </ToastContext.Provider>
  );
}

/* ---------------------------------------------------------------------------
   useToast hook
   --------------------------------------------------------------------------- */
export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    throw new Error('useToast must be used inside <ToastProvider>');
  }
  return ctx;
}

/* ---------------------------------------------------------------------------
   Individual toast item
   --------------------------------------------------------------------------- */
interface ToastItemProps {
  item: ToastItem;
  onDismiss: (id: string) => void;
}

function ToastItem({ item, onDismiss }: ToastItemProps) {
  const { id, message, variant, autoDismiss } = item;

  const dismiss = useCallback(() => onDismiss(id), [id, onDismiss]);

  useEffect(() => {
    if (autoDismiss > 0) {
      const timer = setTimeout(dismiss, autoDismiss);
      return () => clearTimeout(timer);
    }
  }, [autoDismiss, dismiss]);

  const variantClass = variant !== 'default' ? ` toast--${variant}` : '';

  return (
    <div
      className={`toast${variantClass}`}
      role="alert"
    >
      <span className="toast__message">{message}</span>
      <button
        type="button"
        className="toast__dismiss"
        onClick={dismiss}
        aria-label="Dismiss notification"
      >
        &times;
      </button>
    </div>
  );
}

/* ---------------------------------------------------------------------------
   Standalone <Toast> component (for direct usage without provider)
   --------------------------------------------------------------------------- */
export interface ToastProps {
  message: string;
  variant?: ToastVariant;
  autoDismiss?: number; // ms, 0 = no auto-dismiss
  onDismiss: () => void;
}

/**
 * Standalone toast notification.
 * For app-wide toasts, use <ToastProvider> + useToast() instead.
 */
export function Toast({ message, variant = 'default', autoDismiss = 5000, onDismiss }: ToastProps) {
  useEffect(() => {
    if (autoDismiss > 0) {
      const timer = setTimeout(onDismiss, autoDismiss);
      return () => clearTimeout(timer);
    }
  }, [autoDismiss, onDismiss]);

  const variantClass = variant !== 'default' ? ` toast--${variant}` : '';

  return (
    <div className={`toast${variantClass}`} role="alert">
      <span className="toast__message">{message}</span>
      <button
        type="button"
        className="toast__dismiss"
        onClick={onDismiss}
        aria-label="Dismiss notification"
      >
        &times;
      </button>
    </div>
  );
}
