import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ProgrammesPage } from './ProgrammesPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['AUDIT_VIEW', 'AUDIT_CREATE', 'CLIENT_VIEW', 'SCHEME_VIEW'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['AUDIT_VIEW', 'AUDIT_CREATE', 'CLIENT_VIEW', 'SCHEME_VIEW'].includes(code),
  }),
}))

vi.mock('../api/audits', () => ({
  fetchProgrammes: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'p1',
            tenantId: 't1',
            programmeNumber: 'PROG-000001',
            clientId: 'c1',
            schemeId: 's1',
            standardId: null,
            name: 'QMS cycle 2026',
            status: 'DRAFT',
            cycleStartOn: '2026-01-01',
            cycleEndOn: '2028-12-31',
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
  createProgramme: vi.fn(),
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
            industry: null,
            employeeCount: null,
            email: null,
            phone: null,
            website: null,
            addressLine1: null,
            addressLine2: null,
            city: null,
            state: null,
            postalCode: null,
            country: null,
            status: 'ACTIVE',
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
}))

vi.mock('../api/standards', () => ({
  fetchSchemes: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 's1',
            tenantId: 't1',
            code: 'QMS',
            name: 'Quality management',
            description: null,
            accreditationBody: null,
            cycleMonths: 36,
            surveillanceIntervalMonths: 12,
            status: 'ACTIVE',
            notes: null,
            standards: [],
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

describe('ProgrammesPage', () => {
  it('renders the directory and create form', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <ProgrammesPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('QMS cycle 2026')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create programme' })).toBeInTheDocument()
  })
})
