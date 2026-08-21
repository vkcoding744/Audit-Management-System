import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { DashboardPage } from './DashboardPage'

vi.mock('../api/dashboard', () => ({
  fetchTenantDashboard: () =>
    Promise.resolve({
      success: true,
      data: {
        clients: 4,
        upcomingAudits: 2,
        completedAudits: 1,
        openFindings: 3,
        overdueCapa: 1,
        activeCertificates: 5,
        certificatesExpiringSoon: 2,
        outstandingInvoices: 6,
        openComplaints: 0,
        openAppeals: 1,
        pendingAiReviews: 2,
      },
      error: null,
      meta: { correlationId: 'test', timestamp: new Date().toISOString() },
    }),
}))

describe('DashboardPage', () => {
  it('renders live operational counts', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <DashboardPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('Open findings')).toBeInTheDocument()
    expect(screen.getByText('3')).toBeInTheDocument()
    expect(screen.getByText('AI drafts pending review')).toBeInTheDocument()
  })
})
