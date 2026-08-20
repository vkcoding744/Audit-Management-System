import { api } from './client'
import type {
  ApiResponse,
  AppealOutcome,
  AppealSummary,
  ComplaintSummary,
  ImpartialitySummary,
  PageResponse,
  RiskSummary,
} from './types'

export async function fetchComplaints(): Promise<ApiResponse<PageResponse<ComplaintSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<ComplaintSummary>>>('/complaints?size=50')
  return response.data
}

export async function fetchComplaint(id: string): Promise<ApiResponse<ComplaintSummary>> {
  const response = await api.get<ApiResponse<ComplaintSummary>>(`/complaints/${id}`)
  return response.data
}

export async function createComplaint(body: {
  clientId?: string
  subject: string
  source?: string
}): Promise<ApiResponse<ComplaintSummary>> {
  const response = await api.post<ApiResponse<ComplaintSummary>>('/complaints', body)
  return response.data
}

export async function reviewComplaint(id: string): Promise<ApiResponse<ComplaintSummary>> {
  const response = await api.post<ApiResponse<ComplaintSummary>>(`/complaints/${id}/review`)
  return response.data
}

export async function closeComplaint(id: string, resolution: string): Promise<ApiResponse<ComplaintSummary>> {
  const response = await api.post<ApiResponse<ComplaintSummary>>(`/complaints/${id}/close`, { resolution })
  return response.data
}

export async function fetchAppeals(): Promise<ApiResponse<PageResponse<AppealSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<AppealSummary>>>('/appeals?size=50')
  return response.data
}

export async function fetchAppeal(id: string): Promise<ApiResponse<AppealSummary>> {
  const response = await api.get<ApiResponse<AppealSummary>>(`/appeals/${id}`)
  return response.data
}

export async function createAppeal(body: { clientId?: string; subject: string }): Promise<ApiResponse<AppealSummary>> {
  const response = await api.post<ApiResponse<AppealSummary>>('/appeals', body)
  return response.data
}

export async function reviewAppeal(id: string): Promise<ApiResponse<AppealSummary>> {
  const response = await api.post<ApiResponse<AppealSummary>>(`/appeals/${id}/review`)
  return response.data
}

export async function decideAppeal(id: string, outcome: AppealOutcome): Promise<ApiResponse<AppealSummary>> {
  const response = await api.post<ApiResponse<AppealSummary>>(`/appeals/${id}/decide`, { outcome })
  return response.data
}

export async function fetchRisks(): Promise<ApiResponse<PageResponse<RiskSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<RiskSummary>>>('/risks?size=50')
  return response.data
}

export async function fetchRisk(id: string): Promise<ApiResponse<RiskSummary>> {
  const response = await api.get<ApiResponse<RiskSummary>>(`/risks/${id}`)
  return response.data
}

export async function createRisk(body: {
  title: string
  category?: string
  likelihood?: number
  impact?: number
}): Promise<ApiResponse<RiskSummary>> {
  const response = await api.post<ApiResponse<RiskSummary>>('/risks', body)
  return response.data
}

export async function mitigateRisk(id: string): Promise<ApiResponse<RiskSummary>> {
  const response = await api.post<ApiResponse<RiskSummary>>(`/risks/${id}/mitigate`, {})
  return response.data
}

export async function closeRisk(id: string): Promise<ApiResponse<RiskSummary>> {
  const response = await api.post<ApiResponse<RiskSummary>>(`/risks/${id}/close`, {})
  return response.data
}

export async function fetchImpartiality(): Promise<ApiResponse<PageResponse<ImpartialitySummary>>> {
  const response = await api.get<ApiResponse<PageResponse<ImpartialitySummary>>>('/impartiality-records?size=50')
  return response.data
}

export async function fetchImpartialityRecord(id: string): Promise<ApiResponse<ImpartialitySummary>> {
  const response = await api.get<ApiResponse<ImpartialitySummary>>(`/impartiality-records/${id}`)
  return response.data
}

export async function createImpartiality(body: { title: string }): Promise<ApiResponse<ImpartialitySummary>> {
  const response = await api.post<ApiResponse<ImpartialitySummary>>('/impartiality-records', body)
  return response.data
}

export async function reviewImpartiality(id: string): Promise<ApiResponse<ImpartialitySummary>> {
  const response = await api.post<ApiResponse<ImpartialitySummary>>(`/impartiality-records/${id}/review`, {})
  return response.data
}

export async function closeImpartiality(id: string): Promise<ApiResponse<ImpartialitySummary>> {
  const response = await api.post<ApiResponse<ImpartialitySummary>>(`/impartiality-records/${id}/close`, {})
  return response.data
}
