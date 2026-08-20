import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useParams } from 'react-router-dom'
import { cancelTrainingRecord, completeTrainingRecord, fetchTrainingRecord } from '../api/training'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function TrainingRecordDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['training-record', id], queryFn: () => fetchTrainingRecord(id!), enabled: Boolean(id) })
  const record = query.data?.data

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['training-record', id] })
    void queryClient.invalidateQueries({ queryKey: ['training-records'] })
  }

  const complete = useMutation({ mutationFn: () => completeTrainingRecord(id!), onSuccess: invalidate })
  const cancel = useMutation({ mutationFn: () => cancelTrainingRecord(id!), onSuccess: invalidate })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Training record not found or you do not have TRAINING_VIEW.</p>
  }
  if (!record) {
    return <p className="text-sm text-slate-600">Loading training…</p>
  }

  const actionError = (complete.error ?? cancel.error) as AxiosError<ApiResponse<unknown>> | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/training">
          Training
        </Link>{' '}
        / {record.trainingNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{record.title}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {record.status}
        {record.expired ? ' · Expired' : ''}
        {record.provider ? ` · ${record.provider}` : ''}
        {record.completedOn ? ` · completed ${record.completedOn}` : ''}
      </p>
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The training action was rejected.'}</p>
      )}
      {record.status === 'PLANNED' && hasPermission('TRAINING_UPDATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => complete.mutate()}>
            Complete
          </button>
          <button type="button" className="rounded-md border border-red-300 px-3 py-1 text-sm text-red-700" onClick={() => cancel.mutate()}>
            Cancel
          </button>
        </div>
      )}
    </section>
  )
}
