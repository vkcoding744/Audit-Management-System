import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useParams } from 'react-router-dom'
import { approveAiGeneration, fetchAiGeneration, rejectAiGeneration } from '../api/ai'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function AiGenerationDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({
    queryKey: ['ai-generation', id],
    queryFn: () => fetchAiGeneration(id!),
    enabled: Boolean(id),
  })
  const row = query.data?.data
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['ai-generation', id] })
    void queryClient.invalidateQueries({ queryKey: ['ai-generations'] })
  }
  const approve = useMutation({ mutationFn: () => approveAiGeneration(id!), onSuccess: invalidate })
  const reject = useMutation({ mutationFn: () => rejectAiGeneration(id!), onSuccess: invalidate })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Draft not found or you do not have AI_VIEW.</p>
  }
  if (!row) {
    return <p className="text-sm text-slate-600">Loading draft…</p>
  }
  const actionError = (approve.error ?? reject.error) as AxiosError<ApiResponse<unknown>> | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/ai">
          AI drafts
        </Link>{' '}
        / {row.generationNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{row.purpose}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {row.status} · {row.provider} · {row.model} · prompt {row.promptVersion}
      </p>
      <p className="mt-4 text-sm text-slate-600">Human review is mandatory. Approval does not change certificates or findings.</p>
      <h3 className="mt-4 text-sm font-medium text-slate-500">Prompt</h3>
      <pre className="mt-1 whitespace-pre-wrap rounded-lg border border-slate-200 bg-white p-4 text-sm">{row.prompt}</pre>
      <h3 className="mt-4 text-sm font-medium text-slate-500">Output</h3>
      <pre className="mt-1 whitespace-pre-wrap rounded-lg border border-slate-200 bg-white p-4 text-sm">{row.output}</pre>
      {row.errorMessage && <p className="mt-2 text-sm text-red-700">{row.errorMessage}</p>}
      {row.reviewNotes && <p className="mt-2 text-sm text-slate-600">Review notes: {row.reviewNotes}</p>}
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The review action was rejected.'}</p>
      )}
      {row.status === 'PENDING_REVIEW' && hasPermission('AI_UPDATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => approve.mutate()}>
            Approve draft
          </button>
          <button type="button" className="rounded-md border border-red-300 px-3 py-1 text-sm text-red-700" onClick={() => reject.mutate()}>
            Reject
          </button>
        </div>
      )}
    </section>
  )
}
