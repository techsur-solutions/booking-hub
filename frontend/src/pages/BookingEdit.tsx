import { useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useBookingById } from '../hooks/useBookings';
import { bookingsApi } from '../api/bookings';
import { RecurringScopeDialog } from '../components/booking/RecurringScopeDialog';
import type { RecurringScope } from '../components/booking/RecurringScopeDialog';
import { BookingForm } from '../components/booking/BookingForm';
import type { BookingFormValues } from '../components/booking/BookingForm';
// ConflictWarning is used inside BookingForm for re-validation on every edit
import { StateConfirmationBanner } from '../components/StateConfirmationBanner';
import { useToast } from '../components/Toast';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Badge } from '../components/Badge';

/**
 * BookingEdit page — /bookings/:id/edit
 *
 * Flow:
 * 1. Load booking by ID from API
 * 2. If booking.series_id !== null: show RecurringScopeDialog (non-dismissible)
 *    User selects 'this-only' | 'entire-series'
 * 3. Render BookingForm with mode='edit' and initialValues from booking
 * 4. On save: PATCH /bookings/:id?scope={scope} with updated fields
 * 5. Show StateConfirmationBanner with scope-specific message
 * 6. Redirect to Calendar View with date=booking.start_time.split('T')[0]
 */
export function BookingEdit() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();

  // Scope state — null means not yet selected (dialog visible for recurring)
  // scope values: 'this-only' | 'entire-series' (see RecurringScopeDialog)
  const [scope, setScope] = useState<RecurringScope | null>(null);

  // Post-save confirmation banner
  const [confirmationBanner, setConfirmationBanner] = useState<{
    action: string;
    timestamp: Date;
  } | null>(null);

  const [isSaving, setIsSaving] = useState(false);

  const { data: booking, isLoading, error } = useBookingById(id ?? '');

  const handleScopeSelect = useCallback((selectedScope: RecurringScope) => {
    setScope(selectedScope);
  }, []);

  const handleSubmit = useCallback(
    async (values: BookingFormValues) => {
      if (!id || !booking) return;

      setIsSaving(true);
      try {
        // Build PATCH URL with scope query param for recurring bookings
        const patchData: Record<string, unknown> = {
          title: values.title,
          location_id: values.location_id,
          start_time: values.start_time,
          end_time: values.end_time,
          resource_ids: values.resource_ids,
          description: values.description || null,
          custom_field_values: Object.entries(values.custom_field_values).map(
            ([field_id, value]) => ({ field_id, value })
          ),
        };

        // Direct API call with scope query param
        const response = await bookingsApi.patchBooking(id, patchData, scope ?? undefined);
        const savedBooking: { recurrence_updated_count?: number } = response.data;

        // Determine confirmation message based on scope and response
        let actionMessage: string;
        if (booking.series_id === null) {
          // Non-recurring
          actionMessage = 'Booking updated';
        } else if (scope === 'this-only') {
          // StateConfirmationBanner shows: "Only this occurrence changed at {timestamp}"
          actionMessage = 'Only this occurrence changed';
        } else {
          // entire-series
          const count = savedBooking.recurrence_updated_count;
          if (count && count > 1) {
            actionMessage = `Entire series updated (${count} occurrences)`;
          } else {
            actionMessage = 'Only this occurrence changed (series already modified)';
          }
        }

        setConfirmationBanner({ action: actionMessage, timestamp: new Date() });

        // Redirect to Calendar View with date from start_time after brief pause
        setTimeout(() => {
          const date = values.start_time.split('T')[0];
          navigate(`/bookings?view=month&date=${date}`);
        }, 2500);
      } catch (err: unknown) {
        const status = (err as { response?: { status?: number } })?.response?.status;
        if (status === 400) {
          showToast('Validation error. Please check your inputs.', { variant: 'danger' });
        } else if (status === 409) {
          showToast('Conflict detected. Please refresh and try again.', { variant: 'danger' });
        } else if (status === 404) {
          showToast('Booking not found.', { variant: 'danger' });
          navigate('/bookings?view=month');
        } else {
          showToast('Failed to update booking. Please try again.', { variant: 'danger' });
        }
      } finally {
        setIsSaving(false);
      }
    },
    [id, booking, scope, navigate, showToast]
  );

  if (!id) {
    return <div className="page-error">Invalid booking ID.</div>;
  }

  if (isLoading) {
    return (
      <div className="page-loading">
        <LoadingSpinner />
        <span>Loading booking…</span>
      </div>
    );
  }

  if (error || !booking) {
    return (
      <div className="page-error">
        <p>Booking not found.</p>
        <button type="button" onClick={() => navigate('/bookings?view=month')}>
          Back to Calendar
        </button>
      </div>
    );
  }

  // Recurring: show scope dialog until user selects scope
  const isRecurring = booking.series_id !== null;
  const scopeDialogVisible = isRecurring && scope === null;

  // Build initial values from booking for the form
  const initialValues: Partial<BookingFormValues> = {
    title: booking.title,
    location_id: booking.location_id,
    start_time: booking.start_time,
    end_time: booking.end_time,
    resource_ids: booking.resource_ids,
    description: booking.description ?? '',
    custom_field_values: Object.fromEntries(
      booking.custom_field_values.map((cfv) => [cfv.field_id, cfv.value])
    ),
  };

  return (
    <div className="booking-edit-page" data-testid="booking-edit-page">
      {/* Non-dismissible scope dialog for recurring bookings */}
      {scopeDialogVisible && (
        <RecurringScopeDialog onScopeSelect={handleScopeSelect} />
      )}

      <div className="booking-edit-page__header">
        <h1>
          Edit Booking
          {isRecurring && (
            <Badge variant="primary" className="recurring-badge" data-testid="recurring-badge">
              Recurring
            </Badge>
          )}
        </h1>
        {scope && (
          <p className="booking-edit-page__scope-note">
            Editing: <strong>{scope === 'this-only' ? 'This occurrence only' : 'Entire series'}</strong>
          </p>
        )}
      </div>

      {/* Post-save confirmation banner */}
      {confirmationBanner && (
        <StateConfirmationBanner
          action={confirmationBanner.action}
          timestamp={confirmationBanner.timestamp}
          variant="success"
          onDismiss={() => setConfirmationBanner(null)}
          data-testid="edit-confirmation-banner"
        />
      )}

      {/* Show form only after scope is selected (or for non-recurring) */}
      {(!isRecurring || scope !== null) && (
        <BookingForm
          mode="edit"
          initialValues={initialValues}
          bookingId={id}
          isLoading={isSaving}
          onSubmit={handleSubmit}
          onCancel={() => navigate('/bookings?view=month')}
        />
      )}
    </div>
  );
}
