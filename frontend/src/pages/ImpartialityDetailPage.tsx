import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useParams } from 'react-router-dom'
import { closeImpartiality, fetchImpartialityRecord, reviewImpartiality } from '../api/governance'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function ImpartialityDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({
    queryKey: ['impartiality-record', id],
    queryFn: () => fetchImpartialityRecord(id!),
    enabled: Boolean(id),
  })
  const record = query.data?.data
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['impartiality-record', id] })
    void queryClient.invalidateQueries({ queryKey: ['impartiality'] })
  }
  const review = useMutation({ mutationFn: () => reviewImpartiality(id!), onSuccess: invalidate })
  const close = useMutation({ mutationFn: () => closeImpartiality(id!), onSuccess: invalidate })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Impartiality record not found or you do not have RISK_VIEW.</p>
  }
  if (!record) {
    return <p className="text-sm text-slate-600">Loading impartiality record…</p>
  }
  const actionError = (review.error ?? close.error) as AxiosError<ApiResponse<unknown>> | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/governance">
          Governance
        </Link>{' '}
        / {record.impartialityNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{record.title}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {record.status} · identified {record.identifiedOn}
      </p>
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The impartiality action was rejected.'}</p>
      )}
      {record.status !== 'CLOSED' && hasPermission('RISK_UPDATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          {record.status === 'OPEN' && (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => review.mutate()}>
              Record review
            </button>
          )}
          <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => close.mutate()}>
            Close
          </button>
        </div>
      )}
    </section>
  )
}
