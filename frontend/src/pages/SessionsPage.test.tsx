import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi } from 'vitest'
import { SessionsPage } from './SessionsPage'

vi.mock('../api/auth', () => ({
  fetchSessions: () => Promise.resolve({ success: true, data: [], error: null, meta: { correlationId: 'c', timestamp: 't' } }),
  fetchMfaStatus: () => Promise.resolve({ success: true, data: { mfaEnabled: false }, error: null, meta: { correlationId: 'c', timestamp: 't' } }),
  setupMfa: vi.fn(),
  enableMfa: vi.fn(),
  disableMfa: vi.fn(),
}))

describe('SessionsPage', () => {
  it('shows MFA setup controls', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <SessionsPage />
      </QueryClientProvider>,
    )
    expect(await screen.findByRole('heading', { name: 'Authenticator MFA' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Generate authenticator secret' })).toBeEnabled()
  })
})
