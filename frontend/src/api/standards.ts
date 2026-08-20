import { api } from './client'
import type {
  ApiResponse,
  ChecklistItemSummary,
  ChecklistSummary,
  ClauseSummary,
  PageResponse,
  SchemeSummary,
  StandardSummary,
} from './types'

export async function fetchStandards(query?: string): Promise<ApiResponse<PageResponse<StandardSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  if (query) {
    params.set('q', query)
  }
  const response = await api.get<ApiResponse<PageResponse<StandardSummary>>>(`/standards?${params.toString()}`)
  return response.data
}

export async function fetchStandard(id: string): Promise<ApiResponse<StandardSummary>> {
  const response = await api.get<ApiResponse<StandardSummary>>(`/standards/${id}`)
  return response.data
}

export async function createStandard(body: {
  code: string
  name: string
  publisher?: string
  edition?: string
}): Promise<ApiResponse<StandardSummary>> {
  const response = await api.post<ApiResponse<StandardSummary>>('/standards', body)
  return response.data
}

export async function publishStandard(id: string): Promise<ApiResponse<StandardSummary>> {
  const response = await api.post<ApiResponse<StandardSummary>>(`/standards/${id}/publish`)
  return response.data
}

export async function fetchClauses(standardId: string): Promise<ApiResponse<ClauseSummary[]>> {
  const response = await api.get<ApiResponse<ClauseSummary[]>>(`/standards/${standardId}/clauses`)
  return response.data
}

export async function createClause(
  standardId: string,
  body: { clauseCode: string; title: string; parentId?: string; requirementText?: string },
): Promise<ApiResponse<ClauseSummary>> {
  const response = await api.post<ApiResponse<ClauseSummary>>(`/standards/${standardId}/clauses`, body)
  return response.data
}

export async function fetchSchemes(query?: string): Promise<ApiResponse<PageResponse<SchemeSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  if (query) {
    params.set('q', query)
  }
  const response = await api.get<ApiResponse<PageResponse<SchemeSummary>>>(`/schemes?${params.toString()}`)
  return response.data
}

export async function fetchScheme(id: string): Promise<ApiResponse<SchemeSummary>> {
  const response = await api.get<ApiResponse<SchemeSummary>>(`/schemes/${id}`)
  return response.data
}

export async function createScheme(body: {
  code: string
  name: string
  accreditationBody?: string
  cycleMonths?: number
}): Promise<ApiResponse<SchemeSummary>> {
  const response = await api.post<ApiResponse<SchemeSummary>>('/schemes', body)
  return response.data
}

export async function activateScheme(id: string): Promise<ApiResponse<SchemeSummary>> {
  const response = await api.post<ApiResponse<SchemeSummary>>(`/schemes/${id}/activate`)
  return response.data
}

export async function linkStandardToScheme(schemeId: string, standardId: string): Promise<ApiResponse<SchemeSummary>> {
  const response = await api.post<ApiResponse<SchemeSummary>>(`/schemes/${schemeId}/standards`, { standardId })
  return response.data
}

export async function fetchChecklists(schemeId: string): Promise<ApiResponse<ChecklistSummary[]>> {
  const response = await api.get<ApiResponse<ChecklistSummary[]>>(`/schemes/${schemeId}/checklists`)
  return response.data
}

export async function createChecklist(
  schemeId: string,
  body: { name: string; versionLabel: string; standardId?: string },
): Promise<ApiResponse<ChecklistSummary>> {
  const response = await api.post<ApiResponse<ChecklistSummary>>(`/schemes/${schemeId}/checklists`, body)
  return response.data
}

export async function fetchChecklist(id: string): Promise<ApiResponse<ChecklistSummary>> {
  const response = await api.get<ApiResponse<ChecklistSummary>>(`/checklists/${id}`)
  return response.data
}

export async function activateChecklist(id: string): Promise<ApiResponse<ChecklistSummary>> {
  const response = await api.post<ApiResponse<ChecklistSummary>>(`/checklists/${id}/activate`)
  return response.data
}

export async function addChecklistItem(
  checklistId: string,
  body: { title: string; clauseId?: string; guidance?: string },
): Promise<ApiResponse<ChecklistItemSummary>> {
  const response = await api.post<ApiResponse<ChecklistItemSummary>>(`/checklists/${checklistId}/items`, body)
  return response.data
}
