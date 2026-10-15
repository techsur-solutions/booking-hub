import { useState } from 'react';
import type { CustomField } from '../../api/types';
import { customFieldsApi } from '../../api/customFieldsApi';
import type { CustomFieldCreateRequest } from '../../api/customFieldsApi';

export interface CustomFieldFormProps {
  mode: 'create' | 'edit';
  initialData?: CustomField;
  onSaved: (field: CustomField, verb: 'created' | 'updated') => void;
  onCancel: () => void;
}

const FIELD_TYPES = ['textfield', 'select', 'textarea', 'radio', 'checkbox'] as const;
type FieldType = (typeof FIELD_TYPES)[number];

function isChoiceType(type: FieldType): boolean {
  return type === 'select' || type === 'radio' || type === 'checkbox';
}

export function CustomFieldForm({ mode, initialData, onSaved, onCancel }: CustomFieldFormProps) {
  const [name, setName] = useState(initialData?.name ?? '');
  const [fieldType, setFieldType] = useState<FieldType>(initialData?.field_type ?? 'textfield');
  const [required, setRequired] = useState(initialData?.required ?? false);
  const [optionsText, setOptionsText] = useState(initialData?.options?.join('\n') ?? '');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Name is required.');
      return;
    }

    const options: string[] | null = isChoiceType(fieldType)
      ? optionsText
          .split('\n')
          .map((o) => o.trim())
          .filter(Boolean)
      : null;

    if (isChoiceType(fieldType) && (!options || options.length === 0)) {
      setError('Options are required for this field type.');
      return;
    }

    const payload: CustomFieldCreateRequest = {
      name: name.trim(),
      field_type: fieldType,
      required,
      options,
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
          data-testid="cf-name"
        />
      </div>

      <div className="form-group">
        <label htmlFor="cf-field-type">Field Type</label>
        <select
          id="cf-field-type"
          value={fieldType}
          onChange={(e) => setFieldType(e.target.value as FieldType)}
          data-testid="cf-field-type"
        >
          {FIELD_TYPES.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
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

      {isChoiceType(fieldType) && (
        <div className="form-group">
          <label htmlFor="cf-options">Options (one per line) *</label>
          <textarea
            id="cf-options"
            value={optionsText}
            onChange={(e) => setOptionsText(e.target.value)}
            rows={5}
            placeholder="Option 1&#10;Option 2&#10;Option 3"
            data-testid="cf-options"
          />
        </div>
      )}

      <div className="form-actions">
        <button type="button" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" disabled={submitting} className="btn btn--primary">
          {submitting ? 'Saving…' : 'Save'}
        </button>
      </div>
    </form>
  );
}
