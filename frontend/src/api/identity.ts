import { api } from './client'
import type { ApiResponse, PageResponse, RoleSummary, TenantSummary, UserSummary } from './types'

export async function fetchUsers(): Promise<ApiResponse<PageResponse<UserSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<UserSummary>>>('/users?size=50')
  return response.data
}

export async function fetchRoles(): Promise<ApiResponse<RoleSummary[]>> {
  const response = await api.get<ApiResponse<RoleSummary[]>>('/roles')
  return response.data
}

export async function fetchTenants(): Promise<ApiResponse<PageResponse<TenantSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<TenantSummary>>>('/tenants?size=50')
  return response.data
}
