import { useParams, Navigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import type { Resource } from '../../api/types';
import { resourcesApi } from '../../api/resourcesApi';
import { ResourceForm } from '../../components/admin/ResourceForm';

export function ResourceEdit() {
  const { id } = useParams<{ id: string }>();

  const {
    data: resource,
    isLoading,
    isError,
  } = useQuery<Resource>({
    queryKey: ['resource', id],
    queryFn: () => {
      if (!id) throw new Error('Resource ID is required');
      return resourcesApi.getById(id).then((res) => res.data);
    },
    enabled: Boolean(id),
  });

  if (!id) {
    return <Navigate to="/admin/resources" replace />;
  }

  if (isLoading) {
    return (
      <div className="resource-edit" data-testid="resource-edit-loading">
        <div className="loading-state">Loading resource…</div>
      </div>
    );
  }

  if (isError || !resource) {
    return (
      <div className="resource-edit" data-testid="resource-edit-error">
        <div className="error-state" role="alert">
          Failed to load resource. Please try again.
        </div>
      </div>
    );
  }

  return (
    <div className="resource-edit" data-testid="resource-edit">
      <div className="admin-page-header">
        <h1>Edit Resource</h1>
      </div>
      <ResourceForm initialData={resource} />
    </div>
  );
}
