import { api } from './client'
import type {
  ApiResponse,
  NotificationChannelSummary,
  NotificationDispatchResult,
  NotificationJobSummary,
  NotificationTemplateSummary,
  PageResponse,
} from './types'

export async function fetchNotificationTemplates(): Promise<ApiResponse<PageResponse<NotificationTemplateSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<NotificationTemplateSummary>>>('/notification-templates?size=50')
  return response.data
}

export async function fetchNotificationTemplate(id: string): Promise<ApiResponse<NotificationTemplateSummary>> {
  const response = await api.get<ApiResponse<NotificationTemplateSummary>>(`/notification-templates/${id}`)
  return response.data
}

export async function createNotificationTemplate(body: {
  code: string
  name: string
  channel?: string
  subject: string
  body: string
}): Promise<ApiResponse<NotificationTemplateSummary>> {
  const response = await api.post<ApiResponse<NotificationTemplateSummary>>('/notification-templates', body)
  return response.data
}

export async function activateTemplate(id: string): Promise<ApiResponse<NotificationTemplateSummary>> {
  const response = await api.post<ApiResponse<NotificationTemplateSummary>>(`/notification-templates/${id}/activate`)
  return response.data
}

export async function deactivateTemplate(id: string): Promise<ApiResponse<NotificationTemplateSummary>> {
  const response = await api.post<ApiResponse<NotificationTemplateSummary>>(`/notification-templates/${id}/deactivate`)
  return response.data
}

export async function fetchNotificationChannels(): Promise<ApiResponse<NotificationChannelSummary[]>> {
  const response = await api.get<ApiResponse<NotificationChannelSummary[]>>('/notification-channels')
  return response.data
}

export async function updateNotificationChannel(
  id: string,
  body: { enabled?: boolean },
): Promise<ApiResponse<NotificationChannelSummary>> {
  const response = await api.patch<ApiResponse<NotificationChannelSummary>>(`/notification-channels/${id}`, body)
  return response.data
}

export async function fetchNotificationJobs(): Promise<ApiResponse<PageResponse<NotificationJobSummary>>> {
  const response = await api.get<ApiResponse<PageResponse<NotificationJobSummary>>>('/notification-jobs?size=50')
  return response.data
}

export async function fetchNotificationJob(id: string): Promise<ApiResponse<NotificationJobSummary>> {
  const response = await api.get<ApiResponse<NotificationJobSummary>>(`/notification-jobs/${id}`)
  return response.data
}

export async function createNotificationJob(body: {
  templateId?: string
  toAddress: string
  subject?: string
  body?: string
}): Promise<ApiResponse<NotificationJobSummary>> {
  const response = await api.post<ApiResponse<NotificationJobSummary>>('/notification-jobs', body)
  return response.data
}

export async function dispatchDueNotificationJobs(): Promise<ApiResponse<NotificationDispatchResult>> {
  const response = await api.post<ApiResponse<NotificationDispatchResult>>('/notification-jobs/dispatch')
  return response.data
}

export async function sendNotificationJob(id: string): Promise<ApiResponse<NotificationJobSummary>> {
  const response = await api.post<ApiResponse<NotificationJobSummary>>(`/notification-jobs/${id}/send`)
  return response.data
}

export async function cancelNotificationJob(id: string): Promise<ApiResponse<NotificationJobSummary>> {
  const response = await api.post<ApiResponse<NotificationJobSummary>>(`/notification-jobs/${id}/cancel`)
  return response.data
}
