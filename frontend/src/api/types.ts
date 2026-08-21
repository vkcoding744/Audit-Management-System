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
  accessToken: string | null
  refreshToken: string | null
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

export type LeadStatus = 'OPEN' | 'QUALIFIED' | 'CONVERTED' | 'LOST'
export type LeadSource = 'WEBSITE' | 'REFERRAL' | 'TENDER' | 'EVENT' | 'OTHER'

export interface LeadSummary {
  id: string
  tenantId: string
  leadNumber: string
  organisationName: string
  contactName: string | null
  email: string | null
  phone: string | null
  source: LeadSource
  status: LeadStatus
  convertedClientId: string | null
  convertedAt: string | null
  lostReason: string | null
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

export type ProgrammeStatus = 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED'
export type AuditType = 'INITIAL' | 'SURVEILLANCE' | 'RECERTIFICATION' | 'SPECIAL' | 'TRANSFER'
export type AuditStage = 'NOT_APPLICABLE' | 'STAGE_1' | 'STAGE_2'
export type AuditStatus = 'PLANNED' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type AssignmentRole = 'LEAD' | 'TEAM' | 'TECHNICAL_EXPERT' | 'TRAINEE' | 'OBSERVER'

export interface ProgrammeSummary {
  id: string
  tenantId: string
  programmeNumber: string
  clientId: string
  schemeId: string
  standardId: string | null
  name: string
  status: ProgrammeStatus
  cycleStartOn: string | null
  cycleEndOn: string | null
  notes: string | null
}

export interface AuditSiteSummary {
  id: string
  tenantId: string
  auditId: string
  siteId: string
}

export interface AssignmentSummary {
  id: string
  tenantId: string
  auditId: string
  auditorId: string
  assignmentRole: AssignmentRole
}

export interface AuditSummary {
  id: string
  tenantId: string
  auditNumber: string
  programmeId: string
  clientId: string
  schemeId: string
  standardId: string | null
  checklistId: string | null
  name: string
  auditType: AuditType
  stage: AuditStage
  status: AuditStatus
  plannedStartOn: string | null
  plannedEndOn: string | null
  actualStartOn: string | null
  actualEndOn: string | null
  notes: string | null
  openingNotes: string | null
  closingNotes: string | null
  sites: AuditSiteSummary[]
  assignments: AssignmentSummary[]
}

export type AssessmentResult = 'NOT_ASSESSED' | 'CONFORMING' | 'NONCONFORMING' | 'NOT_APPLICABLE' | 'OBSERVATION'

export interface AuditItemSummary {
  id: string
  tenantId: string
  auditId: string
  checklistItemId: string
  clauseId: string | null
  title: string
  guidance: string | null
  itemType: ChecklistItemType
  required: boolean
  sortOrder: number
  result: AssessmentResult
  comment: string | null
  assessedBy: string | null
  assessedAt: string | null
}

export type FindingSeverity = 'MAJOR' | 'MINOR' | 'OBSERVATION' | 'OFI'
export type FindingStatus = 'OPEN' | 'CLOSED'
export type CapaStatus = 'OPEN' | 'COMPLETED' | 'CANCELLED'

export interface CapaSummary {
  id: string
  tenantId: string
  capaNumber: string
  findingId: string
  description: string
  dueOn: string
  completedOn: string | null
  status: CapaStatus
  notes: string | null
}

export interface FindingSummary {
  id: string
  tenantId: string
  findingNumber: string
  auditId: string
  clientId: string
  siteId: string | null
  responseId: string | null
  clauseId: string | null
  title: string
  description: string
  severity: FindingSeverity
  status: FindingStatus
  closedOn: string | null
  notes: string | null
  capa: CapaSummary[]
}

export type CertificateStatus = 'DRAFT' | 'ACTIVE' | 'SUSPENDED' | 'WITHDRAWN'
export type DecisionType = 'ISSUE' | 'SUSPEND' | 'REINSTATE' | 'WITHDRAW'
export type SurveillanceStatus = 'PLANNED' | 'COMPLETED' | 'CANCELLED'

export interface DecisionSummary {
  id: string
  tenantId: string
  certificateId: string
  decisionType: DecisionType
  reason: string | null
  decidedOn: string
}

export interface SurveillanceSummary {
  id: string
  tenantId: string
  certificateId: string
  plannedOn: string
  completedOn: string | null
  status: SurveillanceStatus
  notes: string | null
}

export interface CertificateSummary {
  id: string
  tenantId: string
  certificateNumber: string
  clientId: string
  schemeId: string
  standardId: string | null
  programmeId: string | null
  auditId: string
  scopeText: string | null
  status: CertificateStatus
  validFrom: string
  expiresOn: string
  nextSurveillanceOn: string | null
  expired: boolean
  notes: string | null
  decisions: DecisionSummary[]
  surveillance: SurveillanceSummary[]
}

export type DocumentLinkType = 'GENERAL' | 'CLIENT' | 'AUDIT' | 'FINDING' | 'CERTIFICATE'
export type DocumentCategory = 'EVIDENCE' | 'CONTROLLED' | 'REPORT' | 'OTHER'

export interface DocumentSummary {
  id: string
  tenantId: string
  documentNumber: string
  title: string
  originalFilename: string
  contentType: string
  sizeBytes: number
  checksumSha256: string
  clientId: string | null
  linkedType: DocumentLinkType
  linkedId: string | null
  category: DocumentCategory
  notes: string | null
  createdAt: string
}

export type QuoteStatus = 'DRAFT' | 'ISSUED' | 'ACCEPTED' | 'DECLINED'
export type InvoiceStatus = 'DRAFT' | 'ISSUED' | 'PARTIALLY_PAID' | 'PAID' | 'VOID'
export type PaymentMethod = 'BANK_TRANSFER' | 'CARD' | 'CHEQUE' | 'OTHER'

export interface FinanceLine {
  id: string
  description: string
  quantity: number
  unitAmount: number
  lineAmount: number
}

export interface QuoteSummary {
  id: string
  tenantId: string
  quoteNumber: string
  clientId: string
  currency: string
  status: QuoteStatus
  validUntil: string | null
  expired: boolean
  subtotal: number
  totalAmount: number
  notes: string | null
  lines: FinanceLine[]
}

export interface PaymentSummary {
  id: string
  tenantId: string
  paymentNumber: string
  invoiceId: string
  amount: number
  paidOn: string
  method: PaymentMethod
  reference: string | null
  notes: string | null
}

export interface InvoiceSummary {
  id: string
  tenantId: string
  invoiceNumber: string
  clientId: string
  quoteId: string | null
  currency: string
  status: InvoiceStatus
  issuedOn: string | null
  dueOn: string | null
  overdue: boolean
  subtotal: number
  totalAmount: number
  amountPaid: number
  amountDue: number
  notes: string | null
  lines: FinanceLine[]
  payments: PaymentSummary[]
}

export type TrainingStatus = 'PLANNED' | 'COMPLETED' | 'CANCELLED'
export type AssessmentStatus = 'DRAFT' | 'RECORDED'
export type CompetencyAssessmentResult = 'PASS' | 'FAIL'

export interface TrainingRecordSummary {
  id: string
  tenantId: string
  trainingNumber: string
  auditorId: string
  title: string
  provider: string | null
  plannedOn: string | null
  completedOn: string | null
  hours: number | null
  expiresOn: string | null
  standardId: string | null
  schemeId: string | null
  status: TrainingStatus
  expired: boolean
  notes: string | null
}

export interface AssessmentSummary {
  id: string
  tenantId: string
  assessmentNumber: string
  auditorId: string
  competencyId: string | null
  standardId: string | null
  schemeId: string | null
  assessedOn: string
  assessorName: string | null
  result: CompetencyAssessmentResult | null
  status: AssessmentStatus
  notes: string | null
}

export type ComplaintStatus = 'OPEN' | 'IN_REVIEW' | 'CLOSED'
export type ComplaintSource = 'CLIENT' | 'INTERESTED_PARTY' | 'INTERNAL' | 'REGULATOR' | 'OTHER'
export type AppealStatus = 'OPEN' | 'UNDER_REVIEW' | 'UPHELD' | 'DISMISSED'
export type AppealOutcome = 'UPHELD' | 'DISMISSED'
export type RiskStatus = 'OPEN' | 'MITIGATING' | 'CLOSED'
export type RiskCategory = 'OPERATIONAL' | 'IMPARTIALITY' | 'FINANCIAL' | 'COMPLIANCE' | 'OTHER'
export type ImpartialityStatus = 'OPEN' | 'REVIEWED' | 'CLOSED'

export interface ComplaintSummary {
  id: string
  tenantId: string
  complaintNumber: string
  clientId: string | null
  subject: string
  source: ComplaintSource
  receivedOn: string
  status: ComplaintStatus
  description: string | null
  resolution: string | null
  closedOn: string | null
}

export interface AppealSummary {
  id: string
  tenantId: string
  appealNumber: string
  clientId: string | null
  certificateId: string | null
  findingId: string | null
  subject: string
  receivedOn: string
  status: AppealStatus
  outcome: AppealOutcome | null
  description: string | null
  decisionNotes: string | null
  decidedOn: string | null
}

export interface RiskSummary {
  id: string
  tenantId: string
  riskNumber: string
  title: string
  category: RiskCategory
  likelihood: number | null
  impact: number | null
  score: number | null
  status: RiskStatus
  description: string | null
  mitigation: string | null
  closedOn: string | null
}

export interface ImpartialitySummary {
  id: string
  tenantId: string
  impartialityNumber: string
  title: string
  auditorId: string | null
  clientId: string | null
  identifiedOn: string
  status: ImpartialityStatus
  description: string | null
  reviewNotes: string | null
  closedOn: string | null
}

export type NotificationChannelType = 'EMAIL' | 'IN_APP'
export type TemplateStatus = 'ACTIVE' | 'INACTIVE'
export type NotificationEventType =
  | 'GENERIC'
  | 'PASSWORD_RESET'
  | 'CERTIFICATE_EXPIRING'
  | 'CAPA_OVERDUE'
  | 'COMPLAINT_OPEN'
  | 'AUDIT_SCHEDULED'
export type NotificationJobStatus = 'QUEUED' | 'SENT' | 'FAILED' | 'CANCELLED'

export interface NotificationTemplateSummary {
  id: string
  tenantId: string
  code: string
  name: string
  channel: NotificationChannelType
  eventType: NotificationEventType
  subject: string
  body: string
  status: TemplateStatus
}

export interface NotificationChannelSummary {
  id: string
  tenantId: string
  channel: NotificationChannelType
  enabled: boolean
  fromAddress: string | null
}

export interface NotificationJobSummary {
  id: string
  tenantId: string
  jobNumber: string
  templateId: string | null
  channel: NotificationChannelType
  toAddress: string
  subject: string
  body: string
  status: NotificationJobStatus
  scheduledFor: string | null
  sentAt: string | null
  errorMessage: string | null
  due: boolean
}

export type ReportDataset = 'CLIENTS' | 'AUDITS' | 'FINDINGS' | 'CERTIFICATES' | 'INVOICES' | 'COMPLAINTS'
export type ReportFormat = 'CSV' | 'JSON'
export type ReportDefinitionStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED'
export type ReportExportStatus = 'QUEUED' | 'COMPLETED' | 'FAILED' | 'CANCELLED'

export interface ReportDefinitionSummary {
  id: string
  tenantId: string
  reportNumber: string
  name: string
  description: string | null
  dataset: ReportDataset
  format: ReportFormat
  statusFilter: string | null
  status: ReportDefinitionStatus
}

export interface ReportExportSummary {
  id: string
  tenantId: string
  definitionId: string
  exportNumber: string
  format: ReportFormat
  status: ReportExportStatus
  rowCount: number | null
  byteSize: number | null
  errorMessage: string | null
  completedAt: string | null
}

export type AiPurpose = 'GENERIC' | 'FINDING_SUMMARY' | 'AUDIT_NARRATIVE' | 'COMPLAINT_RESPONSE'
export type AiGenerationStatus = 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED' | 'FAILED'
export type AiLinkedType = 'FINDING' | 'AUDIT' | 'COMPLAINT'

export interface AiGenerationSummary {
  id: string
  tenantId: string
  generationNumber: string
  purpose: AiPurpose
  prompt: string
  output: string
  provider: string
  model: string
  promptVersion: string
  linkedType: AiLinkedType | null
  linkedId: string | null
  status: AiGenerationStatus
  errorMessage: string | null
  reviewedBy: string | null
  reviewedAt: string | null
  reviewNotes: string | null
}

export interface TenantDashboardSummary {
  clients: number
  upcomingAudits: number
  completedAudits: number
  openFindings: number
  overdueCapa: number
  activeCertificates: number
  certificatesExpiringSoon: number
  outstandingInvoices: number
  openComplaints: number
  openAppeals: number
  pendingAiReviews: number
}

export interface AuditLogSummary {
  id: string
  tenantId: string | null
  userId: string | null
  action: string
  entityType: string
  entityId: string | null
  oldValue: string | null
  newValue: string | null
  ipAddress: string | null
  userAgent: string | null
  correlationId: string | null
  createdAt: string
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
