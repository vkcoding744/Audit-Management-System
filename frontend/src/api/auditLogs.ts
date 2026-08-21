import { api } from './client'
import type { ApiResponse, AuditLogSummary, PageResponse } from './types'

export async function fetchAuditLogs(): Promise<ApiResponse<PageResponse<AuditLogSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<AuditLogSummary>>>('/audit-logs?size=50')
  return response.data
}
