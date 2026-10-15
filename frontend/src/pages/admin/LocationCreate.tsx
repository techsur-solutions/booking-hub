import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LocationForm } from '../../components/admin/LocationForm';
import type { LocationFormValues } from '../../components/admin/LocationForm';
import { locationsApi } from '../../api/locationsApi';
import { StateConfirmationBanner } from '../../components/StateConfirmationBanner';

export function LocationCreate() {
  const navigate = useNavigate();
  const [banner, setBanner] = useState<{ timestamp: Date } | null>(null);

  const handleSubmit = async (values: LocationFormValues) => {
    await locationsApi.create({
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

  return (
    <div className="location-create" data-testid="location-create">
      <div className="page-header">
        <h1>Create Location</h1>
      </div>

      {banner && (
        <StateConfirmationBanner
          action="Location saved"
          timestamp={banner.timestamp}
          onDismiss={() => setBanner(null)}
          data-testid="state-confirmation-banner"
        />
      )}

      <LocationForm onSubmit={handleSubmit} submitLabel="Save Location" />
    </div>
  );
}
