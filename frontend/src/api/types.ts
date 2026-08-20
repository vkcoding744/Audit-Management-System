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

export type ClientStatus = 'PROSPECT' | 'ACTIVE' | 'SUSPENDED' | 'INACTIVE'
export type SiteStatus = 'ACTIVE' | 'INACTIVE'

export interface ClientSummary {
  id: string
  tenantId: string
  clientNumber: string
  legalName: string
  tradingName: string | null
  registrationNumber: string | null
  taxNumber: string | null
  industry: string | null
  employeeCount: number | null
  email: string | null
  phone: string | null
  website: string | null
  addressLine1: string | null
  addressLine2: string | null
  city: string | null
  state: string | null
  postalCode: string | null
  country: string | null
  status: ClientStatus
  notes: string | null
}

export interface SiteSummary {
  id: string
  tenantId: string
  clientId: string
  name: string
  addressLine1: string | null
  addressLine2: string | null
  city: string | null
  state: string | null
  postalCode: string | null
  country: string | null
  scope: string | null
  employeeCount: number | null
  processes: string | null
  status: SiteStatus
}

export interface ContactSummary {
  id: string
  tenantId: string
  clientId: string
  siteId: string | null
  firstName: string
  lastName: string
  designation: string | null
  email: string | null
  phone: string | null
  department: string | null
  primaryContact: boolean
  active: boolean
}

export interface ClientDashboard {
  client: ClientSummary
  siteCount: number
  contactCount: number
  upcomingAudits: number
  completedAudits: number
  openFindings: number
  overdueCapa: number
  activeCertificates: number
  certificatesExpiringSoon: number
  outstandingPayments: number
  documents: number
  openComplaints: number
  openAppeals: number
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
