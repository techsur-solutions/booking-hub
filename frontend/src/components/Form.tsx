import React from 'react';

export interface FormProps extends React.FormHTMLAttributes<HTMLFormElement> {
  /** Validation errors keyed by field name. Displayed as an accessible summary banner. */
  errors?: Record<string, string>;
}

/**
 * Form wrapper that optionally renders an accessible validation error summary banner.
 * The banner uses role="alert" so screen readers announce errors when they appear.
 * Individual field errors are still shown inline by Input/Select/Textarea components.
 */
export function Form({ errors, children, ...props }: FormProps) {
  const errorEntries = errors ? Object.entries(errors).filter(([, msg]) => !!msg) : [];
  const errorCount = errorEntries.length;

  return (
    <form {...props}>
      {errorCount > 0 && (
        <div role="alert" className="form-errors">
          <strong>
            {errorCount} error{errorCount > 1 ? 's' : ''} found:
          </strong>
          <ul>
            {errorEntries.map(([field, message]) => (
              <li key={field}>{message}</li>
            ))}
          </ul>
        </div>
      )}
      {children}
    </form>
  );
}
