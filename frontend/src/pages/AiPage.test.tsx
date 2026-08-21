import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AiPage } from './AiPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['AI_VIEW', 'AI_UPDATE'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['AI_VIEW', 'AI_UPDATE'].includes(code),
  }),
}))

vi.mock('../api/ai', () => ({
  fetchAiGenerations: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'a1',
            tenantId: 't1',
            generationNumber: 'AIG-000001',
            purpose: 'FINDING_SUMMARY',
            prompt: 'Summarise finding',
            output: 'Human review required.',
            provider: 'stub',
            model: 'stub-v1',
            promptVersion: 'v1',
            linkedType: null,
            linkedId: null,
            status: 'PENDING_REVIEW',
            errorMessage: null,
            reviewedBy: null,
            reviewedAt: null,
            reviewNotes: null,
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
  createAiGeneration: vi.fn(),
}))

describe('AiPage', () => {
  it('renders drafts and the generate form', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <AiPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('AIG-000001')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create AI draft' })).toBeInTheDocument()
  })
})
