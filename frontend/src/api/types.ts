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

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
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

export interface UserSummary {
  id: string
  tenantId: string | null
  email: string
  firstName: string
  lastName: string
  status: string
  emailVerified: boolean
  mfaEnabled: boolean
  platformAdmin: boolean
  roles: string[]
  permissions: string[]
}

export interface TokenPayload {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserSummary
  resetToken?: string | null
  verificationToken?: string | null
}

export interface RoleSummary {
  id: string
  code: string
  name: string
  description: string
  systemRole: boolean
  permissions: string[]
}

export interface TenantSummary {
  id: string
  code: string
  name: string
  status: string
}

export interface AuthSession {
  id: string
  ipAddress: string | null
  userAgent: string | null
  expiresAt: string
  createdAt: string
  current: boolean
  revoked: boolean
}
