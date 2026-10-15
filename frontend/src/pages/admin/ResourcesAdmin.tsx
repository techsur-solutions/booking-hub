import { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import type { Resource, Location } from '../../api/types';
import { resourcesApi } from '../../api/resourcesApi';
import { locationsApi } from '../../api/locationsApi';
import { ResourceTable } from '../../components/admin/ResourceTable';

export function ResourcesAdmin() {
  const [refreshKey, setRefreshKey] = useState(0);

  const {
    data: allResources,
    isLoading: resourcesLoading,
    isError: resourcesError,
    refetch,
  } = useQuery<Resource[]>({
    queryKey: ['resources', 'admin', refreshKey],
    queryFn: () => resourcesApi.getAll().then((res) => res.data),
  });

  const { data: allLocations } = useQuery<Location[]>({
    queryKey: ['locations', 'admin-ref'],
    queryFn: () => locationsApi.getAll().then((res) => res.data),
  });

  // Filter out soft-deleted rows for admin table (show only active)
  const activeResources = allResources?.filter((r) => r.deleted_at === null) ?? [];

  // Only show active locations for resolve of restrict_locations UUIDs
  const activeLocations = allLocations?.filter((l) => l.deleted_at === null) ?? [];

  const handleRefresh = useCallback(() => {
    setRefreshKey((k) => k + 1);
    refetch();
  }, [refetch]);

  return (
    <div className="resources-admin" data-testid="resources-admin">
      <div className="resources-admin__header">
        <h1 className="resources-admin__title">Resources Admin</h1>
        <Link
          to="/admin/resources/new"
          className="resources-admin__create-btn btn btn--primary"
          data-testid="create-resource-button"
        >
          + Create Resource
        </Link>
      </div>

      {resourcesLoading && (
        <div className="loading-state" data-testid="resources-loading">
          Loading resources…
        </div>
      )}

      {resourcesError && (
        <div className="error-state" role="alert" data-testid="resources-error">
          Failed to load resources. Please try again.
        </div>
      )}

      {!resourcesLoading && !resourcesError && (
        <ResourceTable
          resources={activeResources}
          locations={activeLocations}
          onRefresh={handleRefresh}
        />
      )}
    </div>
  );
}
