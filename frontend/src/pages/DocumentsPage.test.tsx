import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { DocumentsPage } from './DocumentsPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['DOCUMENT_VIEW', 'DOCUMENT_UPLOAD'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['DOCUMENT_VIEW', 'DOCUMENT_UPLOAD'].includes(code),
  }),
}))

vi.mock('../api/documents', () => ({
  fetchDocuments: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'doc1',
            tenantId: 't1',
            documentNumber: 'DOC-000001',
            title: 'Opening meeting notes',
            originalFilename: 'notes.pdf',
            contentType: 'application/pdf',
            sizeBytes: 1200,
            checksumSha256: 'abc',
            clientId: 'c1',
            linkedType: 'AUDIT',
            linkedId: 'a1',
            category: 'EVIDENCE',
            notes: null,
            createdAt: '2026-08-20T12:00:00Z',
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
  uploadDocument: vi.fn(),
}))

describe('DocumentsPage', () => {
  it('renders the directory and upload form', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <DocumentsPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('DOC-000001')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Upload document' })).toBeInTheDocument()
  })
})
