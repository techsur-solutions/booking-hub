import type { ReactNode } from 'react';

export interface CardProps {
  title?: string;
  children: ReactNode;
  actions?: ReactNode;
  className?: string;
}

/**
 * Card container with optional title header and footer actions.
 * Uses card/card__header/card__body/card__footer CSS classes from components.css.
 */
export function Card({ title, children, actions, className = '' }: CardProps) {
  return (
    <div className={`card${className ? ` ${className}` : ''}`}>
      {title && (
        <div className="card__header">
          <h3>{title}</h3>
        </div>
      )}
      <div className="card__body">{children}</div>
      {actions && <div className="card__footer">{actions}</div>}
    </div>
  );
}

/**
 * CardHeader — standalone header sub-component for when the title slot
 * needs richer content (e.g. title + subtitle + badge).
 */
export interface CardHeaderProps {
  children: ReactNode;
  className?: string;
}

export function CardHeader({ children, className = '' }: CardHeaderProps) {
  return (
    <div className={`card__header${className ? ` ${className}` : ''}`}>
      {children}
    </div>
  );
}

/**
 * CardBody — standalone body sub-component for compositional usage.
 */
export function CardBody({ children, className = '' }: { children: ReactNode; className?: string }) {
  return (
    <div className={`card__body${className ? ` ${className}` : ''}`}>
      {children}
    </div>
  );
}

/**
 * CardFooter — standalone footer sub-component for compositional usage.
 */
export function CardFooter({ children, className = '' }: { children: ReactNode; className?: string }) {
  return (
    <div className={`card__footer${className ? ` ${className}` : ''}`}>
      {children}
    </div>
  );
}
