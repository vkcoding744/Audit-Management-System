import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { LeadsPage } from './LeadsPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['LEAD_VIEW', 'LEAD_CREATE'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['LEAD_VIEW', 'LEAD_CREATE'].includes(code),
  }),
}))

vi.mock('../api/leads', () => ({
  fetchLeads: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'l1',
            tenantId: 't1',
            leadNumber: 'LEAD-000001',
            organisationName: 'Harbor Foods',
            contactName: 'Alex Chen',
            email: 'alex@example.com',
            phone: null,
            source: 'REFERRAL',
            status: 'OPEN',
            convertedClientId: null,
            convertedAt: null,
            lostReason: null,
            notes: null,
          },
        ],
        page: 0,
        size: 50,
        totalElements: 1,
        totalPages: 1,
      },
      error: null,
      meta: { correlationId: 'test', timestamp: new Date().toISOString() },
    }),
  createLead: vi.fn(),
}))

describe('LeadsPage', () => {
  it('renders the directory and create form', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <LeadsPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('LEAD-000001')).toBeInTheDocument()
    expect(screen.getByText('Harbor Foods')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create lead' })).toBeInTheDocument()
  })
})
