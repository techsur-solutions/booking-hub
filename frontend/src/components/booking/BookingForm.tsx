import { useState } from 'react';
import type { CustomField } from '../../api/types';
import { Button } from '../Button';
import { Input } from '../Input';
import { Textarea } from '../Textarea';
import { Select } from '../Select';
import { ConflictWarning } from './ConflictWarning';
import { CustomFieldRenderer } from './CustomFieldRenderer';
import { useLocations } from '../../hooks/useLocations';
import { useResources } from '../../hooks/useResources';
import { useCustomFields } from '../../hooks/useCustomFields';

export type BookingMode = 'create' | 'edit';

export interface BookingFormValues {
  title: string;
  location_id: string;
  start_time: string;
  end_time: string;
  resource_ids: string[];
  description: string;
  custom_field_values: Record<string, string>; // field_id → value
  recurrence_pattern?: { type: 'daily' | 'weekly'; until: string } | null;
}

export interface BookingFormProps {
  /** 'create' = new booking, 'edit' = editing existing */
  mode: 'create' | 'edit';
  initialValues?: Partial<BookingFormValues>;
  /** For edit mode: the booking ID (used to exclude self from conflict check) */
  bookingId?: string;
  isLoading?: boolean;
  onSubmit: (values: BookingFormValues) => Promise<void>;
  onCancel?: () => void;
}

function toDatetimeLocal(iso: string): string {
  // Converts ISO-8601 to datetime-local input format (YYYY-MM-DDTHH:mm)
  return iso ? iso.slice(0, 16) : '';
}

/**
 * Reusable booking form supporting create and edit modes.
 *
 * In create mode: shows recurring pattern toggle.
 * In edit mode: hides recurring toggle (recurrence handled by scope dialog in parent).
 *
 * Re-validates conflicts on every field change (does not inherit from original booking).
 */
export function BookingForm({
  mode,
  initialValues,
  bookingId,
  isLoading = false,
  onSubmit,
  onCancel,
}: BookingFormProps) {
  const defaultEndTime = () => {
    if (initialValues?.start_time) {
      const start = new Date(initialValues.start_time);
      start.setHours(start.getHours() + 1);
      return toDatetimeLocal(start.toISOString());
    }
    const now = new Date();
    now.setHours(now.getHours() + 1);
    return toDatetimeLocal(now.toISOString());
  };

  const [title, setTitle] = useState(initialValues?.title ?? '');
  const [locationId, setLocationId] = useState(initialValues?.location_id ?? '');
  const [startTime, setStartTime] = useState(
    initialValues?.start_time ? toDatetimeLocal(initialValues.start_time) : toDatetimeLocal(new Date().toISOString())
  );
  const [endTime, setEndTime] = useState(
    initialValues?.end_time ? toDatetimeLocal(initialValues.end_time) : defaultEndTime()
  );
  const [resourceIds, setResourceIds] = useState<string[]>(initialValues?.resource_ids ?? []);
  const [description, setDescription] = useState(initialValues?.description ?? '');
  const [customFieldValues, setCustomFieldValues] = useState<Record<string, string>>(
    initialValues?.custom_field_values ?? {}
  );
  const [isRecurring, setIsRecurring] = useState(false);
  const [recurrenceType, setRecurrenceType] = useState<'daily' | 'weekly'>('daily');
  const [recurrenceUntil, setRecurrenceUntil] = useState('');

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [conflictState, setConflictState] = useState<{ hasConflicts: boolean; isBlocked: boolean }>(
    { hasConflicts: false, isBlocked: false }
  );

  const { data: locations = [], isLoading: locationsLoading, isError: locationsError } = useLocations();
  const { data: resources = [] } = useResources();
  const { data: customFields = [] } = useCustomFields(locationId);

  // Filter resources by location restriction
  const availableResources = resources.filter((r) => {
    if (!r.restrict_locations || r.restrict_locations.length === 0) return true;
    return locationId ? r.restrict_locations.includes(locationId) : false;
  });

  function toggleResource(id: string) {
    setResourceIds((prev) =>
      prev.includes(id) ? prev.filter((r) => r !== id) : [...prev, id]
    );
  }

  function validate(): boolean {
    const newErrors: Record<string, string> = {};
    if (!title.trim()) newErrors.title = 'Title is required';
    if (title.trim().length > 100) newErrors.title = 'Title must be 100 characters or less';
    if (!locationId) newErrors.location_id = 'Location is required';
    if (!startTime) newErrors.start_time = 'Start time is required';
    if (!endTime) newErrors.end_time = 'End time is required';
    if (startTime && endTime && new Date(endTime) <= new Date(startTime)) {
      newErrors.end_time = 'End time must be after start time';
    }
    if (description.length > 500) newErrors.description = 'Description must be 500 characters or less';
    // Required custom fields
    customFields.forEach((f: CustomField) => {
      if (f.required && !customFieldValues[f.id]) {
        newErrors[`custom_${f.id}`] = `${f.name} is required`;
      }
    });
    if (isRecurring && !recurrenceUntil) {
      newErrors.recurrenceUntil = 'Until date is required for recurring bookings';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;
    if (conflictState.isBlocked) return; // hard block

    const values: BookingFormValues = {
      title: title.trim(),
      location_id: locationId,
      start_time: new Date(startTime).toISOString(),
      end_time: new Date(endTime).toISOString(),
      resource_ids: resourceIds,
      description: description.trim(),
      custom_field_values: customFieldValues,
      recurrence_pattern:
        mode === 'create' && isRecurring
          ? { type: recurrenceType, until: recurrenceUntil }
          : null,
    };

    await onSubmit(values);
  }

  const isSaveDisabled = isLoading || conflictState.isBlocked;

  return (
    <form className="booking-form" onSubmit={handleSubmit} noValidate data-testid="booking-form">
      {/* Title */}
      <Input
        id="booking-title"
        label="Title"
        required
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        maxLength={100}
        error={errors.title}
        data-testid="booking-title"
      />

      {/* Location */}
      <div className="form-field">
        <label htmlFor="booking-location">
          Location <span className="required-mark" aria-hidden="true">*</span>
        </label>
        {locationsLoading && <div className="loading-state">Loading locations...</div>}
        {locationsError && (
          <div className="error" role="alert">
            Failed to load locations. Please refresh the page.
          </div>
        )}
        {!locationsLoading && !locationsError && locations.length === 0 && (
          <div className="warning" role="alert">
            No locations available. Contact your administrator.
          </div>
        )}
        {!locationsLoading && !locationsError && locations.length > 0 && (
          <Select
            id="booking-location"
            value={locationId}
            onChange={(e) => {
              setLocationId(e.target.value);
              // Clear resources when location changes
              setResourceIds([]);
              setCustomFieldValues({});
            }}
            options={locations.map((l) => ({ value: l.id, label: l.name }))}
            placeholder="Select a location"
            required
            error={errors.location_id}
            data-testid="booking-location"
          />
        )}
      </div>

      {/* Start time */}
      <Input
        id="booking-start-time"
        label="Start Time"
        type="datetime-local"
        required
        value={startTime}
        onChange={(e) => setStartTime(e.target.value)}
        error={errors.start_time}
        data-testid="booking-start-time"
      />

      {/* End time */}
      <Input
        id="booking-end-time"
        label="End Time"
        type="datetime-local"
        required
        value={endTime}
        onChange={(e) => setEndTime(e.target.value)}
        error={errors.end_time}
        data-testid="booking-end-time"
      />

      {/* Resources */}
      {availableResources.length > 0 && (
        <div className="form-field">
          <fieldset>
            <legend>Resources</legend>
            {availableResources.map((r) => (
              <label key={r.id} className="resource-checkbox-label">
                <input
                  type="checkbox"
                  value={r.id}
                  checked={resourceIds.includes(r.id)}
                  onChange={() => toggleResource(r.id)}
                  data-testid={`resource-${r.id}`}
                />
                {r.name}
                {r.type && ` (${r.type})`}
              </label>
            ))}
          </fieldset>
        </div>
      )}

      {/* Description */}
      <Textarea
        id="booking-description"
        label="Description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        maxLength={500}
        error={errors.description}
        data-testid="booking-description"
      />

      {/* Custom fields */}
      {customFields.length > 0 && (
        <div className="booking-form__custom-fields" data-testid="custom-fields-section">
          {customFields.map((field: CustomField) => (
            <CustomFieldRenderer
              key={field.id}
              field={field}
              value={customFieldValues[field.id] ?? ''}
              onChange={(fieldId, val) =>
                setCustomFieldValues((prev) => ({ ...prev, [fieldId]: val }))
              }
              error={errors[`custom_${field.id}`]}
            />
          ))}
        </div>
      )}

      {/* Recurring pattern (create mode only) */}
      {mode === 'create' && (
        <div className="form-field">
          <label className="recurring-toggle-label">
            <input
              type="checkbox"
              checked={isRecurring}
              onChange={(e) => setIsRecurring(e.target.checked)}
              data-testid="recurring-toggle"
            />
            Make this a recurring booking
          </label>

          {isRecurring && (
            <div className="recurring-pattern" data-testid="recurring-pattern">
              <Select
                id="recurrence-type"
                label="Repeat"
                value={recurrenceType}
                onChange={(e) => setRecurrenceType(e.target.value as 'daily' | 'weekly')}
                options={[
                  { value: 'daily', label: 'Daily' },
                  { value: 'weekly', label: 'Weekly' },
                ]}
                data-testid="recurrence-type"
              />
              <Input
                id="recurrence-until"
                label="Until"
                type="date"
                required
                value={recurrenceUntil}
                onChange={(e) => setRecurrenceUntil(e.target.value)}
                error={errors.recurrenceUntil}
                data-testid="recurrence-until"
              />
            </div>
          )}
        </div>
      )}

      {/* Conflict detection */}
      {locationId && startTime && endTime && (
        <ConflictWarning
          locationId={locationId}
          resourceIds={resourceIds}
          startTime={startTime ? new Date(startTime).toISOString() : ''}
          endTime={endTime ? new Date(endTime).toISOString() : ''}
          excludeBookingId={bookingId}
          onBlockChange={(blocked) =>
            setConflictState((prev) => ({ ...prev, isBlocked: blocked }))
          }
        />
      )}

      {/* Form actions */}
      <div className="booking-form__actions">
        {onCancel && (
          <Button
            type="button"
            variant="secondary"
            onClick={onCancel}
            data-testid="booking-cancel"
          >
            Cancel
          </Button>
        )}
        <Button
          type="submit"
          variant="primary"
          isLoading={isLoading}
          disabled={isSaveDisabled}
          data-testid="booking-save"
        >
          {mode === 'edit' ? 'Save Changes' : 'Create Booking'}
        </Button>
      </div>
    </form>
  );
}
