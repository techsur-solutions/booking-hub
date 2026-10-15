import { useQuery } from '@tanstack/react-query';
import { customFieldsApi } from '../api/customFields';

/**
 * Fetches custom field templates for a given location context.
 * Returns both global fields (context_id = null) and location-specific fields.
 * When locationId is provided, fetches fields for that location.
 * When locationId is empty/undefined, returns only global fields.
 */
export function useCustomFields(locationId?: string) {
  return useQuery({
    queryKey: ['custom-fields', locationId ?? 'global'],
    queryFn: () =>
      customFieldsApi
        .getCustomFields(locationId ? { context_id: locationId } : undefined)
        .then((res) => res.data),
    enabled: true, // always fetch — global fields when no location selected
  });
}
