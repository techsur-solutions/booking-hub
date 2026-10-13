import React from 'react';
import type { ReactNode } from 'react';

export type BadgeVariant = 'default' | 'success' | 'warning' | 'danger' | 'primary';

export interface BadgeProps {
  variant?: BadgeVariant;
  children: ReactNode;
  className?: string;
}

/**
 * Status badge with colour variants aligned to design token colours.
 * For location colour-coding, use BadgeLocation which accepts a hex colour
 * and uses the badge--location CSS class with --location-color custom property.
 */
export function Badge({ variant = 'default', children, className = '' }: BadgeProps) {
  return (
    <span className={`badge badge--${variant}${className ? ` ${className}` : ''}`}>
      {children}
    </span>
  );
}

export interface BadgeLocationProps {
  colour: string;  // hex colour from locations.colour field
  children: ReactNode;
  className?: string;
}

/**
 * Location colour badge — renders using the location's configured colour.
 * Implements UI-CAL-04: location colours render consistently across Calendar,
 * Day, and List views via CSS custom property --location-color.
 */
export function BadgeLocation({ colour, children, className = '' }: BadgeLocationProps) {
  return (
    <span
      className={`badge badge--location${className ? ` ${className}` : ''}`}
      style={{ '--location-color': colour } as React.CSSProperties}
    >
      {children}
    </span>
  );
}
