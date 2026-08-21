import { api } from './client'
import type { ApiResponse, SearchResult } from './types'

export async function searchRecords(q: string, type?: string): Promise<ApiResponse<SearchResult>> {
  const response = await api.get<ApiResponse<SearchResult>>('/search', {
    params: { q, type: type || undefined },
  })
  return response.data
}
