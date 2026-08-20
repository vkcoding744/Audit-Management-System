import { api } from './client'
import type { ApiResponse, LeadStatus, LeadSummary, PageResponse } from './types'

export async function fetchLeads(status?: LeadStatus): Promise<ApiResponse<PageResponse<LeadSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  if (status) {
    params.set('status', status)
  }
  const response = await api.get<ApiResponse<PageResponse<LeadSummary>>>(`/leads?${params.toString()}`)
  return response.data
}

export async function fetchLead(id: string): Promise<ApiResponse<LeadSummary>> {
  const response = await api.get<ApiResponse<LeadSummary>>(`/leads/${id}`)
  return response.data
}

export async function createLead(body: {
  organisationName: string
  contactName?: string
  email?: string
  phone?: string
  source?: string
}): Promise<ApiResponse<LeadSummary>> {
  const response = await api.post<ApiResponse<LeadSummary>>('/leads', body)
  return response.data
}

export async function qualifyLead(id: string): Promise<ApiResponse<LeadSummary>> {
  const response = await api.post<ApiResponse<LeadSummary>>(`/leads/${id}/qualify`)
  return response.data
}

export async function loseLead(id: string, reason: string): Promise<ApiResponse<LeadSummary>> {
  const response = await api.post<ApiResponse<LeadSummary>>(`/leads/${id}/lose`, { reason })
  return response.data
}

export async function convertLead(id: string): Promise<ApiResponse<LeadSummary>> {
  const response = await api.post<ApiResponse<LeadSummary>>(`/leads/${id}/convert`)
  return response.data
}
