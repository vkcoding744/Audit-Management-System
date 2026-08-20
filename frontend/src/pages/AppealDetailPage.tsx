import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useParams } from 'react-router-dom'
import { decideAppeal, fetchAppeal, reviewAppeal } from '../api/governance'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function AppealDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['appeal', id], queryFn: () => fetchAppeal(id!), enabled: Boolean(id) })
  const appeal = query.data?.data
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['appeal', id] })
    void queryClient.invalidateQueries({ queryKey: ['appeals'] })
  }
  const review = useMutation({ mutationFn: () => reviewAppeal(id!), onSuccess: invalidate })
  const uphold = useMutation({ mutationFn: () => decideAppeal(id!, 'UPHELD'), onSuccess: invalidate })
  const dismiss = useMutation({ mutationFn: () => decideAppeal(id!, 'DISMISSED'), onSuccess: invalidate })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Appeal not found or you do not have APPEAL_VIEW.</p>
  }
  if (!appeal) {
    return <p className="text-sm text-slate-600">Loading appeal…</p>
  }
  const actionError = (review.error ?? uphold.error ?? dismiss.error) as AxiosError<ApiResponse<unknown>> | undefined
  const open = appeal.status === 'OPEN' || appeal.status === 'UNDER_REVIEW'

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/governance">
          Governance
        </Link>{' '}
        / {appeal.appealNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{appeal.subject}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {appeal.status}
        {appeal.outcome ? ` · ${appeal.outcome}` : ''} · received {appeal.receivedOn}
      </p>
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The appeal action was rejected.'}</p>
      )}
      {open && hasPermission('APPEAL_UPDATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          {appeal.status === 'OPEN' && (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => review.mutate()}>
              Start review
            </button>
          )}
          <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => uphold.mutate()}>
            Uphold
          </button>
          <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => dismiss.mutate()}>
            Dismiss
          </button>
        </div>
      )}
    </section>
  )
}
