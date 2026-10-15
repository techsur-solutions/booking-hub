import { useState, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { customFieldsApi } from '../../api/customFieldsApi';
import type { CustomField } from '../../api/types';
import { StateConfirmationBanner } from '../StateConfirmationBanner';
import { CustomFieldForm } from './CustomFieldForm';

type FormMode = 'none' | 'create' | 'edit';

export function CustomFieldDefinitionsTab() {
  const queryClient = useQueryClient();
  const [formMode, setFormMode] = useState<FormMode>('none');
  const [editingField, setEditingField] = useState<CustomField | null>(null);
  const [banner, setBanner] = useState<{ action: string; entity: string; timestamp: Date } | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const { data: fields, isLoading, isError } = useQuery({
    queryKey: ['custom-fields'],
    queryFn: () => customFieldsApi.getDefinitions().then(r => r.data),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => customFieldsApi.deleteDefinition(id),
    onSuccess: (_data, id) => {
      setDeleteError(null);
      queryClient.invalidateQueries({ queryKey: ['custom-fields'] });
      const deleted = fields?.find(f => f.id === id);
      setBanner({
        action: 'Field deleted',
        entity: deleted?.name ?? '',
        timestamp: new Date(),
      });
    },
    onError: () => {
      setDeleteError('Failed to delete field definition. Please try again.');
    },
  });

  const handleSaved = useCallback((field: CustomField, verb: 'created' | 'updated') => {
    queryClient.invalidateQueries({ queryKey: ['custom-fields'] });
    setBanner({
      action: verb === 'created' ? 'Field definition created' : 'Field definition updated',
      entity: field.name,
      timestamp: new Date(),
    });
    setFormMode('none');
    setEditingField(null);
  }, [queryClient]);

  const handleEdit = (field: CustomField) => {
    setEditingField(field);
    setFormMode('edit');
  };

  const handleDelete = (id: string) => {
    if (window.confirm('Delete this field definition?')) {
      deleteMutation.mutate(id);
    }
  };

  const optionsPreview = (field: CustomField): string => {
    if (field.options && field.options.length > 0) {
      return field.options.join(', ');
    }
    return 'N/A';
  };

  return (
    <div className="custom-field-definitions-tab" data-testid="custom-field-definitions-tab">
      {banner && (
        <StateConfirmationBanner
          action={banner.action}
          entity={banner.entity}
          timestamp={banner.timestamp}
          onDismiss={() => setBanner(null)}
        />
      )}

      <div className="tab-header">
        <h2>Field Definitions</h2>
        {formMode === 'none' && (
          <button
            type="button"
            className="btn btn--primary"
            data-testid="create-field-definition-btn"
            onClick={() => {
              setEditingField(null);
              setFormMode('create');
            }}
          >
            Create Field Definition
          </button>
        )}
      </div>

      {formMode !== 'none' && (
        <div className="form-panel" data-testid="field-definition-form-panel">
          <CustomFieldForm
            mode={formMode}
            initialData={editingField ?? undefined}
            onSaved={handleSaved}
            onCancel={() => {
              setFormMode('none');
              setEditingField(null);
            }}
          />
        </div>
      )}

      {deleteError && (
        <div className="error" role="alert" data-testid="delete-field-error">{deleteError}</div>
      )}

      {isLoading && <p>Loading field definitions…</p>}
      {isError && <p className="error">Failed to load field definitions.</p>}

      {!isLoading && !isError && (
        <table className="admin-table" data-testid="field-definitions-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Field Type</th>
              <th>Options</th>
              <th>Required</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {fields && fields.length === 0 && (
              <tr>
                <td colSpan={5} className="empty-row">No field definitions yet.</td>
              </tr>
            )}
            {fields?.map(field => (
              <tr key={field.id} data-testid={`field-row-${field.id}`}>
                <td data-testid={`field-name-${field.id}`}>{field.name}</td>
                <td data-testid={`field-type-${field.id}`}>{field.field_type}</td>
                <td data-testid={`field-options-${field.id}`}>{optionsPreview(field)}</td>
                <td>{field.required ? 'Yes' : 'No'}</td>
                <td className="actions-cell">
                  <button
                    type="button"
                    className="btn btn--sm btn--secondary"
                    data-testid={`edit-field-${field.id}`}
                    onClick={() => handleEdit(field)}
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    className="btn btn--sm btn--danger"
                    data-testid={`delete-field-${field.id}`}
                    onClick={() => handleDelete(field.id)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
