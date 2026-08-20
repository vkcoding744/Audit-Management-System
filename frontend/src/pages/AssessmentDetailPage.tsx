import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useParams } from 'react-router-dom'
import { completeAssessment, fetchAssessment } from '../api/training'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function AssessmentDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['assessment', id], queryFn: () => fetchAssessment(id!), enabled: Boolean(id) })
  const assessment = query.data?.data

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['assessment', id] })
    void queryClient.invalidateQueries({ queryKey: ['assessments'] })
  }

  const pass = useMutation({ mutationFn: () => completeAssessment(id!, 'PASS'), onSuccess: invalidate })
  const fail = useMutation({ mutationFn: () => completeAssessment(id!, 'FAIL'), onSuccess: invalidate })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Assessment not found or you do not have TRAINING_VIEW.</p>
  }
  if (!assessment) {
    return <p className="text-sm text-slate-600">Loading assessment…</p>
  }

  const actionError = (pass.error ?? fail.error) as AxiosError<ApiResponse<unknown>> | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/training">
          Training
        </Link>{' '}
        / {assessment.assessmentNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{assessment.assessmentNumber}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {assessment.status}
        {assessment.result ? ` · ${assessment.result}` : ''} · assessed {assessment.assessedOn}
        {assessment.assessorName ? ` · ${assessment.assessorName}` : ''}
      </p>
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The assessment action was rejected.'}</p>
      )}
      {assessment.status === 'DRAFT' && hasPermission('TRAINING_UPDATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => pass.mutate()}>
            Record pass
          </button>
          <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => fail.mutate()}>
            Record fail
          </button>
        </div>
      )}
    </section>
  )
}
