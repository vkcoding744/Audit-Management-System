import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ReportsPage } from './ReportsPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['REPORT_VIEW', 'REPORT_EXPORT'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['REPORT_VIEW', 'REPORT_EXPORT'].includes(code),
  }),
}))

const page = (content: unknown[]) => ({
  success: true,
  data: { content, page: 0, size: 50, totalElements: content.length, totalPages: 1 },
  error: null,
  meta: { correlationId: 'test', timestamp: new Date().toISOString() },
})

vi.mock('../api/reports', () => ({
  fetchReports: () =>
    Promise.resolve(
      page([
        {
          id: 'r1',
          tenantId: 't1',
          reportNumber: 'RPT-000001',
          name: 'Active clients',
          description: null,
          dataset: 'CLIENTS',
          format: 'CSV',
          statusFilter: 'ACTIVE',
          status: 'DRAFT',
        },
      ]),
    ),
  fetchReportExports: () =>
    Promise.resolve(
      page([
        {
          id: 'e1',
          tenantId: 't1',
          definitionId: 'r1',
          exportNumber: 'EXP-000001',
          format: 'CSV',
          status: 'COMPLETED',
          rowCount: 3,
          byteSize: 80,
          errorMessage: null,
          completedAt: '2026-08-21T12:00:00Z',
        },
      ]),
    ),
  createReport: vi.fn(),
}))

describe('ReportsPage', () => {
  it('renders definitions, exports, and create form', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <ReportsPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('RPT-000001')).toBeInTheDocument()
    expect(screen.getByText('EXP-000001')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create report' })).toBeInTheDocument()
  })
})
