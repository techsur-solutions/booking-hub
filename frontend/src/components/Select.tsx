import React from 'react';

export interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  hint?: string;
  options: SelectOption[];
  placeholder?: string;
  required?: boolean;
}

/**
 * Select dropdown with label, validation error, hint, and placeholder option.
 * Follows the same pattern as Input for consistent form field UX.
 * Custom chevron via CSS background-image in components.css.
 */
export function Select({
  label,
  error,
  hint,
  options,
  placeholder,
  required,
  className = '',
  ...props
}: SelectProps) {
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
      <select
        className={`form-select${error ? ' form-select--error' : ''}${className ? ` ${className}` : ''}`}
        aria-invalid={!!error || undefined}
        aria-describedby={describedBy}
        aria-required={required || undefined}
        {...props}
      >
        {placeholder && (
          <option value="" disabled>
            {placeholder}
          </option>
        )}
        {options.map((opt) => (
          <option key={opt.value} value={opt.value} disabled={opt.disabled}>
            {opt.label}
          </option>
        ))}
      </select>
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
