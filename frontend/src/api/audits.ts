import { api } from './client'
import type {
  ApiResponse,
  AssignmentSummary,
  AuditSiteSummary,
  AuditStatus,
  AuditSummary,
  PageResponse,
  ProgrammeSummary,
} from './types'

export async function fetchProgrammes(): Promise<ApiResponse<PageResponse<ProgrammeSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  const response = await api.get<ApiResponse<PageResponse<ProgrammeSummary>>>(`/programmes?${params.toString()}`)
  return response.data
}

export async function fetchProgramme(id: string): Promise<ApiResponse<ProgrammeSummary>> {
  const response = await api.get<ApiResponse<ProgrammeSummary>>(`/programmes/${id}`)
  return response.data
}

export async function createProgramme(body: {
  clientId: string
  schemeId: string
  standardId?: string
  name: string
  cycleStartOn?: string
  cycleEndOn?: string
  notes?: string
}): Promise<ApiResponse<ProgrammeSummary>> {
  const response = await api.post<ApiResponse<ProgrammeSummary>>('/programmes', body)
  return response.data
}

export async function setProgrammeStatus(
  id: string,
  action: 'activate' | 'complete' | 'cancel',
): Promise<ApiResponse<ProgrammeSummary>> {
  const response = await api.post<ApiResponse<ProgrammeSummary>>(`/programmes/${id}/${action}`)
  return response.data
}

export async function fetchProgrammeAudits(programmeId: string): Promise<ApiResponse<AuditSummary[]>> {
  const response = await api.get<ApiResponse<AuditSummary[]>>(`/programmes/${programmeId}/audits`)
  return response.data
}

export async function fetchAudits(status?: AuditStatus): Promise<ApiResponse<PageResponse<AuditSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  if (status) {
    params.set('status', status)
  }
  const response = await api.get<ApiResponse<PageResponse<AuditSummary>>>(`/audits?${params.toString()}`)
  return response.data
}

export async function fetchAudit(id: string): Promise<ApiResponse<AuditSummary>> {
  const response = await api.get<ApiResponse<AuditSummary>>(`/audits/${id}`)
  return response.data
}

export async function createAudit(body: {
  programmeId: string
  name: string
  auditType?: string
  stage?: string
  checklistId?: string
  plannedStartOn?: string
  plannedEndOn?: string
  notes?: string
}): Promise<ApiResponse<AuditSummary>> {
  const response = await api.post<ApiResponse<AuditSummary>>('/audits', body)
  return response.data
}

export async function scheduleAudit(id: string): Promise<ApiResponse<AuditSummary>> {
  const response = await api.post<ApiResponse<AuditSummary>>(`/audits/${id}/schedule`)
  return response.data
}

export async function cancelAudit(id: string): Promise<ApiResponse<AuditSummary>> {
  const response = await api.post<ApiResponse<AuditSummary>>(`/audits/${id}/cancel`)
  return response.data
}

export async function addAuditSite(auditId: string, siteId: string): Promise<ApiResponse<AuditSiteSummary>> {
  const response = await api.post<ApiResponse<AuditSiteSummary>>(`/audits/${auditId}/sites`, { siteId })
  return response.data
}

export async function assignAuditor(
  auditId: string,
  body: { auditorId: string; assignmentRole?: string },
): Promise<ApiResponse<AssignmentSummary>> {
  const response = await api.post<ApiResponse<AssignmentSummary>>(`/audits/${auditId}/assignments`, body)
  return response.data
}
