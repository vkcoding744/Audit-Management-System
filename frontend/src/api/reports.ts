import { api } from './client'
import type { ApiResponse, PageResponse, ReportDefinitionSummary, ReportExportSummary } from './types'

export async function fetchReports(): Promise<ApiResponse<PageResponse<ReportDefinitionSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<ReportDefinitionSummary>>>('/reports?size=50')
  return response.data
}

export async function fetchReport(id: string): Promise<ApiResponse<ReportDefinitionSummary>> {
  const response = await api.get<ApiResponse<ReportDefinitionSummary>>(`/reports/${id}`)
  return response.data
}

export async function createReport(body: {
  name: string
  description?: string
  dataset: string
  format?: string
  statusFilter?: string
}): Promise<ApiResponse<ReportDefinitionSummary>> {
  const response = await api.post<ApiResponse<ReportDefinitionSummary>>('/reports', body)
  return response.data
}

export async function publishReport(id: string): Promise<ApiResponse<ReportDefinitionSummary>> {
  const response = await api.post<ApiResponse<ReportDefinitionSummary>>(`/reports/${id}/publish`)
  return response.data
}

export async function archiveReport(id: string): Promise<ApiResponse<ReportDefinitionSummary>> {
  const response = await api.post<ApiResponse<ReportDefinitionSummary>>(`/reports/${id}/archive`)
  return response.data
}

export async function runReport(id: string): Promise<ApiResponse<ReportExportSummary>> {
  const response = await api.post<ApiResponse<ReportExportSummary>>(`/reports/${id}/run`)
  return response.data
}

export async function fetchReportExports(): Promise<ApiResponse<PageResponse<ReportExportSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<ReportExportSummary>>>('/report-exports?size=50')
  return response.data
}

export async function fetchReportExport(id: string): Promise<ApiResponse<ReportExportSummary>> {
  const response = await api.get<ApiResponse<ReportExportSummary>>(`/report-exports/${id}`)
  return response.data
}

export async function cancelReportExport(id: string): Promise<ApiResponse<ReportExportSummary>> {
  const response = await api.post<ApiResponse<ReportExportSummary>>(`/report-exports/${id}/cancel`)
  return response.data
}

export async function downloadReportExport(id: string, filename: string): Promise<void> {
  const response = await api.get(`/report-exports/${id}/download`, { responseType: 'blob' })
  const blob = new Blob([response.data])
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}
