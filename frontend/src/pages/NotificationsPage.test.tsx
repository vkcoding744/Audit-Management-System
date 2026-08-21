import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { NotificationsPage } from './NotificationsPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['NOTIFICATION_VIEW', 'NOTIFICATION_UPDATE'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['NOTIFICATION_VIEW', 'NOTIFICATION_UPDATE'].includes(code),
  }),
}))

const page = (content: unknown[]) => ({
  success: true,
  data: { content, page: 0, size: 50, totalElements: content.length, totalPages: 1 },
  error: null,
  meta: { correlationId: 'test', timestamp: new Date().toISOString() },
})

vi.mock('../api/notifications', () => ({
  fetchNotificationTemplates: () =>
    Promise.resolve(
      page([
        {
          id: 'tpl1',
          tenantId: 't1',
          code: 'CAPA_OVERDUE',
          name: 'Overdue CAPA',
          channel: 'EMAIL',
          eventType: 'CAPA_OVERDUE',
          subject: 'CAPA overdue',
          body: 'Action {{capaNumber}} is overdue.',
          status: 'ACTIVE',
        },
      ]),
    ),
  fetchNotificationChannels: () =>
    Promise.resolve({
      success: true,
      data: [{ id: 'ch1', tenantId: 't1', channel: 'EMAIL', enabled: true, fromAddress: null }],
      error: null,
      meta: { correlationId: 'test', timestamp: new Date().toISOString() },
    }),
  fetchNotificationJobs: () =>
    Promise.resolve(
      page([
        {
          id: 'j1',
          tenantId: 't1',
          jobNumber: 'NTF-000001',
          templateId: null,
          channel: 'EMAIL',
          toAddress: 'ops@example.com',
          subject: 'Hello',
          body: 'Body',
          status: 'QUEUED',
          scheduledFor: null,
          sentAt: null,
          errorMessage: null,
          due: false,
        },
      ]),
    ),
  createNotificationTemplate: vi.fn(),
  createNotificationJob: vi.fn(),
  updateNotificationChannel: vi.fn(),
  dispatchDueNotificationJobs: vi.fn(),
}))

describe('NotificationsPage', () => {
  it('renders templates, jobs, and create forms', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <NotificationsPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('CAPA_OVERDUE')).toBeInTheDocument()
    expect(screen.getByText('NTF-000001')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create template' })).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create job' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Dispatch due jobs' })).toBeInTheDocument()
  })
})
