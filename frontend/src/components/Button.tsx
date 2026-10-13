import React from 'react';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'small' | 'medium' | 'large';
  isLoading?: boolean;
}

/**
 * Reusable button with primary/secondary/danger/ghost variants.
 * Supports disabled and loading states with spinner.
 * Focus ring via --focus-ring CSS token for keyboard navigation.
 */
export function Button({
  variant = 'primary',
  size = 'medium',
  isLoading = false,
  children,
  className = '',
  disabled,
  ...props
}: ButtonProps) {
  return (
    <button
      className={`btn btn--${variant} btn--${size}${className ? ` ${className}` : ''}`}
      disabled={isLoading || disabled}
      aria-busy={isLoading || undefined}
      {...props}
    >
      {isLoading ? <span className="btn__spinner" aria-hidden="true" /> : children}
    </button>
  );
}
