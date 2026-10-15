import React from 'react';

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  hint?: string;
  required?: boolean;
}

/**
 * Textarea with label, validation error state and hint text.
 * Follows the same pattern as Input/Select for consistent form field UX.
 * Resizes vertically only (controlled via CSS).
 */
export function Textarea({ label, error, hint, required, className = '', ...props }: TextareaProps) {
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
      <textarea
        className={`form-textarea${error ? ' form-textarea--error' : ''}${className ? ` ${className}` : ''}`}
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
