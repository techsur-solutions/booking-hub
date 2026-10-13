import { useState, useEffect } from 'react';
import type { Location, Resource } from '../../api/types';
import { Input } from '../Input';
import { Select } from '../Select';
import { Textarea } from '../Textarea';
import { CustomFieldRenderer } from './CustomFieldRenderer';
import type { CustomFieldValues } from './CustomFieldRenderer';
import { useCustomFields } from '../../hooks/useCustomFields';

export interface RecurrencePattern {
  type: 'daily' | 'weekly';
  until: string; // ISO date string YYYY-MM-DD
}

export interface BookingFormValues {
  title: string;
  location_id: string;
  start_time: string; // datetime-local format: YYYY-MM-DDTHH:mm
  end_time: string;
  resource_ids: string[];
  description: string;
  custom_field_values: CustomFieldValues;
  recurrence_pattern: RecurrencePattern | null;
}

export interface BookingFormErrors {
  title?: string;
  location_id?: string;
  start_time?: string;
  end_time?: string;
  description?: string;
  [key: string]: string | undefined;
}

interface BookingFormProps {
  initialValues?: Partial<BookingFormValues>;
  locations: Location[];
  resources: Resource[];
  onSubmit: (values: BookingFormValues) => Promise<void>;
  onFormChange?: (values: BookingFormValues) => void;
  mode: 'create' | 'edit';
  isSubmitting?: boolean;
  submitDisabled?: boolean;
  /** External errors from API response (e.g. 400 validation) */
  externalErrors?: BookingFormErrors;
  /** Additional content to render before the Save button (e.g. ConflictWarning) */
  belowFields?: React.ReactNode;
}

/**
 * Reusable booking form component with all fields.
 * Handles: title, location, start/end datetime, resources, description,
 * custom fields (dynamic via useCustomFields), recurring pattern checkbox.
 */
export function BookingForm({
  initialValues,
  locations,
  resources,
  onSubmit,
  onFormChange,
  mode,
  isSubmitting = false,
  submitDisabled = false,
  externalErrors = {},
  belowFields,
}: BookingFormProps) {
  // ── Form state ───────────────────────────────────────────────────────────────
  const [title, setTitle] = useState(initialValues?.title ?? '');
  const [locationId, setLocationId] = useState(initialValues?.location_id ?? '');
  const [startTime, setStartTime] = useState(initialValues?.start_time ?? '');
  const [endTime, setEndTime] = useState(initialValues?.end_time ?? '');
  const [resourceIds, setResourceIds] = useState<string[]>(initialValues?.resource_ids ?? []);
  const [description, setDescription] = useState(initialValues?.description ?? '');
  const [customFieldValues, setCustomFieldValues] = useState<CustomFieldValues>(
    initialValues?.custom_field_values ?? {}
  );
  const [isRecurring, setIsRecurring] = useState(!!initialValues?.recurrence_pattern);
  const [recurrencePattern, setRecurrencePattern] = useState<RecurrencePattern | null>(
    initialValues?.recurrence_pattern ?? null
  );

  // ── Validation errors ────────────────────────────────────────────────────────
  const [errors, setErrors] = useState<BookingFormErrors>({});

  // ── Custom fields ────────────────────────────────────────────────────────────
  const { data: customFields = [] } = useCustomFields(locationId || undefined);

  // ── Auto-set end time = start + 1 hour when start changes ───────────────────
  useEffect(() => {
    if (startTime && !endTime) {
      const start = new Date(startTime);
      start.setHours(start.getHours() + 1);
      const endLocal = toDatetimeLocal(start);
      setEndTime(endLocal);
    }
  }, [startTime]); // eslint-disable-line react-hooks/exhaustive-deps

  // ── Notify parent on form change ─────────────────────────────────────────────
  useEffect(() => {
    if (onFormChange) {
      onFormChange(buildValues());
    }
  }, [title, locationId, startTime, endTime, resourceIds, description, customFieldValues, recurrencePattern]); // eslint-disable-line react-hooks/exhaustive-deps

  // ── Filtered resources by location (restrict_locations) ─────────────────────
  const filteredResources = resources.filter((r) => {
    if (!r.restrict_locations || r.restrict_locations.length === 0) return true;
    return locationId ? r.restrict_locations.includes(locationId) : false;
  });

  // ── Helpers ───────────────────────────────────────────────────────────────────
  function buildValues(): BookingFormValues {
    return {
      title,
      location_id: locationId,
      start_time: startTime,
      end_time: endTime,
      resource_ids: resourceIds,
      description,
      custom_field_values: customFieldValues,
      recurrence_pattern: isRecurring ? recurrencePattern : null,
    };
  }

  function validate(): BookingFormErrors {
    const errs: BookingFormErrors = {};
    if (!title.trim()) errs.title = 'Title is required';
    if (title.trim().length > 100) errs.title = 'Title must be 100 characters or less';
    if (!locationId) errs.location_id = 'Location is required';
    if (!startTime) errs.start_time = 'Start time is required';
    if (!endTime) errs.end_time = 'End time is required';
    if (startTime && endTime && endTime <= startTime) {
      errs.end_time = 'End time must be after start time';
    }
    if (description.length > 500) errs.description = 'Description must be 500 characters or less';
    // Validate required custom fields
    for (const field of customFields) {
      if (field.required && !customFieldValues[field.id]) {
        errs[`custom_${field.id}`] = `${field.name} is required`;
      }
    }
    return errs;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const errs = validate();
    setErrors(errs);
    if (Object.values(errs).some(Boolean)) return;
    await onSubmit(buildValues());
  }

  function handleCustomFieldChange(fieldId: string, value: string) {
    setCustomFieldValues((prev) => ({ ...prev, [fieldId]: value }));
  }

  function handleResourceToggle(resourceId: string) {
    setResourceIds((prev) =>
      prev.includes(resourceId) ? prev.filter((id) => id !== resourceId) : [...prev, resourceId]
    );
  }

  // Merge external errors (API validation) with local validation errors
  const allErrors: BookingFormErrors = { ...errors, ...externalErrors };

  const locationOptions = locations.map((l) => ({ value: l.id, label: l.name }));

  // Custom field errors for CustomFieldRenderer (keyed by field.id)
  const customFieldErrors: Record<string, string> = {};
  for (const [key, val] of Object.entries(allErrors)) {
    if (key.startsWith('custom_')) {
      customFieldErrors[key.replace('custom_', '')] = val ?? '';
    }
  }

  return (
    <form
      className="booking-form"
      onSubmit={handleSubmit}
      noValidate
      data-testid="booking-form"
    >
      {/* Title */}
      <Input
        id="booking-title"
        label="Title"
        required
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        error={allErrors.title}
        maxLength={100}
        placeholder="Enter booking title"
      />

      {/* Location */}
      <Select
        id="booking-location"
        label="Location"
        required
        value={locationId}
        onChange={(e) => {
          setLocationId(e.target.value);
          // Clear resource selection when location changes
          setResourceIds([]);
          // Clear custom field values for previous location
          setCustomFieldValues({});
        }}
        options={locationOptions}
        placeholder="Select a location"
        error={allErrors.location_id}
      />

      {/* Start Date/Time */}
      <Input
        id="booking-start-time"
        label="Start Date & Time"
        required
        type="datetime-local"
        value={startTime}
        onChange={(e) => setStartTime(e.target.value)}
        error={allErrors.start_time}
      />

      {/* End Date/Time */}
      <Input
        id="booking-end-time"
        label="End Date & Time"
        required
        type="datetime-local"
        value={endTime}
        onChange={(e) => setEndTime(e.target.value)}
        error={allErrors.end_time}
      />

      {/* Resources (multi-select checkboxes) */}
      {filteredResources.length > 0 && (
        <div className="form-field">
          <fieldset>
            <legend>Resources</legend>
            <div className="form-checkbox-group">
              {filteredResources.map((resource) => (
                <label key={resource.id} className="form-checkbox-label">
                  <input
                    type="checkbox"
                    value={resource.id}
                    checked={resourceIds.includes(resource.id)}
                    onChange={() => handleResourceToggle(resource.id)}
                  />
                  {resource.name}
                  {resource.type && <span className="resource-type"> ({resource.type})</span>}
                </label>
              ))}
            </div>
          </fieldset>
        </div>
      )}

      {/* Description */}
      <Textarea
        id="booking-description"
        label="Description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        error={allErrors.description}
        maxLength={500}
        placeholder="Optional description"
        rows={3}
      />

      {/* Dynamic Custom Fields */}
      <CustomFieldRenderer
        fields={customFields}
        values={customFieldValues}
        onChange={handleCustomFieldChange}
        errors={customFieldErrors}
      />

      {/* Recurring booking checkbox */}
      <div className="form-field">
        <label className="form-checkbox-label">
          <input
            type="checkbox"
            id="booking-is-recurring"
            checked={isRecurring}
            onChange={(e) => {
              setIsRecurring(e.target.checked);
              if (!e.target.checked) setRecurrencePattern(null);
            }}
            data-testid="recurring-checkbox"
          />
          Make this a recurring booking
        </label>
      </div>

      {/* Recurring Pattern fields (shown when recurring checked) */}
      {isRecurring && (
        <RecurringPatternInline
          startDate={startTime ? startTime.split('T')[0] : ''}
          value={recurrencePattern}
          onChange={setRecurrencePattern}
        />
      )}

      {/* Additional content slot (e.g. ConflictWarning) */}
      {belowFields}

      {/* Submit */}
      <div className="booking-form__actions">
        <button
          type="submit"
          className="btn btn--primary"
          disabled={isSubmitting || submitDisabled}
          data-testid="booking-submit"
        >
          {isSubmitting ? 'Saving…' : mode === 'create' ? 'Create Booking' : 'Save Changes'}
        </button>
      </div>
    </form>
  );
}

// ── Inline recurring pattern (imported by BookingCreate page) ──────────────────

interface RecurringPatternInlineProps {
  startDate: string;
  value: RecurrencePattern | null;
  onChange: (pattern: RecurrencePattern | null) => void;
}

function RecurringPatternInline({ startDate, value, onChange }: RecurringPatternInlineProps) {
  const type = value?.type ?? 'daily';
  const until = value?.until ?? '';

  function handleTypeChange(newType: 'daily' | 'weekly') {
    onChange({ type: newType, until });
  }

  function handleUntilChange(newUntil: string) {
    onChange({ type, until: newUntil });
  }

  return (
    <div className="recurring-pattern-inline" data-testid="recurring-pattern-inline">
      <div className="form-field">
        <label htmlFor="recurring-type">Repeat</label>
        <select
          id="recurring-type"
          className="form-select"
          value={type}
          onChange={(e) => handleTypeChange(e.target.value as 'daily' | 'weekly')}
          data-testid="recurring-type-select"
        >
          <option value="daily">Daily</option>
          <option value="weekly">Weekly</option>
        </select>
      </div>
      <div className="form-field">
        <label htmlFor="recurring-until">Until</label>
        <input
          id="recurring-until"
          type="date"
          className="form-input"
          value={until}
          min={startDate}
          onChange={(e) => handleUntilChange(e.target.value)}
          required
          data-testid="recurring-until-date"
        />
      </div>
    </div>
  );
}

// ── Utility ───────────────────────────────────────────────────────────────────

function toDatetimeLocal(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}`
  );
}
