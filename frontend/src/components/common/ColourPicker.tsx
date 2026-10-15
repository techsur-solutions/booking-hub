import { useState } from 'react';

export interface ColourPickerProps {
  value: string;
  onChange: (hex: string) => void;
  label?: string;
  error?: string;
}

// Validates hex colour: #RGB or #RRGGBB format
// T-10-01: prevents malformed colour strings (regex ^#[0-9A-Fa-f]{3,6}$)
const HEX_REGEX = /^#[0-9A-Fa-f]{3}([0-9A-Fa-f]{3})?$/;

function isValidHex(value: string): boolean {
  return HEX_REGEX.test(value);
}

export function ColourPicker({ value, onChange, label = 'Colour', error }: ColourPickerProps) {
  const [inputValue, setInputValue] = useState(value);
  const [validationError, setValidationError] = useState<string | null>(null);

  const displayError = error ?? validationError;
  const swatchColour = isValidHex(value) ? value : '#cccccc';

  const handleTextChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value;
    setInputValue(raw);
    setValidationError(null);

    // Live propagate only when valid
    if (isValidHex(raw)) {
      onChange(raw);
    }
  };

  const handleTextBlur = () => {
    if (!isValidHex(inputValue)) {
      setValidationError('Invalid hex colour format');
    } else {
      setValidationError(null);
      onChange(inputValue);
    }
  };

  const handleNativePickerChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const hex = e.target.value; // native picker always returns #RRGGBB
    setInputValue(hex);
    setValidationError(null);
    onChange(hex);
  };

  return (
    <div className="colour-picker" data-testid="colour-picker">
      {label && (
        <label htmlFor="colour-picker-text" className="colour-picker__label">
          {label}
        </label>
      )}

      <div className="colour-picker__controls">
        {/* Visual swatch */}
        <span
          className="colour-picker__swatch"
          style={{
            backgroundColor: swatchColour,
            display: 'inline-block',
            width: '2rem',
            height: '2rem',
            border: '1px solid #ccc',
            borderRadius: '4px',
            verticalAlign: 'middle',
          }}
          aria-label={`Selected colour: ${value}`}
          data-testid="colour-swatch-preview"
        />

        {/* Hex text input */}
        <input
          id="colour-picker-text"
          type="text"
          value={inputValue}
          onChange={handleTextChange}
          onBlur={handleTextBlur}
          placeholder="#RRGGBB"
          maxLength={7}
          className={`colour-picker__text-input${displayError ? ' colour-picker__text-input--error' : ''}`}
          aria-describedby={displayError ? 'colour-picker-error' : undefined}
          aria-invalid={displayError ? true : undefined}
          data-testid="colour-picker-input"
        />

        {/* Native browser colour picker fallback */}
        <input
          type="color"
          value={isValidHex(value) ? value : '#000000'}
          onChange={handleNativePickerChange}
          className="colour-picker__native"
          aria-label="Open colour picker"
          data-testid="colour-picker-native"
        />
      </div>

      {displayError && (
        <span
          id="colour-picker-error"
          role="alert"
          className="colour-picker__error"
          data-testid="colour-picker-error"
        >
          {displayError}
        </span>
      )}
    </div>
  );
}
