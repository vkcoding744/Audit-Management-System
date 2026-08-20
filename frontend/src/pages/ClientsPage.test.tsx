import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ClientsPage } from './ClientsPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['CLIENT_VIEW', 'CLIENT_CREATE'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['CLIENT_VIEW', 'CLIENT_CREATE'].includes(code),
  }),
}))

vi.mock('../api/clients', () => ({
  fetchClients: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'c1',
            tenantId: 't1',
            clientNumber: 'CLIENT-000001',
            legalName: 'Northwind Manufacturing',
            tradingName: null,
            registrationNumber: null,
            taxNumber: null,
            industry: 'Manufacturing',
            employeeCount: 120,
            email: null,
            phone: null,
            website: null,
            addressLine1: null,
            addressLine2: null,
            city: null,
            state: null,
            postalCode: null,
            country: null,
            status: 'PROSPECT',
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
  createClient: vi.fn(),
}))

describe('ClientsPage', () => {
  it('renders the directory and create form', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <ClientsPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('Northwind Manufacturing')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create client' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Create client' })).toBeEnabled()
  })
})
