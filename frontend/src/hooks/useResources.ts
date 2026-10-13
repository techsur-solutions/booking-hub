import { useQuery } from '@tanstack/react-query';
import { resourcesApi } from '../api/resources';

export function useResources(params?: { include_deleted?: boolean }) {
  return useQuery({
    queryKey: ['resources', params],
    queryFn: () => resourcesApi.getResources(params).then((res) => res.data),
  });
}
