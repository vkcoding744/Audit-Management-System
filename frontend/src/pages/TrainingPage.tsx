import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm, type UseFormRegisterReturn } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { fetchAuditors } from '../api/auditors'
import { createAssessment, createTrainingRecord, fetchAssessments, fetchTrainingRecords } from '../api/training'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const trainingSchema = z.object({
  auditorId: z.string().min(1),
  title: z.string().min(1),
  provider: z.string().optional(),
})

const assessmentSchema = z.object({
  auditorId: z.string().min(1),
  assessedOn: z.string().min(1),
  assessorName: z.string().optional(),
})

export function TrainingPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const trainingQuery = useQuery({ queryKey: ['training-records'], queryFn: () => fetchTrainingRecords() })
  const assessmentQuery = useQuery({ queryKey: ['assessments'], queryFn: () => fetchAssessments() })
  const auditorsQuery = useQuery({
    queryKey: ['auditors'],
    queryFn: () => fetchAuditors(),
    enabled: hasPermission('TRAINING_UPDATE') && hasPermission('AUDITOR_VIEW'),
  })
  const records = trainingQuery.data?.data?.content ?? []
  const assessments = assessmentQuery.data?.data?.content ?? []
  const auditors = auditorsQuery.data?.data?.content ?? []
  const trainingForm = useForm({
    resolver: zodResolver(trainingSchema),
    defaultValues: { auditorId: '', title: '', provider: '' },
  })
  const assessmentForm = useForm({
    resolver: zodResolver(assessmentSchema),
    defaultValues: { auditorId: '', assessedOn: '', assessorName: '' },
  })

  const createT = useMutation({
    mutationFn: (values: z.infer<typeof trainingSchema>) =>
      createTrainingRecord({
        auditorId: values.auditorId,
        title: values.title,
        provider: values.provider || undefined,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['training-records'] })
      trainingForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      trainingForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create training' })
    },
  })
  const createA = useMutation({
    mutationFn: (values: z.infer<typeof assessmentSchema>) =>
      createAssessment({
        auditorId: values.auditorId,
        assessedOn: values.assessedOn,
        assessorName: values.assessorName || undefined,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['assessments'] })
      assessmentForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      assessmentForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create assessment' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Training</h2>
      <p className="mt-1 text-sm text-slate-600">
        Planned and completed training records, plus competency assessments. Completing training or passing an
        assessment does not change auditor competency used for assignment.
      </p>
      {(trainingQuery.isError || assessmentQuery.isError) && (
        <p className="mt-4 text-sm text-red-700">You do not have TRAINING_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}

      <h3 className="mt-6 text-lg font-medium">Training records</h3>
      <div className="mt-2 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Title</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {records.map((record) => (
              <tr key={record.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/training-records/${record.id}`}>
                    {record.trainingNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">{record.title}</td>
                <td className="px-4 py-2">
                  {record.status}
                  {record.expired ? ' · Expired' : ''}
                </td>
              </tr>
            ))}
            {records.length === 0 && !trainingQuery.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={3}>
                  No training records in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <h3 className="mt-8 text-lg font-medium">Competency assessments</h3>
      <div className="mt-2 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Assessed on</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {assessments.map((assessment) => (
              <tr key={assessment.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/assessments/${assessment.id}`}>
                    {assessment.assessmentNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">{assessment.assessedOn}</td>
                <td className="px-4 py-2">
                  {assessment.status}
                  {assessment.result ? ` · ${assessment.result}` : ''}
                </td>
              </tr>
            ))}
            {assessments.length === 0 && !assessmentQuery.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={3}>
                  No competency assessments in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {hasPermission('TRAINING_UPDATE') && (
        <div className="mt-8 grid gap-6 lg:grid-cols-2">
          <form
            className="space-y-3 rounded-lg border border-slate-200 bg-white p-4"
            onSubmit={trainingForm.handleSubmit((values) => createT.mutate(values))}
            aria-label="Create training"
          >
            <h3 className="text-lg font-medium">New training</h3>
            <AuditorSelect auditors={auditors} field={trainingForm.register('auditorId')} />
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Title" {...trainingForm.register('title')} />
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Provider" {...trainingForm.register('provider')} />
            {trainingForm.formState.errors.root && (
              <p className="text-sm text-red-700" role="alert">
                {trainingForm.formState.errors.root.message}
              </p>
            )}
            <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
              Create training
            </button>
          </form>
          <form
            className="space-y-3 rounded-lg border border-slate-200 bg-white p-4"
            onSubmit={assessmentForm.handleSubmit((values) => createA.mutate(values))}
            aria-label="Create assessment"
          >
            <h3 className="text-lg font-medium">New assessment</h3>
            <AuditorSelect auditors={auditors} field={assessmentForm.register('auditorId')} />
            <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...assessmentForm.register('assessedOn')} />
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Assessor name" {...assessmentForm.register('assessorName')} />
            {assessmentForm.formState.errors.root && (
              <p className="text-sm text-red-700" role="alert">
                {assessmentForm.formState.errors.root.message}
              </p>
            )}
            <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
              Create assessment
            </button>
          </form>
        </div>
      )}
    </section>
  )
}

function AuditorSelect({
  auditors,
  field,
}: {
  auditors: { id: string; firstName: string; lastName: string; employeeNumber: string }[]
  field: UseFormRegisterReturn
}) {
  return (
    <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...field}>
      <option value="">Select auditor</option>
      {auditors.map((auditor) => (
        <option key={auditor.id} value={auditor.id}>
          {auditor.employeeNumber} · {auditor.firstName} {auditor.lastName}
        </option>
      ))}
    </select>
  )
}
