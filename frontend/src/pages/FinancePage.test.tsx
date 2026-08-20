import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { FinancePage } from './FinancePage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['INVOICE_VIEW', 'INVOICE_CREATE', 'CLIENT_VIEW'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['INVOICE_VIEW', 'INVOICE_CREATE', 'CLIENT_VIEW'].includes(code),
  }),
}))

vi.mock('../api/finance', () => ({
  fetchQuotes: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'q1',
            tenantId: 't1',
            quoteNumber: 'QUOTE-000001',
            clientId: 'c1',
            currency: 'USD',
            status: 'ISSUED',
            validUntil: '2026-12-01',
            expired: false,
            subtotal: 500,
            totalAmount: 500,
            notes: null,
            lines: [],
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
  fetchInvoices: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'i1',
            tenantId: 't1',
            invoiceNumber: 'INV-000001',
            clientId: 'c1',
            quoteId: null,
            currency: 'USD',
            status: 'ISSUED',
            issuedOn: '2026-08-01',
            dueOn: '2026-08-10',
            overdue: true,
            subtotal: 500,
            totalAmount: 500,
            amountPaid: 0,
            amountDue: 500,
            notes: null,
            lines: [],
            payments: [],
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
  createQuote: vi.fn(),
  createInvoice: vi.fn(),
}))

vi.mock('../api/clients', () => ({
  fetchClients: () =>
    Promise.resolve({
      success: true,
      data: { content: [], page: 0, size: 50, totalElements: 0, totalPages: 0 },
      error: null,
      meta: { correlationId: 'test', timestamp: new Date().toISOString() },
    }),
}))

describe('FinancePage', () => {
  it('renders quotes, invoices, and create forms', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <FinancePage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('QUOTE-000001')).toBeInTheDocument()
    expect(screen.getByText('INV-000001')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create quote' })).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create invoice' })).toBeInTheDocument()
  })
})
