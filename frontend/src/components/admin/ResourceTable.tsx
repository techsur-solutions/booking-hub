import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { Resource, Location } from '../../api/types';
import { resourcesApi } from '../../api/resourcesApi';
import { SoftDeleteDialog } from '../common/SoftDeleteDialog';

export interface ResourceTableProps {
  resources: Resource[];
  locations: Location[];
  onRefresh: () => void;
}

export function ResourceTable({ resources, locations, onRefresh }: ResourceTableProps) {
  const navigate = useNavigate();
  const [deleteTarget, setDeleteTarget] = useState<Resource | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;
    setDeleteLoading(true);
    try {
      await resourcesApi.delete(deleteTarget.id);
      setDeleteTarget(null);
      onRefresh();
    } catch (err) {
      console.error('[ResourceTable] Failed to delete resource:', err);
    } finally {
      setDeleteLoading(false);
    }
  };

  /**
   * Resolves restrict_locations UUIDs to display names.
   * Returns "All locations" if the array is empty or null.
   */
  const resolveLocationNames = (restrictLocations: string[] | null): string => {
    if (!restrictLocations || restrictLocations.length === 0) {
      return 'All locations';
    }
    const names = restrictLocations.map((id) => {
      const loc = locations.find((l) => l.id === id);
      return loc ? loc.name : id;
    });
    return names.join(', ');
  };

  return (
    <>
      <table className="resource-table" data-testid="resource-table" aria-label="Resources">
        <thead>
          <tr>
            <th scope="col">Name</th>
            <th scope="col">Type</th>
            <th scope="col">Description</th>
            <th scope="col">Is Unique</th>
            <th scope="col">Restrict Locations</th>
            <th scope="col">Actions</th>
          </tr>
        </thead>
        <tbody>
          {resources.length === 0 ? (
            <tr>
              <td colSpan={6} className="resource-table__empty">
                No resources found. Create the first one.
              </td>
            </tr>
          ) : (
            resources.map((resource) => (
              <tr key={resource.id} data-testid={`resource-row-${resource.id}`}>
                <td data-testid={`resource-name-${resource.id}`}>{resource.name}</td>
                <td data-testid={`resource-type-${resource.id}`}>
                  {resource.type ?? '—'}
                </td>
                <td
                  data-testid={`resource-description-${resource.id}`}
                  title={resource.description ?? undefined}
                  className="resource-table__description"
                >
                  {resource.description
                    ? resource.description.length > 60
                      ? `${resource.description.slice(0, 60)}…`
                      : resource.description
                    : '—'}
                </td>
                <td data-testid={`resource-is-unique-${resource.id}`}>
                  {resource.is_unique ? (
                    <span
                      className="is-unique-icon is-unique-icon--true"
                      aria-label="Unique resource"
                      title="This resource can only be used by one booking at a time"
                      data-testid={`resource-unique-check-${resource.id}`}
                    >
                      ✓
                    </span>
                  ) : (
                    <span
                      className="is-unique-icon is-unique-icon--false"
                      aria-label="Shared resource"
                      title="This resource can be shared across multiple bookings"
                      data-testid={`resource-unique-dash-${resource.id}`}
                    >
                      —
                    </span>
                  )}
                </td>
                <td data-testid={`resource-restrict-locations-${resource.id}`}>
                  {resolveLocationNames(resource.restrict_locations)}
                </td>
                <td className="resource-table__actions">
                  <button
                    type="button"
                    className="resource-table__action-btn resource-table__action-btn--edit"
                    aria-label={`Edit ${resource.name}`}
                    data-testid={`resource-edit-${resource.id}`}
                    onClick={() => navigate(`/admin/resources/${resource.id}/edit`)}
                  >
                    ✎ Edit
                  </button>
                  <button
                    type="button"
                    className="resource-table__action-btn resource-table__action-btn--delete"
                    aria-label={`Delete ${resource.name}`}
                    data-testid={`resource-delete-${resource.id}`}
                    onClick={() => setDeleteTarget(resource)}
                  >
                    🗑 Delete
                  </button>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>

      <SoftDeleteDialog
        open={deleteTarget !== null}
        onConfirm={deleteLoading ? () => {} : handleDeleteConfirm}
        onCancel={() => setDeleteTarget(null)}
        entityName={deleteTarget?.name ?? ''}
        contextMessage="Existing bookings will keep this resource reference"
      />
    </>
  );
}
