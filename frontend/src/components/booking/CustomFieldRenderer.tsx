import type { CustomField } from '../../api/types';

export interface CustomFieldRendererProps {
  field: CustomField;
  value: string;
  onChange: (fieldId: string, value: string) => void;
  error?: string;
}

/**
 * Renders a single custom field based on its field_type.
 * Supports: textfield, textarea, select, radio, checkbox.
 */
export function CustomFieldRenderer({ field, value, onChange, error }: CustomFieldRendererProps) {
  const { id, name, field_type, required, options } = field;
  const inputId = `custom-field-${id}`;

  const handleChange = (val: string) => onChange(id, val);

  return (
    <div className="form-field custom-field" data-testid={`custom-field-${id}`}>
      <label htmlFor={field_type !== 'radio' && field_type !== 'checkbox' ? inputId : undefined}>
        {name}
        {required && (
          <span className="required-mark" aria-hidden="true">
            *
          </span>
        )}
      </label>

      {field_type === 'textfield' && (
        <input
          id={inputId}
          type="text"
          className={`form-input${error ? ' form-input--error' : ''}`}
          value={value}
          onChange={(e) => handleChange(e.target.value)}
          aria-required={required || undefined}
          aria-invalid={!!error || undefined}
        />
      )}

      {field_type === 'textarea' && (
        <textarea
          id={inputId}
          className={`form-textarea${error ? ' form-textarea--error' : ''}`}
          value={value}
          onChange={(e) => handleChange(e.target.value)}
          aria-required={required || undefined}
          aria-invalid={!!error || undefined}
        />
      )}

      {field_type === 'select' && options && (
        <select
          id={inputId}
          className={`form-select${error ? ' form-select--error' : ''}`}
          value={value}
          onChange={(e) => handleChange(e.target.value)}
          aria-required={required || undefined}
          aria-invalid={!!error || undefined}
        >
          <option value="">Select…</option>
          {options.map((opt) => (
            <option key={opt} value={opt}>
              {opt}
            </option>
          ))}
        </select>
      )}

      {field_type === 'radio' && options && (
        <fieldset className="custom-field__fieldset">
          <legend className="sr-only">{name}</legend>
          {options.map((opt) => (
            <label key={opt} className="custom-field__radio-label">
              <input
                type="radio"
                name={`custom-${id}`}
                value={opt}
                checked={value === opt}
                onChange={() => handleChange(opt)}
              />
              {opt}
            </label>
          ))}
        </fieldset>
      )}

      {field_type === 'checkbox' && options && (
        <fieldset className="custom-field__fieldset">
          <legend className="sr-only">{name}</legend>
          {options.map((opt) => {
            const checked = value.split(',').filter(Boolean).includes(opt);
            return (
              <label key={opt} className="custom-field__checkbox-label">
                <input
                  type="checkbox"
                  value={opt}
                  checked={checked}
                  onChange={() => {
                    const vals = value.split(',').filter(Boolean);
                    const next = checked ? vals.filter((v) => v !== opt) : [...vals, opt];
                    handleChange(next.join(','));
                  }}
                />
                {opt}
              </label>
            );
          })}
        </fieldset>
      )}

      {error && (
        <span className="form-error" role="alert">
          {error}
        </span>
      )}
    </div>
  );
}
