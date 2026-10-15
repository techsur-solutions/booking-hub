import { useState, useEffect } from 'react';
import type { CustomField } from '../../api/types';
import { customFieldsApi } from '../../api/customFieldsApi';
import type { CustomFieldCreateRequest } from '../../api/customFieldsApi';
import { FieldPreview } from './FieldPreview';

export interface CustomFieldFormProps {
  mode: 'create' | 'edit';
  initialData?: CustomField;
  onSaved: (field: CustomField, verb: 'created' | 'updated') => void;
  onCancel: () => void;
}

const FIELD_TYPES = ['textfield', 'select', 'textarea', 'radio', 'checkbox'] as const;
type FieldType = (typeof FIELD_TYPES)[number];

// Choice-based field types: select, radio, checkbox → options validation applies
// Text-based field types: textfield, textarea → MUST NOT have options (input hidden)
const CHOICE_TYPES: FieldType[] = ['select', 'radio', 'checkbox'];

function isChoiceType(type: FieldType): boolean {
  return CHOICE_TYPES.includes(type);
}

export function CustomFieldForm({ mode, initialData, onSaved, onCancel }: CustomFieldFormProps) {
  const [name, setName] = useState(initialData?.name ?? '');
  const [fieldType, setFieldType] = useState<FieldType>(initialData?.field_type ?? 'textfield');
  const [required, setRequired] = useState(initialData?.required ?? false);
  // Options stored as newline-separated text for textarea input
  const [optionsText, setOptionsText] = useState(initialData?.options?.join('\n') ?? '');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Reset form when initialData changes (editing different field)
  useEffect(() => {
    setName(initialData?.name ?? '');
    setFieldType(initialData?.field_type ?? 'textfield');
    setRequired(initialData?.required ?? false);
    setOptionsText(initialData?.options?.join('\n') ?? '');
    setError(null);
  }, [initialData]);

  // Live options array derived from textarea — used by FieldPreview for live rendering
  const optionsArray: string[] = optionsText
    .split('\n')
    .map((o) => o.trim())
    .filter(Boolean);

  const handleFieldTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newType = e.target.value as FieldType;
    setFieldType(newType);
    // Clear options when switching to non-choice types
    // (textfield/textarea MUST NOT have options — field type controls visibility)
    if (!isChoiceType(newType)) {
      setOptionsText('');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Name is required.');
      return;
    }

    // Options validation: select/radio/checkbox MUST have at least one option
    // field_type select, radio, checkbox → options validation enforced
    if (isChoiceType(fieldType) && optionsArray.length === 0) {
      setError(`Options required for ${fieldType} type`);
      return;
    }

    const payload: CustomFieldCreateRequest = {
      name: name.trim(),
      field_type: fieldType,
      required,
      // textfield/textarea MUST NOT have options (null); choice types use optionsArray
      options: isChoiceType(fieldType) ? optionsArray : null,
    };

    setSubmitting(true);
    try {
      if (mode === 'create') {
        const res = await customFieldsApi.createDefinition(payload);
        onSaved(res.data, 'created');
      } else if (initialData) {
        const res = await customFieldsApi.updateDefinition(initialData.id, payload);
        onSaved(res.data, 'updated');
      }
    } catch {
      setError('Failed to save field definition. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="custom-field-form-wrapper">
      <form
        onSubmit={handleSubmit}
        className="custom-field-form"
        data-testid="custom-field-form"
        noValidate
      >
        <h3>{mode === 'create' ? 'Create Field Definition' : 'Edit Field Definition'}</h3>

        {error && (
          <div role="alert" className="form-error" data-testid="custom-field-form-error">
            {error}
          </div>
        )}

        <div className="form-group">
          <label htmlFor="cf-name">Name *</label>
          <input
            id="cf-name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            maxLength={255}
            placeholder="e.g. Catering Requirements"
            data-testid="cf-name"
          />
        </div>

        <div className="form-group">
          <label htmlFor="cf-field-type">Field Type</label>
          {/* F0-confirmed 5-value field_type enum: textfield | select | textarea | radio | checkbox */}
          <select
            id="cf-field-type"
            value={fieldType}
            onChange={handleFieldTypeChange}
            data-testid="cf-field-type"
          >
            <option value="textfield">textfield</option>
            <option value="select">select</option>
            <option value="textarea">textarea</option>
            <option value="radio">radio</option>
            <option value="checkbox">checkbox</option>
          </select>
        </div>

        <div className="form-group form-group--checkbox">
          <label>
            <input
              type="checkbox"
              checked={required}
              onChange={(e) => setRequired(e.target.checked)}
              data-testid="cf-required"
            />
            {' '}Required
          </label>
        </div>

        {/* Options textarea: shown ONLY for select, radio, checkbox.
            options validation: select/radio/checkbox MUST have at least one option.
            textfield/textarea: options MUST NOT be set (textarea hidden entirely). */}
        {isChoiceType(fieldType) && (
          <div className="form-group" data-testid="cf-options-group">
            <label htmlFor="cf-options">Options (one per line) *</label>
            <textarea
              id="cf-options"
              value={optionsText}
              onChange={(e) => setOptionsText(e.target.value)}
              rows={5}
              placeholder="Option 1&#10;Option 2&#10;Option 3"
              data-testid="cf-options"
            />
            <small className="field-hint">Maximum 100 options per field</small>
          </div>
        )}

        {/* FieldPreview: live field rendering, updates as field_type or options change */}
        <div className="field-preview-section" data-testid="field-preview-section">
          <h4>Live Preview</h4>
          <FieldPreview
            field_type={fieldType}
            options={isChoiceType(fieldType) ? optionsArray : []}
            label={name || 'Field Preview'}
          />
        </div>

        <div className="form-actions">
          <button type="button" onClick={onCancel} disabled={submitting}>
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="btn btn--primary"
            data-testid="cf-submit"
          >
            {submitting ? 'Saving…' : 'Save'}
          </button>
        </div>
      </form>
    </div>
  );
}
