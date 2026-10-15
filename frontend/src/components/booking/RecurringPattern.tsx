export interface RecurrencePattern {
  type: 'daily' | 'weekly';
  until: string; // ISO date string YYYY-MM-DD
}

interface RecurringPatternProps {
  /** Minimum date for "Until" picker — should be start_time date */
  startDate?: string;
  value: RecurrencePattern;
  onChange: (pattern: RecurrencePattern) => void;
  error?: string;
}

/**
 * Recurring pattern selector component.
 * Renders repeat type (daily/weekly) and an "until" date picker.
 * Shown when user checks "Make this a recurring booking".
 */
export function RecurringPattern({ startDate, value, onChange, error }: RecurringPatternProps) {
  function handleTypeChange(type: 'daily' | 'weekly') {
    onChange({ ...value, type });
  }

  function handleUntilChange(until: string) {
    onChange({ ...value, until });
  }

  return (
    <div className="recurring-pattern" data-testid="recurring-pattern">
      <div className="form-field">
        <label htmlFor="recurring-repeat-type">Repeat</label>
        <select
          id="recurring-repeat-type"
          className="form-select"
          value={value.type}
          onChange={(e) => handleTypeChange(e.target.value as 'daily' | 'weekly')}
          data-testid="recurring-type"
        >
          <option value="daily">Daily</option>
          <option value="weekly">Weekly</option>
        </select>
      </div>

      <div className="form-field">
        <label htmlFor="recurring-until-date">
          Until
          <span className="required-mark" aria-hidden="true">*</span>
        </label>
        <input
          id="recurring-until-date"
          type="date"
          className={`form-input${error ? ' form-input--error' : ''}`}
          value={value.until}
          min={startDate}
          onChange={(e) => handleUntilChange(e.target.value)}
          required
          aria-required="true"
          data-testid="recurring-until"
        />
        {error && (
          <span className="form-error" role="alert">{error}</span>
        )}
      </div>
    </div>
  );
}
