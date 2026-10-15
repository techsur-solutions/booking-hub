import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import type { Location } from '../../api/types';
import { locationsApi } from '../../api/locationsApi';
import { LocationForm } from '../../components/admin/LocationForm';
import type { LocationFormValues } from '../../components/admin/LocationForm';
import { toFormValues } from '../../components/admin/LocationForm';
import { StateConfirmationBanner } from '../../components/StateConfirmationBanner';

export function LocationEdit() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [banner, setBanner] = useState<{ timestamp: Date } | null>(null);

  const {
    data: location,
    isLoading,
    isError,
  } = useQuery<Location>({
    queryKey: ['locations', id],
    queryFn: () => locationsApi.getById(id!).then((res) => res.data),
    enabled: !!id,
  });

  const handleSubmit = async (values: LocationFormValues) => {
    if (!id) return;

    await locationsApi.update(id, {
      name: values.name,
      class: values.class || undefined,
      colour: values.colour,
      description: values.description || undefined,
      building: values.building || undefined,
      layouts: values.layouts || undefined,
    });

    setBanner({ timestamp: new Date() });

    // Navigate back to the locations table after a short pause
    setTimeout(() => {
      navigate('/admin/locations');
    }, 1500);
  };

  if (isLoading) {
    return (
      <div className="loading-state" data-testid="location-edit-loading">
        Loading location…
      </div>
    );
  }

  if (isError || !location) {
    return (
      <div className="error-state" role="alert" data-testid="location-edit-error">
        Failed to load location. Please go back and try again.
      </div>
    );
  }

  return (
    <div className="location-edit" data-testid="location-edit">
      <div className="page-header">
        <h1>Edit Location: {location.name}</h1>
      </div>

      {banner && (
        <StateConfirmationBanner
          action="Location saved"
          timestamp={banner.timestamp}
          onDismiss={() => setBanner(null)}
        />
      )}

      <LocationForm
        initialValues={toFormValues(location)}
        onSubmit={handleSubmit}
        submitLabel="Save Location"
      />
    </div>
  );
}
