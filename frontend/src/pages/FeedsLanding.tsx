import { Link } from 'react-router-dom';

export function FeedsLanding() {
  return (
    <div>
      <h1>Public Feeds Landing</h1>
      <p>RSS2 / iCal / JSON feed links and JSON widget — built in Phase 7</p>
      <p><Link to="/display-board">View Display Board</Link></p>
    </div>
  );
}
