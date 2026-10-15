// RolesAdmin is now the Role & Permission Matrix (UI-ADMIN-05).
// The route /admin/roles renders RolePermissionMatrix directly.
// This re-export keeps the routes/index.tsx import working without change.
export { RolePermissionMatrix as RolesAdmin } from './RolePermissionMatrix';
