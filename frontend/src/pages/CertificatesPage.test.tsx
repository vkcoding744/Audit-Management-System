import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { CertificatesPage } from './CertificatesPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['CERTIFICATE_VIEW', 'CERTIFICATE_ISSUE', 'AUDIT_VIEW'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['CERTIFICATE_VIEW', 'CERTIFICATE_ISSUE', 'AUDIT_VIEW'].includes(code),
  }),
}))

vi.mock('../api/certificates', () => ({
  fetchCertificates: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'cert1',
            tenantId: 't1',
            certificateNumber: 'CERT-000001',
            clientId: 'c1',
            schemeId: 's1',
            standardId: null,
            programmeId: null,
            auditId: 'a1',
            scopeText: 'Design and manufacture',
            status: 'ACTIVE',
            validFrom: '2026-01-01',
            expiresOn: '2029-01-01',
            nextSurveillanceOn: null,
            expired: false,
            notes: null,
            decisions: [],
            surveillance: [],
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
  createCertificate: vi.fn(),
}))

vi.mock('../api/audits', () => ({
  fetchAudits: () =>
    Promise.resolve({
      success: true,
      data: { content: [], page: 0, size: 50, totalElements: 0, totalPages: 0 },
      error: null,
      meta: { correlationId: 'test', timestamp: new Date().toISOString() },
    }),
}))

describe('CertificatesPage', () => {
  it('renders the directory and create form', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <CertificatesPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('CERT-000001')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create certificate' })).toBeInTheDocument()
  })
})
