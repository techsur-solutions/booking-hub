import { useState } from 'react';
import type { Location } from '../../api/types';
import { ColourPicker } from '../common/ColourPicker';

export interface LocationFormValues {
  name: string;
  class: string;
  colour: string;
  description: string;
  building: string;
  layouts: string;
}

export interface LocationFormProps {
  initialValues?: Partial<LocationFormValues>;
  onSubmit: (values: LocationFormValues) => Promise<void>;
  submitLabel?: string;
}

function isValidHex(value: string): boolean {
  return /^#[0-9A-Fa-f]{3}([0-9A-Fa-f]{3})?$/.test(value);
}

export function toFormValues(location: Location): LocationFormValues {
  return {
    name: location.name,
    class: '',
    colour: location.colour ?? '',
    description: '',
    building: location.building ?? '',
    layouts: location.layouts ?? '',
  };
}

export function LocationForm({
  initialValues = {},
  onSubmit,
  submitLabel = 'Save Location',
}: LocationFormProps) {
  const [name, setName] = useState(initialValues.name ?? '');
  const [cssClass, setCssClass] = useState(initialValues.class ?? '');
  const [colour, setColour] = useState(initialValues.colour ?? '#000000');
  const [description, setDescription] = useState(initialValues.description ?? '');
  const [building, setBuilding] = useState(initialValues.building ?? '');
  const [layouts, setLayouts] = useState(initialValues.layouts ?? '');

  const [nameError, setNameError] = useState<string | null>(null);
  const [colourError, setColourError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const validate = (): boolean => {
    let valid = true;

    if (!name.trim()) {
      setNameError('Name is required');
      valid = false;
    } else if (name.length > 255) {
      setNameError('Name must be at most 255 characters');
      valid = false;
    } else {
      setNameError(null);
    }

    if (!isValidHex(colour)) {
      setColourError('Invalid hex colour format');
      valid = false;
    } else {
      setColourError(null);
    }

    return valid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitError(null);

    if (!validate()) return;

    setSubmitting(true);
    try {
      await onSubmit({
        name: name.trim(),
        class: cssClass.trim(),
        colour,
        description: description.trim(),
        building: building.trim(),
        layouts: layouts.trim(),
      });
    } catch {
      setSubmitError('Failed to save location. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="location-form"
      data-testid="location-form"
      noValidate
    >
      {submitError && (
        <div role="alert" className="form-error" data-testid="location-form-error">
          {submitError}
        </div>
      )}

      {/* name — required, max 255 chars */}
      <div className="form-group">
        <label htmlFor="location-name">
          Name <span aria-hidden="true">*</span>
        </label>
        <input
          id="location-name"
          type="text"
          value={name}
          onChange={(e) => {
            setName(e.target.value);
            if (nameError) setNameError(null);
          }}
          required
          maxLength={255}
          className={nameError ? 'input--error' : ''}
          aria-invalid={nameError ? true : undefined}
          aria-describedby={nameError ? 'location-name-error' : undefined}
          data-testid="location-name-input"
        />
        {nameError && (
          <span
            id="location-name-error"
            role="alert"
            className="field-error"
            data-testid="location-name-error"
          >
            {nameError}
          </span>
        )}
      </div>

      {/* class — CSS class for legacy compatibility */}
      <div className="form-group">
        <label htmlFor="location-class">CSS Class</label>
        <input
          id="location-class"
          type="text"
          value={cssClass}
          onChange={(e) => setCssClass(e.target.value)}
          data-testid="location-class-input"
        />
      </div>

      {/* colour — ColourPicker with hex input + swatch */}
      <div className="form-group">
        <ColourPicker
          value={colour}
          onChange={(hex) => {
            setColour(hex);
            if (colourError) setColourError(null);
          }}
          label="Colour"
          error={colourError ?? undefined}
        />
      </div>

      {/* description — optional textarea */}
      <div className="form-group">
        <label htmlFor="location-description">Description</label>
        <textarea
          id="location-description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={3}
          data-testid="location-description-input"
        />
      </div>

      {/* building — text input, grouping metadata */}
      <div className="form-group">
        <label htmlFor="location-building">Building</label>
        <input
          id="location-building"
          type="text"
          value={building}
          onChange={(e) => setBuilding(e.target.value)}
          data-testid="location-building-input"
        />
      </div>

      {/* layouts — free-text, NOT a dropdown (F0 audit BF-048) */}
      <div className="form-group">
        <label htmlFor="location-layouts">Layouts</label>
        <input
          id="location-layouts"
          type="text"
          value={layouts}
          onChange={(e) => setLayouts(e.target.value)}
          data-testid="location-layouts-input"
        />
      </div>

      <div className="form-actions">
        <button
          type="submit"
          disabled={submitting}
          className="btn btn--primary"
          data-testid="location-form-submit"
        >
          {submitting ? 'Saving…' : submitLabel}
        </button>
      </div>
    </form>
  );
}
