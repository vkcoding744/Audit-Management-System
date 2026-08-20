import { api } from './client'
import type {
  ApiResponse,
  CapaSummary,
  FindingSeverity,
  FindingStatus,
  FindingSummary,
  PageResponse,
} from './types'

export async function fetchFindings(status?: FindingStatus): Promise<ApiResponse<PageResponse<FindingSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  if (status) {
    params.set('status', status)
  }
  const response = await api.get<ApiResponse<PageResponse<FindingSummary>>>(`/findings?${params.toString()}`)
  return response.data
}

export async function fetchFinding(id: string): Promise<ApiResponse<FindingSummary>> {
  const response = await api.get<ApiResponse<FindingSummary>>(`/findings/${id}`)
  return response.data
}

export async function fetchAuditFindings(auditId: string): Promise<ApiResponse<FindingSummary[]>> {
  const response = await api.get<ApiResponse<FindingSummary[]>>(`/audits/${auditId}/findings`)
  return response.data
}

export async function createFinding(body: {
  auditId: string
  title: string
  description: string
  severity?: FindingSeverity
  responseId?: string
  notes?: string
}): Promise<ApiResponse<FindingSummary>> {
  const response = await api.post<ApiResponse<FindingSummary>>('/findings', body)
  return response.data
}

export async function closeFinding(id: string): Promise<ApiResponse<FindingSummary>> {
  const response = await api.post<ApiResponse<FindingSummary>>(`/findings/${id}/close`)
  return response.data
}

export async function createCapa(
  findingId: string,
  body: { description: string; dueOn: string; notes?: string },
): Promise<ApiResponse<CapaSummary>> {
  const response = await api.post<ApiResponse<CapaSummary>>(`/findings/${findingId}/capa`, body)
  return response.data
}

export async function completeCapa(capaId: string): Promise<ApiResponse<CapaSummary>> {
  const response = await api.post<ApiResponse<CapaSummary>>(`/capa/${capaId}/complete`)
  return response.data
}
