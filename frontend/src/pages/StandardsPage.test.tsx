import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { StandardsPage } from './StandardsPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['STANDARD_VIEW', 'STANDARD_CREATE'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['STANDARD_VIEW', 'STANDARD_CREATE'].includes(code),
  }),
}))

vi.mock('../api/standards', () => ({
  fetchStandards: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 's1',
            tenantId: 't1',
            code: 'QMS-001',
            name: 'Quality management requirements',
            publisher: 'Internal',
            edition: '2024',
            description: null,
            status: 'DRAFT',
            publishedAt: null,
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
  createStandard: vi.fn(),
}))

describe('StandardsPage', () => {
  it('renders the catalogue and create form', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <StandardsPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('Quality management requirements')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create standard' })).toBeInTheDocument()
  })
})
