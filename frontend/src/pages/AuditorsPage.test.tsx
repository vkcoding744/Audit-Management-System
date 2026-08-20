import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuditorsPage } from './AuditorsPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['AUDITOR_VIEW', 'AUDITOR_CREATE'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['AUDITOR_VIEW', 'AUDITOR_CREATE'].includes(code),
  }),
}))

vi.mock('../api/auditors', () => ({
  fetchAuditors: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'a1',
            tenantId: 't1',
            userId: null,
            employeeNumber: 'AUD-000001',
            firstName: 'Jordan',
            lastName: 'Lee',
            email: null,
            phone: null,
            jobTitle: 'Lead auditor',
            employmentType: 'EMPLOYEE',
            status: 'ACTIVE',
            baseLocation: null,
            country: null,
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
  createAuditor: vi.fn(),
}))

describe('AuditorsPage', () => {
  it('renders the directory and create form', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <AuditorsPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('Jordan Lee')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create auditor' })).toBeInTheDocument()
  })
})
