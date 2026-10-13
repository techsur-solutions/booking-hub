import { useState, useMemo } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { BookingForm } from '../components/booking/BookingForm';
import type { BookingFormValues, BookingFormErrors } from '../components/booking/BookingForm';
import { ConflictWarning } from '../components/booking/ConflictWarning';
import { StateConfirmationBanner } from '../components/StateConfirmationBanner';
import { useLocations } from '../hooks/useLocations';
import { useResources } from '../hooks/useResources';
import { bookingsApi } from '../api/bookings';
import { useToast } from '../components/Toast';

/**
 * Booking Create Page
 *
 * Reads pre-fill params from URL:
 *   ?location_id=<uuid>&start_time=YYYY-MM-DDTHH:mm&end_time=YYYY-MM-DDTHH:mm
 *
 * Renders BookingForm with all fields plus ConflictWarning, handles save flow
 * including StateConfirmationBanner with status (approved/pending) and redirect
 * to Calendar View with date=start_time date.
 */
export function BookingCreate() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { showToast } = useToast();

  // ── Pre-fill from URL params ──────────────────────────────────────────────────
  const prefillLocationId = searchParams.get('location_id') ?? '';
  const prefillStartTime = searchParams.get('start_time') ?? '';
  const prefillEndTime = (() => {
    const end = searchParams.get('end_time');
    if (end) return end;
    if (prefillStartTime) {
      // Default end time = start + 1 hour
      const start = new Date(prefillStartTime);
      start.setHours(start.getHours() + 1);
      return toDatetimeLocal(start);
    }
    return '';
  })();

  const initialValues = useMemo<Partial<BookingFormValues>>(
    () => ({
      location_id: prefillLocationId,
      start_time: prefillStartTime,
      end_time: prefillEndTime,
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );

  // ── Data ──────────────────────────────────────────────────────────────────────
  const { data: locations = [] } = useLocations();
  const { data: resources = [] } = useResources();

  // ── State ─────────────────────────────────────────────────────────────────────
  const [formValues, setFormValues] = useState<BookingFormValues>({
    title: '',
    location_id: prefillLocationId,
    start_time: prefillStartTime,
    end_time: prefillEndTime,
    resource_ids: [],
    description: '',
    custom_field_values: {},
    recurrence_pattern: null,
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [saveDisabled, setSaveDisabled] = useState(false);
  const [confirmationBanner, setConfirmationBanner] = useState<{
    action: string;
    variant: 'success' | 'warning';
    timestamp: Date;
  } | null>(null);
  const [externalErrors, setExternalErrors] = useState<BookingFormErrors>({});

  // ── Save handler ──────────────────────────────────────────────────────────────
  async function handleSubmit(values: BookingFormValues) {
    setIsSubmitting(true);
    setExternalErrors({});
    try {
      // Build the POST /bookings payload from TechArch §4.2
      const payload = {
        title: values.title,
        location_id: values.location_id,
        start_time: toIso(values.start_time),
        end_time: toIso(values.end_time),
        resource_ids: values.resource_ids,
        description: values.description || undefined,
        custom_field_values: Object.entries(values.custom_field_values)
          .filter(([, v]) => v !== '')
          .map(([field_definition_id, value]) => ({ field_definition_id, value })),
        recurrence_pattern: values.recurrence_pattern ?? undefined,
      };

      const response = await bookingsApi.createBooking(payload as Parameters<typeof bookingsApi.createBooking>[0]);
      const booking = response.data;

      // Show status confirmation banner
      const isApproved = booking.status === 'approved';
      setConfirmationBanner({
        action: isApproved
          ? 'Booking approved'
          : 'Booking submitted for approval',
        variant: isApproved ? 'success' : 'warning',
        timestamp: new Date(),
      });

      // Redirect to Calendar View with date=booking.start_time date after a short delay
      const date = booking.start_time.split('T')[0];
      setTimeout(() => {
        navigate(`/bookings?view=month&date=${date}`);
      }, 2000);
    } catch (err: unknown) {
      const error = err as { response?: { status?: number; data?: unknown } };
      if (error.response?.status === 400) {
        // Show inline validation errors
        const data = error.response?.data as Record<string, string> | undefined;
        if (data && typeof data === 'object') {
          setExternalErrors(data);
        } else {
          showToast('Validation error. Please check the form.', { variant: 'danger' });
        }
      } else if (error.response?.status === 409) {
        showToast('Conflict detected. Please refresh and try again.', { variant: 'danger' });
      } else {
        showToast('Failed to create booking. Please try again.', { variant: 'danger' });
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  // ── Render ────────────────────────────────────────────────────────────────────
  return (
    <div className="booking-create" data-testid="booking-create">
      <h1 className="booking-create__title">New Booking</h1>

      {confirmationBanner && (
        <StateConfirmationBanner
          action={confirmationBanner.action}
          timestamp={confirmationBanner.timestamp}
          variant={confirmationBanner.variant}
          onDismiss={() => setConfirmationBanner(null)}
        />
      )}

      <BookingForm
        initialValues={initialValues}
        locations={locations}
        resources={resources}
        onSubmit={handleSubmit}
        onFormChange={setFormValues}
        mode="create"
        isSubmitting={isSubmitting}
        submitDisabled={saveDisabled}
        externalErrors={externalErrors}
        belowFields={
          <ConflictWarning
            locationId={formValues.location_id}
            startTime={formValues.start_time}
            endTime={formValues.end_time}
            resourceIds={formValues.resource_ids}
            onBlockChange={setSaveDisabled}
          />
        }
      />
    </div>
  );
}

// ── Utilities ─────────────────────────────────────────────────────────────────

/** Convert datetime-local (YYYY-MM-DDTHH:mm) to ISO-8601 with seconds */
function toIso(dtLocal: string): string {
  if (!dtLocal) return '';
  return dtLocal.length === 16 ? `${dtLocal}:00` : dtLocal;
}

function toDatetimeLocal(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}`
  );
}
