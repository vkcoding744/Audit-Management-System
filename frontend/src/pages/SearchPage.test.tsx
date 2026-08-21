import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { SearchPage } from './SearchPage'

vi.mock('../api/search', () => ({
  searchRecords: () =>
    Promise.resolve({
      success: true,
      data: {
        provider: 'mysql',
        query: 'acme',
        hits: [
          {
            type: 'CLIENT',
            id: 'c1',
            title: 'Acme Ltd',
            subtitle: 'CLIENT-000001',
            path: '/clients/c1',
          },
        ],
      },
      error: null,
      meta: { correlationId: 'test', timestamp: new Date().toISOString() },
    }),
}))

describe('SearchPage', () => {
  it('renders tenant search copy', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <SearchPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByRole('searchbox', { name: 'Search query' })).toBeInTheDocument()
    expect(screen.getByText(/not an Elasticsearch cluster/)).toBeInTheDocument()
  })
})
