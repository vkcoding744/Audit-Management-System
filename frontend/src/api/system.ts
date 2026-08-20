import { api } from './client'
import type { ApiResponse, SystemHealth, SystemInfo } from './types'

export async function fetchSystemHealth(): Promise<ApiResponse<SystemHealth>> {
  const response = await api.get<ApiResponse<SystemHealth>>('/system/health')
  return response.data
}

export async function fetchSystemInfo(): Promise<ApiResponse<SystemInfo>> {
  const response = await api.get<ApiResponse<SystemInfo>>('/system/info')
  return response.data
}
