import { api } from './client'
import type {
  ApiResponse,
  AuditorSummary,
  AvailabilitySummary,
  CompetencySummary,
  EligibilitySummary,
  PageResponse,
  QualificationSummary,
} from './types'

export async function fetchAuditors(query?: string): Promise<ApiResponse<PageResponse<AuditorSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  if (query) {
    params.set('q', query)
  }
  const response = await api.get<ApiResponse<PageResponse<AuditorSummary>>>(`/auditors?${params.toString()}`)
  return response.data
}

export async function fetchAuditor(id: string): Promise<ApiResponse<AuditorSummary>> {
  const response = await api.get<ApiResponse<AuditorSummary>>(`/auditors/${id}`)
  return response.data
}

export async function createAuditor(body: {
  firstName: string
  lastName: string
  email?: string
  jobTitle?: string
  employmentType?: 'EMPLOYEE' | 'CONTRACTOR'
}): Promise<ApiResponse<AuditorSummary>> {
  const response = await api.post<ApiResponse<AuditorSummary>>('/auditors', body)
  return response.data
}

export async function fetchQualifications(auditorId: string): Promise<ApiResponse<QualificationSummary[]>> {
  const response = await api.get<ApiResponse<QualificationSummary[]>>(`/auditors/${auditorId}/qualifications`)
  return response.data
}

export async function createQualification(
  auditorId: string,
  body: { title: string; issuer?: string; expiresOn?: string },
): Promise<ApiResponse<QualificationSummary>> {
  const response = await api.post<ApiResponse<QualificationSummary>>(`/auditors/${auditorId}/qualifications`, body)
  return response.data
}

export async function fetchCompetencies(auditorId: string): Promise<ApiResponse<CompetencySummary[]>> {
  const response = await api.get<ApiResponse<CompetencySummary[]>>(`/auditors/${auditorId}/competencies`)
  return response.data
}

export async function createCompetency(
  auditorId: string,
  body: { standardId?: string; schemeId?: string; competencyRole?: string; validFrom: string; validTo?: string },
): Promise<ApiResponse<CompetencySummary>> {
  const response = await api.post<ApiResponse<CompetencySummary>>(`/auditors/${auditorId}/competencies`, body)
  return response.data
}

export async function fetchAvailability(auditorId: string): Promise<ApiResponse<AvailabilitySummary[]>> {
  const response = await api.get<ApiResponse<AvailabilitySummary[]>>(`/auditors/${auditorId}/availability`)
  return response.data
}

export async function createAvailability(
  auditorId: string,
  body: { startOn: string; endOn: string; kind: 'AVAILABLE' | 'UNAVAILABLE'; reason?: string },
): Promise<ApiResponse<AvailabilitySummary>> {
  const response = await api.post<ApiResponse<AvailabilitySummary>>(`/auditors/${auditorId}/availability`, body)
  return response.data
}

export async function fetchEligibility(
  auditorId: string,
  params: { standardId?: string; schemeId?: string; on?: string },
): Promise<ApiResponse<EligibilitySummary>> {
  const search = new URLSearchParams()
  if (params.standardId) {
    search.set('standardId', params.standardId)
  }
  if (params.schemeId) {
    search.set('schemeId', params.schemeId)
  }
  if (params.on) {
    search.set('on', params.on)
  }
  const response = await api.get<ApiResponse<EligibilitySummary>>(`/auditors/${auditorId}/eligibility?${search.toString()}`)
  return response.data
}
