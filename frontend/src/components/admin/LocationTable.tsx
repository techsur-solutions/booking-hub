import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { Location } from '../../api/types';
import { locationsApi } from '../../api/locationsApi';
import { SoftDeleteDialog } from '../common/SoftDeleteDialog';

export interface LocationTableProps {
  locations: Location[];
  onRefresh: () => void;
}

export function LocationTable({ locations, onRefresh }: LocationTableProps) {
  const navigate = useNavigate();
  const [deleteTarget, setDeleteTarget] = useState<Location | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;
    setDeleteLoading(true);
    try {
      await locationsApi.delete(deleteTarget.id);
      setDeleteTarget(null);
      onRefresh();
    } catch (err) {
      console.error('[LocationTable] Failed to delete location:', err);
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <>
      <table className="location-table" data-testid="location-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Colour</th>
            <th>Building</th>
            <th>Layout</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {locations.length === 0 ? (
            <tr>
              <td colSpan={5} className="location-table__empty">
                No locations found. Create the first one.
              </td>
            </tr>
          ) : (
            locations.map((location) => (
              <tr key={location.id} data-testid={`location-row-${location.id}`}>
                <td data-testid={`location-name-${location.id}`}>{location.name}</td>
                <td>
                  <span
                    className="colour-swatch"
                    style={{ backgroundColor: location.colour }}
                    title={location.colour}
                    aria-label={`Colour: ${location.colour}`}
                    data-testid={`location-colour-swatch-${location.id}`}
                  />
                </td>
                <td data-testid={`location-building-${location.id}`}>
                  {location.building ?? '—'}
                </td>
                <td data-testid={`location-layout-${location.id}`}>
                  {location.layouts ?? '—'}
                </td>
                <td className="location-table__actions">
                  <button
                    type="button"
                    className="location-table__action-btn location-table__action-btn--edit"
                    aria-label={`Edit ${location.name}`}
                    data-testid={`location-edit-${location.id}`}
                    onClick={() => navigate(`/admin/locations/${location.id}/edit`)}
                  >
                    ✎ Edit
                  </button>
                  <button
                    type="button"
                    className="location-table__action-btn location-table__action-btn--delete"
                    aria-label={`Delete ${location.name}`}
                    data-testid={`location-delete-${location.id}`}
                    onClick={() => setDeleteTarget(location)}
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
        contextMessage="Existing bookings will keep this location reference"
      />
    </>
  );
}
