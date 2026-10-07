import { Link } from 'react-router-dom';

export function Nav() {
  return (
    <nav style={{ 
      padding: '16px', 
      background: '#f0f0f0', 
      borderBottom: '1px solid #ccc',
      display: 'flex',
      gap: '20px',
      flexWrap: 'wrap'
    }}>
      <div style={{ display: 'flex', gap: '12px', flexDirection: 'column' }}>
        <strong>Bookings</strong>
        <div style={{ display: 'flex', gap: '12px' }}>
          <Link to="/calendar">Calendar</Link>
          <Link to="/list">List</Link>
          <Link to="/day">Day</Link>
        </div>
      </div>
      
      <div style={{ display: 'flex', gap: '12px', flexDirection: 'column' }}>
        <strong>Admin</strong>
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <Link to="/admin/locations">Locations</Link>
          <Link to="/admin/resources">Resources</Link>
          <Link to="/admin/custom-fields">Custom Fields</Link>
          <Link to="/admin/users">Users</Link>
          <Link to="/admin/roles">Roles</Link>
          <Link to="/admin/settings">Settings</Link>
        </div>
      </div>
      
      <div style={{ display: 'flex', gap: '12px', flexDirection: 'column' }}>
        <strong>Other</strong>
        <div style={{ display: 'flex', gap: '12px' }}>
          <Link to="/audit-log">Audit Log</Link>
          <Link to="/feeds">Public Feeds</Link>
        </div>
      </div>
    </nav>
  );
}
