import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { FindingsPage } from './FindingsPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['AUDIT_VIEW', 'FINDING_CREATE'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['AUDIT_VIEW', 'FINDING_CREATE'].includes(code),
  }),
}))

vi.mock('../api/findings', () => ({
  fetchFindings: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'f1',
            tenantId: 't1',
            findingNumber: 'FIND-000001',
            auditId: 'a1',
            clientId: 'c1',
            siteId: null,
            responseId: null,
            clauseId: null,
            title: 'Calibration overdue',
            description: 'Measuring equipment past due date',
            severity: 'MINOR',
            status: 'OPEN',
            closedOn: null,
            notes: null,
            capa: [],
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
}))

describe('FindingsPage', () => {
  it('renders the findings directory', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <FindingsPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('Calibration overdue')).toBeInTheDocument()
    expect(screen.getByText('FIND-000001')).toBeInTheDocument()
  })
})
