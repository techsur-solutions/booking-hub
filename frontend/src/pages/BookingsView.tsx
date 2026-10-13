import { useSearchParams } from 'react-router-dom';
import { DayView } from './DayView';

type ViewMode = 'month' | 'day' | 'list';

const VIEW_LABELS: Record<ViewMode, string> = {
  month: 'Calendar',
  day: 'Day',
  list: 'List',
};

export function BookingsView() {
  const [searchParams, setSearchParams] = useSearchParams();
  const view = (searchParams.get('view') as ViewMode) ?? 'month';

  function switchView(newView: ViewMode) {
    setSearchParams({ view: newView });
  }

  return (
    <div className="bookings-workspace">
      {/* View switcher tabs */}
      <div className="bookings-tabs" role="tablist" aria-label="Booking view">
        {(['month', 'day', 'list'] as ViewMode[]).map((v) => (
          <button
            key={v}
            role="tab"
            aria-selected={view === v}
            className={`bookings-tab${view === v ? ' bookings-tab--active' : ''}`}
            onClick={() => switchView(v)}
          >
            {VIEW_LABELS[v]}
          </button>
        ))}
      </div>

      {/* View content */}
      <div className="bookings-content" role="tabpanel">
        {view === 'month' && (
          <div>
            <h1>Calendar View</h1>
            <p>Month calendar — built in Phase 9 (calendar views plan)</p>
          </div>
        )}
        {view === 'day' && <DayView />}
        {view === 'list' && (
          <div>
            <h1>List View</h1>
            <p>Booking list — built in Phase 9 (calendar views plan)</p>
          </div>
        )}
      </div>
    </div>
  );
}
