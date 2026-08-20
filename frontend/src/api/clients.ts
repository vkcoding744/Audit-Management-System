import { api } from './client'
import type {
  ApiResponse,
  ClientDashboard,
  ClientStatus,
  ClientSummary,
  ContactSummary,
  PageResponse,
  SiteSummary,
} from './types'

export async function fetchClients(query?: string): Promise<ApiResponse<PageResponse<ClientSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  if (query) {
    params.set('q', query)
  }
  const response = await api.get<ApiResponse<PageResponse<ClientSummary>>>(`/clients?${params.toString()}`)
  return response.data
}

export async function fetchClient(id: string): Promise<ApiResponse<ClientSummary>> {
  const response = await api.get<ApiResponse<ClientSummary>>(`/clients/${id}`)
  return response.data
}

export async function fetchClientDashboard(id: string): Promise<ApiResponse<ClientDashboard>> {
  const response = await api.get<ApiResponse<ClientDashboard>>(`/clients/${id}/dashboard`)
  return response.data
}

export async function createClient(body: {
  legalName: string
  tradingName?: string
  registrationNumber?: string
  taxNumber?: string
  industry?: string
  employeeCount?: number
  email?: string
  phone?: string
  website?: string
  addressLine1?: string
  city?: string
  country?: string
  status?: ClientStatus
}): Promise<ApiResponse<ClientSummary>> {
  const response = await api.post<ApiResponse<ClientSummary>>('/clients', body)
  return response.data
}

export async function setClientStatus(id: string, action: 'activate' | 'suspend'): Promise<ApiResponse<ClientSummary>> {
  const response = await api.post<ApiResponse<ClientSummary>>(`/clients/${id}/${action}`)
  return response.data
}

export async function fetchSites(clientId: string): Promise<ApiResponse<SiteSummary[]>> {
  const response = await api.get<ApiResponse<SiteSummary[]>>(`/clients/${clientId}/sites`)
  return response.data
}

export async function createSite(
  clientId: string,
  body: { name: string; city?: string; country?: string; employeeCount?: number },
): Promise<ApiResponse<SiteSummary>> {
  const response = await api.post<ApiResponse<SiteSummary>>(`/clients/${clientId}/sites`, body)
  return response.data
}

export async function fetchContacts(clientId: string): Promise<ApiResponse<ContactSummary[]>> {
  const response = await api.get<ApiResponse<ContactSummary[]>>(`/clients/${clientId}/contacts`)
  return response.data
}

export async function createContact(
  clientId: string,
  body: {
    firstName: string
    lastName: string
    email?: string
    phone?: string
    designation?: string
    siteId?: string
    primaryContact: boolean
  },
): Promise<ApiResponse<ContactSummary>> {
  const response = await api.post<ApiResponse<ContactSummary>>(`/clients/${clientId}/contacts`, body)
  return response.data
}
