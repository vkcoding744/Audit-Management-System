import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { GovernancePage } from './GovernancePage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['COMPLAINT_VIEW', 'COMPLAINT_UPDATE', 'APPEAL_VIEW', 'APPEAL_UPDATE', 'RISK_VIEW', 'RISK_UPDATE', 'CLIENT_VIEW'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) =>
      ['COMPLAINT_VIEW', 'COMPLAINT_UPDATE', 'APPEAL_VIEW', 'APPEAL_UPDATE', 'RISK_VIEW', 'RISK_UPDATE', 'CLIENT_VIEW'].includes(code),
  }),
}))

const page = (content: unknown[]) => ({
  success: true,
  data: { content, page: 0, size: 50, totalElements: content.length, totalPages: 1 },
  error: null,
  meta: { correlationId: 'test', timestamp: new Date().toISOString() },
})

vi.mock('../api/governance', () => ({
  fetchComplaints: () =>
    Promise.resolve(
      page([
        {
          id: 'c1',
          tenantId: 't1',
          complaintNumber: 'CMP-000001',
          clientId: null,
          subject: 'Late report',
          source: 'CLIENT',
          receivedOn: '2026-08-01',
          status: 'OPEN',
          description: null,
          resolution: null,
          closedOn: null,
        },
      ]),
    ),
  fetchAppeals: () =>
    Promise.resolve(
      page([
        {
          id: 'a1',
          tenantId: 't1',
          appealNumber: 'APL-000001',
          clientId: null,
          certificateId: null,
          findingId: null,
          subject: 'Scope dispute',
          receivedOn: '2026-08-02',
          status: 'OPEN',
          outcome: null,
          description: null,
          decisionNotes: null,
          decidedOn: null,
        },
      ]),
    ),
  fetchRisks: () => Promise.resolve(page([])),
  fetchImpartiality: () => Promise.resolve(page([])),
  createComplaint: vi.fn(),
  createAppeal: vi.fn(),
  createRisk: vi.fn(),
  createImpartiality: vi.fn(),
}))

vi.mock('../api/clients', () => ({
  fetchClients: () => Promise.resolve(page([])),
}))

describe('GovernancePage', () => {
  it('renders complaints, appeals, and create forms', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <GovernancePage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('CMP-000001')).toBeInTheDocument()
    expect(screen.getByText('APL-000001')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create complaint' })).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create appeal' })).toBeInTheDocument()
  })
})
