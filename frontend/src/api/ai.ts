import { api } from './client'
import type { AiGenerationSummary, ApiResponse, PageResponse } from './types'

export async function fetchAiGenerations(): Promise<ApiResponse<PageResponse<AiGenerationSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<AiGenerationSummary>>>('/ai-generations?size=50')
  return response.data
}

export async function fetchAiGeneration(id: string): Promise<ApiResponse<AiGenerationSummary>> {
  const response = await api.get<ApiResponse<AiGenerationSummary>>(`/ai-generations/${id}`)
  return response.data
}

export async function createAiGeneration(body: {
  purpose?: string
  prompt: string
  linkedType?: string
  linkedId?: string
}): Promise<ApiResponse<AiGenerationSummary>> {
  const response = await api.post<ApiResponse<AiGenerationSummary>>('/ai-generations', body)
  return response.data
}

export async function approveAiGeneration(id: string, notes?: string): Promise<ApiResponse<AiGenerationSummary>> {
  const response = await api.post<ApiResponse<AiGenerationSummary>>(`/ai-generations/${id}/approve`, { notes })
  return response.data
}

export async function rejectAiGeneration(id: string, notes?: string): Promise<ApiResponse<AiGenerationSummary>> {
  const response = await api.post<ApiResponse<AiGenerationSummary>>(`/ai-generations/${id}/reject`, { notes })
  return response.data
}
