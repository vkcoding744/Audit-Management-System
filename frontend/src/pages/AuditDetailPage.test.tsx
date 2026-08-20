import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuditDetailPage } from './AuditDetailPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['AUDIT_VIEW', 'AUDIT_UPDATE'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['AUDIT_VIEW', 'AUDIT_UPDATE'].includes(code),
  }),
}))

vi.mock('../api/audits', () => ({
  fetchAudit: () =>
    Promise.resolve({
      success: true,
      data: {
        id: 'a1',
        tenantId: 't1',
        auditNumber: 'AUDIT-000001',
        programmeId: 'p1',
        clientId: 'c1',
        schemeId: 's1',
        standardId: null,
        checklistId: 'cl1',
        name: 'Stage 1 visit',
        auditType: 'INITIAL',
        stage: 'STAGE_1',
        status: 'SCHEDULED',
        plannedStartOn: '2026-09-01',
        plannedEndOn: '2026-09-02',
        actualStartOn: null,
        actualEndOn: null,
        notes: null,
        openingNotes: null,
        closingNotes: null,
        sites: [],
        assignments: [],
      },
      error: null,
      meta: { correlationId: 'test', timestamp: new Date().toISOString() },
    }),
  fetchAuditResponses: vi.fn(),
  scheduleAudit: vi.fn(),
  startAudit: vi.fn(),
  completeAudit: vi.fn(),
  cancelAudit: vi.fn(),
  addAuditSite: vi.fn(),
  assignAuditor: vi.fn(),
  updateAuditItem: vi.fn(),
  updateExecutionNotes: vi.fn(),
}))

vi.mock('../api/auditors', () => ({
  fetchAuditors: () => Promise.resolve({ success: true, data: { content: [] }, error: null, meta: {} }),
}))

vi.mock('../api/clients', () => ({
  fetchSites: () => Promise.resolve({ success: true, data: [], error: null, meta: {} }),
}))

vi.mock('../api/findings', () => ({
  fetchAuditFindings: () => Promise.resolve({ success: true, data: [], error: null, meta: {} }),
  createFinding: vi.fn(),
}))

describe('AuditDetailPage', () => {
  it('offers start fieldwork for a scheduled audit', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter initialEntries={['/audits/a1']}>
          <Routes>
            <Route path="/audits/:id" element={<AuditDetailPage />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('Stage 1 visit')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Start fieldwork' })).toBeInTheDocument()
  })
})
