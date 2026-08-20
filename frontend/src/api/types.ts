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

export type StandardStatus = 'DRAFT' | 'PUBLISHED' | 'SUPERSEDED' | 'WITHDRAWN'
export type SchemeStatus = 'DRAFT' | 'ACTIVE' | 'SUSPENDED' | 'RETIRED'
export type ChecklistStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED'
export type ChecklistItemType = 'QUESTION' | 'EVIDENCE' | 'GUIDANCE'

export interface StandardSummary {
  id: string
  tenantId: string
  code: string
  name: string
  publisher: string | null
  edition: string | null
  description: string | null
  status: StandardStatus
  publishedAt: string | null
  notes: string | null
}

export interface ClauseSummary {
  id: string
  tenantId: string
  standardId: string
  parentId: string | null
  clauseCode: string
  title: string
  requirementText: string | null
  sortOrder: number
}

export interface SchemeSummary {
  id: string
  tenantId: string
  code: string
  name: string
  description: string | null
  accreditationBody: string | null
  cycleMonths: number | null
  surveillanceIntervalMonths: number | null
  status: SchemeStatus
  notes: string | null
  standards: StandardSummary[]
}

export interface ChecklistItemSummary {
  id: string
  tenantId: string
  checklistId: string
  clauseId: string | null
  title: string
  guidance: string | null
  itemType: ChecklistItemType
  required: boolean
  sortOrder: number
}

export interface ChecklistSummary {
  id: string
  tenantId: string
  schemeId: string
  standardId: string | null
  name: string
  versionLabel: string
  status: ChecklistStatus
  notes: string | null
  items: ChecklistItemSummary[]
}

export type AuditorStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
export type EmploymentType = 'EMPLOYEE' | 'CONTRACTOR'
export type CompetencyRole = 'LEAD' | 'TEAM' | 'TECHNICAL_EXPERT' | 'TRAINEE'
export type CompetencyRecordStatus = 'ACTIVE' | 'SUSPENDED' | 'REVOKED'
export type AvailabilityKind = 'AVAILABLE' | 'UNAVAILABLE'

export interface AuditorSummary {
  id: string
  tenantId: string
  userId: string | null
  employeeNumber: string
  firstName: string
  lastName: string
  email: string | null
  phone: string | null
  jobTitle: string | null
  employmentType: EmploymentType
  status: AuditorStatus
  baseLocation: string | null
  country: string | null
  notes: string | null
}

export interface QualificationSummary {
  id: string
  tenantId: string
  auditorId: string
  title: string
  issuer: string | null
  issuedOn: string | null
  expiresOn: string | null
  notes: string | null
}

export interface CompetencySummary {
  id: string
  tenantId: string
  auditorId: string
  standardId: string | null
  schemeId: string | null
  competencyRole: CompetencyRole
  status: CompetencyRecordStatus
  validFrom: string
  validTo: string | null
  expired: boolean
  current: boolean
  notes: string | null
}

export interface AvailabilitySummary {
  id: string
  tenantId: string
  auditorId: string
  startOn: string
  endOn: string
  kind: AvailabilityKind
  reason: string | null
}

export interface EligibilitySummary {
  auditorId: string
  standardId: string | null
  schemeId: string | null
  on: string
  eligible: boolean
  reasons: string[]
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
