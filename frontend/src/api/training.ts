import { api } from './client'
import type {
  ApiResponse,
  CompetencyAssessmentResult,
  AssessmentStatus,
  AssessmentSummary,
  PageResponse,
  TrainingRecordSummary,
  TrainingStatus,
} from './types'

export async function fetchTrainingRecords(): Promise<ApiResponse<PageResponse<TrainingRecordSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  const response = await api.get<ApiResponse<PageResponse<TrainingRecordSummary>>>(
    `/training-records?${params.toString()}`,
  )
  return response.data
}

export async function fetchTrainingRecord(id: string): Promise<ApiResponse<TrainingRecordSummary>> {
  const response = await api.get<ApiResponse<TrainingRecordSummary>>(`/training-records/${id}`)
  return response.data
}

export async function createTrainingRecord(body: {
  auditorId: string
  title: string
  provider?: string
  plannedOn?: string
  hours?: number
  expiresOn?: string
}): Promise<ApiResponse<TrainingRecordSummary>> {
  const response = await api.post<ApiResponse<TrainingRecordSummary>>('/training-records', body)
  return response.data
}

export async function completeTrainingRecord(id: string, completedOn?: string): Promise<ApiResponse<TrainingRecordSummary>> {
  const response = await api.post<ApiResponse<TrainingRecordSummary>>(`/training-records/${id}/complete`, completedOn ? { completedOn } : {})
  return response.data
}

export async function cancelTrainingRecord(id: string): Promise<ApiResponse<TrainingRecordSummary>> {
  const response = await api.post<ApiResponse<TrainingRecordSummary>>(`/training-records/${id}/cancel`)
  return response.data
}

export async function fetchAssessments(): Promise<ApiResponse<PageResponse<AssessmentSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  const response = await api.get<ApiResponse<PageResponse<AssessmentSummary>>>(
    `/competency-assessments?${params.toString()}`,
  )
  return response.data
}

export async function fetchAssessment(id: string): Promise<ApiResponse<AssessmentSummary>> {
  const response = await api.get<ApiResponse<AssessmentSummary>>(`/competency-assessments/${id}`)
  return response.data
}

export async function createAssessment(body: {
  auditorId: string
  assessedOn: string
  assessorName?: string
}): Promise<ApiResponse<AssessmentSummary>> {
  const response = await api.post<ApiResponse<AssessmentSummary>>('/competency-assessments', body)
  return response.data
}

export async function completeAssessment(
  id: string,
  result: CompetencyAssessmentResult,
): Promise<ApiResponse<AssessmentSummary>> {
  const response = await api.post<ApiResponse<AssessmentSummary>>(`/competency-assessments/${id}/complete`, { result })
  return response.data
}

export type { CompetencyAssessmentResult as AssessmentResult, AssessmentStatus, TrainingStatus }
