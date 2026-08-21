import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuditLogsPage } from './AuditLogsPage'

vi.mock('../api/auditLogs', () => ({
  fetchAuditLogs: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'l1',
            tenantId: 't1',
            userId: 'u1',
            action: 'CLIENT_CREATE',
            entityType: 'Client',
            entityId: 'c1',
            oldValue: null,
            newValue: 'CLI-000001',
            ipAddress: null,
            userAgent: null,
            correlationId: 'corr',
            createdAt: '2026-08-21T12:00:00Z',
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

describe('AuditLogsPage', () => {
  it('renders tenant-scoped log entries', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <AuditLogsPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('CLIENT_CREATE')).toBeInTheDocument()
    expect(screen.getByText(/Client/)).toBeInTheDocument()
  })
})
