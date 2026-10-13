import React from 'react';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  hint?: string;
  required?: boolean;
}

/**
 * Text input with label, validation error state and hint text.
 * Uses ARIA attributes (aria-invalid, aria-describedby) for accessible error messaging.
 * Focus ring via --focus-ring / --focus-ring-danger CSS tokens.
 */
export function Input({ label, error, hint, required, className = '', ...props }: InputProps) {
  const describedBy =
    [error ? `${props.id}-error` : null, hint ? `${props.id}-hint` : null]
      .filter(Boolean)
      .join(' ') || undefined;

  return (
    <div className="form-field">
      {label && (
        <label htmlFor={props.id}>
          {label}
          {required && <span className="required-mark" aria-hidden="true">*</span>}
        </label>
      )}
      <input
        className={`form-input${error ? ' form-input--error' : ''}${className ? ` ${className}` : ''}`}
        aria-invalid={!!error || undefined}
        aria-describedby={describedBy}
        aria-required={required || undefined}
        {...props}
      />
      {hint && !error && (
        <span id={`${props.id}-hint`} className="form-hint">
          {hint}
        </span>
      )}
      {error && (
        <span id={`${props.id}-error`} className="form-error" role="alert">
          {error}
        </span>
      )}
    </div>
  );
}
