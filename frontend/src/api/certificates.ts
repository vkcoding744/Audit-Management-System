import { api } from './client'
import type {
  ApiResponse,
  CertificateStatus,
  CertificateSummary,
  PageResponse,
  SurveillanceSummary,
} from './types'

export async function fetchCertificates(): Promise<ApiResponse<PageResponse<CertificateSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  const response = await api.get<ApiResponse<PageResponse<CertificateSummary>>>(`/certificates?${params.toString()}`)
  return response.data
}

export async function fetchCertificate(id: string): Promise<ApiResponse<CertificateSummary>> {
  const response = await api.get<ApiResponse<CertificateSummary>>(`/certificates/${id}`)
  return response.data
}

export async function createCertificate(body: {
  auditId: string
  validFrom?: string
  expiresOn: string
  scopeText?: string
  nextSurveillanceOn?: string
}): Promise<ApiResponse<CertificateSummary>> {
  const response = await api.post<ApiResponse<CertificateSummary>>('/certificates', body)
  return response.data
}

export async function issueCertificate(id: string): Promise<ApiResponse<CertificateSummary>> {
  const response = await api.post<ApiResponse<CertificateSummary>>(`/certificates/${id}/issue`)
  return response.data
}

export async function suspendCertificate(id: string, reason: string): Promise<ApiResponse<CertificateSummary>> {
  const response = await api.post<ApiResponse<CertificateSummary>>(`/certificates/${id}/suspend`, { reason })
  return response.data
}

export async function reinstateCertificate(id: string, reason: string): Promise<ApiResponse<CertificateSummary>> {
  const response = await api.post<ApiResponse<CertificateSummary>>(`/certificates/${id}/reinstate`, { reason })
  return response.data
}

export async function withdrawCertificate(id: string, reason: string): Promise<ApiResponse<CertificateSummary>> {
  const response = await api.post<ApiResponse<CertificateSummary>>(`/certificates/${id}/withdraw`, { reason })
  return response.data
}

export async function addSurveillance(
  certificateId: string,
  body: { plannedOn: string; notes?: string },
): Promise<ApiResponse<SurveillanceSummary>> {
  const response = await api.post<ApiResponse<SurveillanceSummary>>(`/certificates/${certificateId}/surveillance`, body)
  return response.data
}

export async function completeSurveillance(id: string): Promise<ApiResponse<SurveillanceSummary>> {
  const response = await api.post<ApiResponse<SurveillanceSummary>>(`/surveillance/${id}/complete`)
  return response.data
}

export type { CertificateStatus }
