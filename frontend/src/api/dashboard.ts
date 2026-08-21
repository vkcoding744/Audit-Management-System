import { api } from './client'
import type { ApiResponse, TenantDashboardSummary } from './types'

export async function fetchTenantDashboard(): Promise<ApiResponse<TenantDashboardSummary>> {
  const response = await api.get<ApiResponse<TenantDashboardSummary>>('/dashboard')
  return response.data
}
