import { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import type { Location } from '../../api/types';
import { locationsApi } from '../../api/locationsApi';
import { LocationTable } from '../../components/admin/LocationTable';

export function LocationsAdmin() {
  const [refreshKey, setRefreshKey] = useState(0);

  const {
    data: allLocations,
    isLoading,
    isError,
    refetch,
  } = useQuery<Location[]>({
    queryKey: ['locations', 'admin', refreshKey],
    queryFn: () => locationsApi.getAll().then((res) => res.data),
  });

  // Filter out soft-deleted rows for admin table (show only active)
  const activeLocations = allLocations?.filter((loc) => loc.deleted_at === null) ?? [];

  const handleRefresh = useCallback(() => {
    setRefreshKey((k) => k + 1);
    refetch();
  }, [refetch]);

  return (
    <div className="locations-admin" data-testid="locations-admin">
      <div className="locations-admin__header">
        <h1 className="locations-admin__title">Locations Admin</h1>
        <Link
          to="/admin/locations/new"
          className="locations-admin__create-btn btn btn--primary"
          data-testid="create-location-button"
        >
          + Create Location
        </Link>
      </div>

      {isLoading && (
        <div className="loading-state" data-testid="locations-loading">
          Loading locations…
        </div>
      )}

      {isError && (
        <div className="error-state" role="alert" data-testid="locations-error">
          Failed to load locations. Please try again.
        </div>
      )}

      {!isLoading && !isError && (
        <LocationTable locations={activeLocations} onRefresh={handleRefresh} />
      )}
    </div>
  );
}
