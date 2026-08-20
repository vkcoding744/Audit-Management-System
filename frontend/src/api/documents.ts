import { api } from './client'
import type { ApiResponse, DocumentSummary, PageResponse } from './types'

export async function fetchDocuments(): Promise<ApiResponse<PageResponse<DocumentSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  const response = await api.get<ApiResponse<PageResponse<DocumentSummary>>>(`/documents?${params.toString()}`)
  return response.data
}

export async function fetchDocument(id: string): Promise<ApiResponse<DocumentSummary>> {
  const response = await api.get<ApiResponse<DocumentSummary>>(`/documents/${id}`)
  return response.data
}

export async function uploadDocument(form: FormData): Promise<ApiResponse<DocumentSummary>> {
  const response = await api.post<ApiResponse<DocumentSummary>>('/documents', form)
  return response.data
}

export async function downloadDocument(id: string, filename: string): Promise<void> {
  const response = await api.get(`/documents/${id}/content`, { responseType: 'blob' })
  const blob = new Blob([response.data])
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

export async function deleteDocument(id: string): Promise<void> {
  await api.delete(`/documents/${id}`)
}
