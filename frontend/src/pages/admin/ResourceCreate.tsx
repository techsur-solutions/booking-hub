import { ResourceForm } from '../../components/admin/ResourceForm';

export function ResourceCreate() {
  return (
    <div className="resource-create" data-testid="resource-create">
      <div className="admin-page-header">
        <h1>Create Resource</h1>
      </div>
      <ResourceForm />
    </div>
  );
}
