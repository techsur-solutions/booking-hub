export function LoadingSpinner({ message = 'Loading...' }: { message?: string }) {
  return (
    <div role="status" aria-live="polite" style={{ textAlign: 'center', padding: '2rem' }}>
      <div
        className="spinner"
        aria-hidden="true"
        style={{
          display: 'inline-block',
          width: '2rem',
          height: '2rem',
          border: '3px solid rgba(0,0,0,0.1)',
          borderTopColor: '#3b82f6',
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite',
        }}
      />
      <p style={{ marginTop: '0.5rem', color: '#6b7280' }}>{message}</p>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}
