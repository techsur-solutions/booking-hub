type FieldType = 'textfield' | 'select' | 'textarea' | 'radio' | 'checkbox';

interface FieldPreviewProps {
  field_type: FieldType;
  options: string[];
  label: string;
}

export function FieldPreview({ field_type, options, label }: FieldPreviewProps) {
  return (
    <div className="field-preview" data-testid="field-preview">
      <div className="field-preview__label">
        <label>{label}</label>
      </div>
      <div className="field-preview__control">
        {field_type === 'textfield' && (
          <input
            type="text"
            placeholder="Text input preview"
            disabled
            className="preview-input"
            data-testid="preview-textfield"
          />
        )}

        {field_type === 'textarea' && (
          <textarea
            placeholder="Textarea preview"
            disabled
            rows={3}
            className="preview-textarea"
            data-testid="preview-textarea"
          />
        )}

        {field_type === 'select' && (
          <select disabled className="preview-select" data-testid="preview-select">
            <option>Choose…</option>
            {options.map((opt, i) => (
              <option key={i} value={opt}>{opt}</option>
            ))}
          </select>
        )}

        {field_type === 'radio' && (
          <div className="preview-radio-group" data-testid="preview-radio">
            {options.length === 0 ? (
              <span className="preview-placeholder">Add options above to see preview</span>
            ) : (
              options.map((opt, i) => (
                <label key={i} className="preview-radio-label">
                  <input type="radio" disabled name="preview-radio" value={opt} />
                  {opt}
                </label>
              ))
            )}
          </div>
        )}

        {field_type === 'checkbox' && (
          <div className="preview-checkbox-group" data-testid="preview-checkbox">
            {options.length === 0 ? (
              <span className="preview-placeholder">Add options above to see preview</span>
            ) : (
              options.map((opt, i) => (
                <label key={i} className="preview-checkbox-label">
                  <input type="checkbox" disabled value={opt} />
                  {opt}
                </label>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
}
