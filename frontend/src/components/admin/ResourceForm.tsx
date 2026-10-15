import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import type { Resource, Location } from '../../api/types';
import { resourcesApi } from '../../api/resourcesApi';
import type { ResourceCreateRequest, ResourceUpdateRequest } from '../../api/resourcesApi';
import { locationsApi } from '../../api/locationsApi';
import { StateConfirmationBanner } from '../StateConfirmationBanner';

export interface ResourceFormProps {
  /** If provided, the form is in edit mode; otherwise create mode. */
  initialData?: Resource;
}

export function ResourceForm({ initialData }: ResourceFormProps) {
  const navigate = useNavigate();
  const isEdit = Boolean(initialData);

  // Form state
  const [name, setName] = useState(initialData?.name ?? '');
  const [type, setType] = useState(initialData?.type ?? '');
  const [description, setDescription] = useState(initialData?.description ?? '');
  const [isUnique, setIsUnique] = useState(initialData?.is_unique ?? false);
  const [restrictLocations, setRestrictLocations] = useState<string[]>(
    initialData?.restrict_locations ?? []
  );

  // UI state
  const [submitting, setSubmitting] = useState(false);
  const [nameError, setNameError] = useState<string | null>(null);
  const [banner, setBanner] = useState<{ action: string; timestamp: Date } | null>(null);

  // Fetch active locations for the restrict_locations multi-select
  const { data: allLocations } = useQuery<Location[]>({
    queryKey: ['locations', 'resource-form'],
    queryFn: () => locationsApi.getAll().then((res) => res.data),
  });

  const activeLocations = allLocations?.filter((l) => l.deleted_at === null) ?? [];

  // Sync initialData if it changes (edit mode)
  useEffect(() => {
    if (initialData) {
      setName(initialData.name);
      setType(initialData.type ?? '');
      setDescription(initialData.description ?? '');
      setIsUnique(initialData.is_unique);
      setRestrictLocations(initialData.restrict_locations ?? []);
    }
  }, [initialData]);

  const validate = (): boolean => {
    if (!name.trim()) {
      setNameError('Name is required.');
      return false;
    }
    if (name.trim().length > 255) {
      setNameError('Name must be 255 characters or fewer.');
      return false;
    }
    setNameError(null);
    return true;
  };

  const handleLocationToggle = (locationId: string) => {
    setRestrictLocations((prev) =>
      prev.includes(locationId)
        ? prev.filter((id) => id !== locationId)
        : [...prev, locationId]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) return;

    setSubmitting(true);
    try {
      if (isEdit && initialData) {
        const payload: ResourceUpdateRequest = {
          name: name.trim(),
          type: type.trim() || undefined,
          description: description.trim() || undefined,
          is_unique: isUnique,
          restrict_locations: restrictLocations,
        };
        await resourcesApi.update(initialData.id, payload);
      } else {
        const payload: ResourceCreateRequest = {
          name: name.trim(),
          type: type.trim() || undefined,
          description: description.trim() || undefined,
          is_unique: isUnique,
          restrict_locations: restrictLocations,
        };
        await resourcesApi.create(payload);
      }

      setBanner({ action: 'Resource saved', timestamp: new Date() });

      // Navigate back after short delay to let banner show
      setTimeout(() => {
        navigate('/admin/resources');
      }, 1500);
    } catch (err) {
      console.error('[ResourceForm] Failed to save resource:', err);
      setNameError('Failed to save resource. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="resource-form-container" data-testid="resource-form-container">
      {banner && (
        <StateConfirmationBanner
          action={banner.action}
          timestamp={banner.timestamp}
          onDismiss={() => setBanner(null)}
          variant="success"
        />
      )}

      <form
        onSubmit={handleSubmit}
        className="resource-form"
        data-testid="resource-form"
        noValidate
      >
        {/* Name */}
        <div className="form-field">
          <label htmlFor="resource-name">
            Name <span className="required-mark" aria-hidden="true">*</span>
          </label>
          <input
            id="resource-name"
            type="text"
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              if (nameError) setNameError(null);
            }}
            maxLength={255}
            aria-required="true"
            aria-invalid={nameError ? 'true' : undefined}
            aria-describedby={nameError ? 'resource-name-error' : undefined}
            data-testid="resource-name-input"
          />
          {nameError && (
            <span
              id="resource-name-error"
              className="form-error"
              role="alert"
              data-testid="resource-name-error"
            >
              {nameError}
            </span>
          )}
        </div>

        {/* Type */}
        <div className="form-field">
          <label htmlFor="resource-type">Type</label>
          <input
            id="resource-type"
            type="text"
            value={type}
            onChange={(e) => setType(e.target.value)}
            placeholder="e.g. Equipment, Service, AV"
            data-testid="resource-type-input"
          />
        </div>

        {/* Description */}
        <div className="form-field">
          <label htmlFor="resource-description">Description</label>
          <textarea
            id="resource-description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={4}
            data-testid="resource-description-input"
          />
        </div>

        {/* Is Unique — checkbox with tooltip */}
        <div className="form-field form-field--checkbox">
          <label className="checkbox-label">
            <input
              id="resource-is-unique"
              type="checkbox"
              checked={isUnique}
              onChange={(e) => setIsUnique(e.target.checked)}
              data-testid="resource-is-unique-checkbox"
            />
            {' '}Is Unique
          </label>
          <span
            className="field-tooltip"
            title="When checked, this resource can only be used by one booking at a time. Enables resource-level conflict detection."
            aria-label="When checked, this resource can only be used by one booking at a time. Enables resource-level conflict detection."
            data-testid="resource-is-unique-tooltip"
          >
            ⓘ
          </span>
          <p className="field-hint">
            When checked, this resource can only be used by one booking at a time. Enables
            resource-level conflict detection.
          </p>
        </div>

        {/* Restrict Locations — multi-select via checkboxes */}
        <div className="form-field" data-testid="restrict-locations-field">
          <fieldset className="restrict-locations-fieldset">
            <legend className="restrict-locations-legend">
              Restrict Locations
              <span className="field-hint-inline">
                {' '}— Leave all unchecked to allow this resource in all locations
              </span>
            </legend>
            {activeLocations.length === 0 ? (
              <p className="restrict-locations-empty">No locations available.</p>
            ) : (
              <ul className="restrict-locations-list" data-testid="restrict-locations-multi-select">
                {activeLocations.map((location) => (
                  <li key={location.id} className="restrict-locations-item">
                    <label className="checkbox-label">
                      <input
                        type="checkbox"
                        value={location.id}
                        checked={restrictLocations.includes(location.id)}
                        onChange={() => handleLocationToggle(location.id)}
                        data-testid={`restrict-location-${location.id}`}
                      />
                      {' '}{location.name}
                    </label>
                  </li>
                ))}
              </ul>
            )}
          </fieldset>
        </div>

        {/* Form actions */}
        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/admin/resources')}
            disabled={submitting}
            className="btn btn--secondary"
            data-testid="resource-form-cancel"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="btn btn--primary"
            data-testid="resource-form-submit"
          >
            {submitting ? 'Saving…' : 'Save Resource'}
          </button>
        </div>
      </form>
    </div>
  );
}
