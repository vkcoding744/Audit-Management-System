export interface ApiErrorBody {
  code: string
  message: string
  details: { field: string; message: string }[]
}

export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: ApiErrorBody | null
  meta: {
    correlationId: string
    timestamp: string
  }
}

export interface SystemHealth {
  status: string
  database: string
  tenantCount: number
}

export interface SystemInfo {
  application: string
  apiVersion: string
  environment: string
}
