import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { SystemStatusPage } from '../pages/SystemStatusPage'

vi.mock('../api/system', () => ({
  fetchSystemHealth: () =>
    Promise.resolve({
      success: true,
      data: { status: 'UP', database: 'UP', tenantCount: 0 },
      error: null,
      meta: { correlationId: 'test', timestamp: new Date().toISOString() },
    }),
  fetchSystemInfo: () =>
    Promise.resolve({
      success: true,
      data: { application: 'audit-platform', apiVersion: '0.2.0', environment: 'test' },
      error: null,
      meta: { correlationId: 'test', timestamp: new Date().toISOString() },
    }),
}))

describe('SystemStatusPage', () => {
  it('renders live health cards from the API client', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <SystemStatusPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )

    expect(await screen.findByText('Database')).toBeInTheDocument()
    expect(screen.getByText('0.2.0')).toBeInTheDocument()
    expect(screen.getAllByText('UP').length).toBeGreaterThan(0)
  })
})
