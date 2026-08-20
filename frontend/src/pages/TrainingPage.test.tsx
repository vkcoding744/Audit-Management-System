import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { TrainingPage } from './TrainingPage'

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({
    user: { permissions: ['TRAINING_VIEW', 'TRAINING_UPDATE', 'AUDITOR_VIEW'] },
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
    hasPermission: (code: string) => ['TRAINING_VIEW', 'TRAINING_UPDATE', 'AUDITOR_VIEW'].includes(code),
  }),
}))

vi.mock('../api/training', () => ({
  fetchTrainingRecords: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 't1',
            tenantId: 't1',
            trainingNumber: 'TRN-000001',
            auditorId: 'a1',
            title: 'Lead auditor course',
            provider: 'Internal',
            plannedOn: '2026-08-01',
            completedOn: null,
            hours: 16,
            expiresOn: null,
            standardId: null,
            schemeId: null,
            status: 'PLANNED',
            expired: false,
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
  fetchAssessments: () =>
    Promise.resolve({
      success: true,
      data: {
        content: [
          {
            id: 'asm1',
            tenantId: 't1',
            assessmentNumber: 'ASM-000001',
            auditorId: 'a1',
            competencyId: null,
            standardId: null,
            schemeId: null,
            assessedOn: '2026-08-10',
            assessorName: 'Pat Lee',
            result: null,
            status: 'DRAFT',
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
  createTrainingRecord: vi.fn(),
  createAssessment: vi.fn(),
}))

vi.mock('../api/auditors', () => ({
  fetchAuditors: () =>
    Promise.resolve({
      success: true,
      data: { content: [], page: 0, size: 50, totalElements: 0, totalPages: 0 },
      error: null,
      meta: { correlationId: 'test', timestamp: new Date().toISOString() },
    }),
}))

describe('TrainingPage', () => {
  it('renders training, assessments, and create forms', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <TrainingPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('TRN-000001')).toBeInTheDocument()
    expect(screen.getByText('ASM-000001')).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create training' })).toBeInTheDocument()
    expect(screen.getByRole('form', { name: 'Create assessment' })).toBeInTheDocument()
  })
})
