import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { locationsApi } from '../api/locations';
import type { Location } from '../api/types';

export function useLocations(params?: { include_deleted?: boolean }) {
  return useQuery({
    queryKey: ['locations', params],
    queryFn: () => locationsApi.getLocations(params).then((res) => res.data),
  });
}

export function useLocationById(id: string) {
  return useQuery({
    queryKey: ['locations', id],
    queryFn: () => locationsApi.getLocationById(id).then((res) => res.data),
    enabled: !!id,
  });
}

export function useCreateLocation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: Parameters<typeof locationsApi.createLocation>[0]) =>
      locationsApi.createLocation(data).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['locations'] });
    },
  });
}

export function useUpdateLocation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: string;
      data: Partial<Omit<Location, 'id' | 'deleted_at'>>;
    }) => locationsApi.updateLocation(id, data).then((res) => res.data),
    onSuccess: (_data, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['locations'] });
      queryClient.invalidateQueries({ queryKey: ['locations', id] });
    },
  });
}

export function useDeleteLocation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => locationsApi.deleteLocation(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['locations'] });
    },
  });
}
