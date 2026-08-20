import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useParams } from 'react-router-dom'
import { closeRisk, fetchRisk, mitigateRisk } from '../api/governance'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function RiskDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['risk', id], queryFn: () => fetchRisk(id!), enabled: Boolean(id) })
  const risk = query.data?.data
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['risk', id] })
    void queryClient.invalidateQueries({ queryKey: ['risks'] })
  }
  const mitigate = useMutation({ mutationFn: () => mitigateRisk(id!), onSuccess: invalidate })
  const close = useMutation({ mutationFn: () => closeRisk(id!), onSuccess: invalidate })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Risk not found or you do not have RISK_VIEW.</p>
  }
  if (!risk) {
    return <p className="text-sm text-slate-600">Loading risk…</p>
  }
  const actionError = (mitigate.error ?? close.error) as AxiosError<ApiResponse<unknown>> | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/governance">
          Governance
        </Link>{' '}
        / {risk.riskNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{risk.title}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {risk.status} · {risk.category}
        {risk.score != null ? ` · score ${risk.score}` : ''}
      </p>
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The risk action was rejected.'}</p>
      )}
      {risk.status !== 'CLOSED' && hasPermission('RISK_UPDATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          {risk.status === 'OPEN' && (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => mitigate.mutate()}>
              Start mitigation
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
