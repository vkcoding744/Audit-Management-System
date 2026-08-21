import { Navigate, Route, Routes } from 'react-router-dom'
import { RequireAuth } from './auth/RequireAuth'
import { AppShell } from './layout/AppShell'
import { AuditDetailPage } from './pages/AuditDetailPage'
import { AuditorDetailPage } from './pages/AuditorDetailPage'
import { AuditorsPage } from './pages/AuditorsPage'
import { CertificateDetailPage } from './pages/CertificateDetailPage'
import { CertificatesPage } from './pages/CertificatesPage'
import { ChecklistDetailPage } from './pages/ChecklistDetailPage'
import { ClientDetailPage } from './pages/ClientDetailPage'
import { ClientsPage } from './pages/ClientsPage'
import { LeadDetailPage } from './pages/LeadDetailPage'
import { LeadsPage } from './pages/LeadsPage'
import { DocumentDetailPage } from './pages/DocumentDetailPage'
import { DocumentsPage } from './pages/DocumentsPage'
import { FindingDetailPage } from './pages/FindingDetailPage'
import { FindingsPage } from './pages/FindingsPage'
import { FinancePage } from './pages/FinancePage'
import { InvoiceDetailPage } from './pages/InvoiceDetailPage'
import { QuoteDetailPage } from './pages/QuoteDetailPage'
import { ForgotPasswordPage } from './pages/ForgotPasswordPage'
import { LoginPage } from './pages/LoginPage'
import { ResetPasswordPage } from './pages/ResetPasswordPage'
import { ProgrammeDetailPage } from './pages/ProgrammeDetailPage'
import { ProgrammesPage } from './pages/ProgrammesPage'
import { RolesPage } from './pages/RolesPage'
import { SchemeDetailPage } from './pages/SchemeDetailPage'
import { SchemesPage } from './pages/SchemesPage'
import { SessionsPage } from './pages/SessionsPage'
import { StandardDetailPage } from './pages/StandardDetailPage'
import { StandardsPage } from './pages/StandardsPage'
import { SystemStatusPage } from './pages/SystemStatusPage'
import { TenantsPage } from './pages/TenantsPage'
import { UsersPage } from './pages/UsersPage'
import { TrainingPage } from './pages/TrainingPage'
import { TrainingRecordDetailPage } from './pages/TrainingRecordDetailPage'
import { AssessmentDetailPage } from './pages/AssessmentDetailPage'
import { GovernancePage } from './pages/GovernancePage'
import { ComplaintDetailPage } from './pages/ComplaintDetailPage'
import { AppealDetailPage } from './pages/AppealDetailPage'
import { RiskDetailPage } from './pages/RiskDetailPage'
import { ImpartialityDetailPage } from './pages/ImpartialityDetailPage'
import { NotificationsPage } from './pages/NotificationsPage'
import { NotificationTemplateDetailPage } from './pages/NotificationTemplateDetailPage'
import { NotificationJobDetailPage } from './pages/NotificationJobDetailPage'
import { ReportsPage } from './pages/ReportsPage'
import { ReportDetailPage } from './pages/ReportDetailPage'
import { ReportExportDetailPage } from './pages/ReportExportDetailPage'
import { AiPage } from './pages/AiPage'
import { AiGenerationDetailPage } from './pages/AiGenerationDetailPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route
        element={
          <RequireAuth>
            <AppShell />
          </RequireAuth>
        }
      >
        <Route path="/" element={<SystemStatusPage />} />
        <Route path="/clients" element={<ClientsPage />} />
        <Route path="/clients/:id" element={<ClientDetailPage />} />
        <Route path="/leads" element={<LeadsPage />} />
        <Route path="/leads/:id" element={<LeadDetailPage />} />
        <Route path="/standards" element={<StandardsPage />} />
        <Route path="/standards/:id" element={<StandardDetailPage />} />
        <Route path="/schemes" element={<SchemesPage />} />
        <Route path="/schemes/:id" element={<SchemeDetailPage />} />
        <Route path="/checklists/:id" element={<ChecklistDetailPage />} />
        <Route path="/auditors" element={<AuditorsPage />} />
        <Route path="/auditors/:id" element={<AuditorDetailPage />} />
        <Route path="/programmes" element={<ProgrammesPage />} />
        <Route path="/programmes/:id" element={<ProgrammeDetailPage />} />
        <Route path="/audits/:id" element={<AuditDetailPage />} />
        <Route path="/findings" element={<FindingsPage />} />
        <Route path="/findings/:id" element={<FindingDetailPage />} />
        <Route path="/certificates" element={<CertificatesPage />} />
        <Route path="/certificates/:id" element={<CertificateDetailPage />} />
        <Route path="/documents" element={<DocumentsPage />} />
        <Route path="/documents/:id" element={<DocumentDetailPage />} />
        <Route path="/finance" element={<FinancePage />} />
        <Route path="/quotes/:id" element={<QuoteDetailPage />} />
        <Route path="/invoices/:id" element={<InvoiceDetailPage />} />
        <Route path="/training" element={<TrainingPage />} />
        <Route path="/training-records/:id" element={<TrainingRecordDetailPage />} />
        <Route path="/assessments/:id" element={<AssessmentDetailPage />} />
        <Route path="/governance" element={<GovernancePage />} />
        <Route path="/complaints/:id" element={<ComplaintDetailPage />} />
        <Route path="/appeals/:id" element={<AppealDetailPage />} />
        <Route path="/risks/:id" element={<RiskDetailPage />} />
        <Route path="/impartiality/:id" element={<ImpartialityDetailPage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/notification-templates/:id" element={<NotificationTemplateDetailPage />} />
        <Route path="/notification-jobs/:id" element={<NotificationJobDetailPage />} />
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="/reports/:id" element={<ReportDetailPage />} />
        <Route path="/report-exports/:id" element={<ReportExportDetailPage />} />
        <Route path="/ai" element={<AiPage />} />
        <Route path="/ai-generations/:id" element={<AiGenerationDetailPage />} />
        <Route path="/users" element={<UsersPage />} />
        <Route path="/roles" element={<RolesPage />} />
        <Route path="/tenants" element={<TenantsPage />} />
        <Route path="/sessions" element={<SessionsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
