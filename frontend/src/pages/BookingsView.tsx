import { useSearchParams } from 'react-router-dom';
import { CalendarView } from './CalendarView';
import { DayView } from './DayView';
import { ListView } from './ListView';

type ViewMode = 'month' | 'day' | 'list';

const VIEW_LABELS: Record<ViewMode, string> = {
  month: 'Calendar',
  day: 'Day',
  list: 'List',
};

export function BookingsView() {
  const [searchParams, setSearchParams] = useSearchParams();
  const view = (searchParams.get('view') as ViewMode) ?? 'month';

  /** Switch view while preserving date, location, and filter params (UI-CAL-04) */
  function switchView(newView: ViewMode) {
    const current: Record<string, string> = Object.fromEntries(searchParams.entries());
    current['view'] = newView;
    setSearchParams(current);
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
        {view === 'month' && <CalendarView />}
        {view === 'day' && <DayView />}
        {view === 'list' && <ListView />}
      </div>
    </div>
  );
}
