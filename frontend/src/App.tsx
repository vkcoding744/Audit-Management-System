import { Navigate, Route, Routes } from 'react-router-dom'
import { RequireAuth } from './auth/RequireAuth'
import { AppShell } from './layout/AppShell'
import { AuditDetailPage } from './pages/AuditDetailPage'
import { AuditorDetailPage } from './pages/AuditorDetailPage'
import { AuditorsPage } from './pages/AuditorsPage'
import { ChecklistDetailPage } from './pages/ChecklistDetailPage'
import { ClientDetailPage } from './pages/ClientDetailPage'
import { ClientsPage } from './pages/ClientsPage'
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
        <Route path="/users" element={<UsersPage />} />
        <Route path="/roles" element={<RolesPage />} />
        <Route path="/tenants" element={<TenantsPage />} />
        <Route path="/sessions" element={<SessionsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
