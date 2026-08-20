import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import { convertLead, fetchLead, loseLead, qualifyLead } from '../api/leads'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const loseSchema = z.object({ reason: z.string().min(1) })

export function LeadDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['lead', id], queryFn: () => fetchLead(id!), enabled: Boolean(id) })
  const lead = query.data?.data
  const loseForm = useForm({ resolver: zodResolver(loseSchema), defaultValues: { reason: '' } })

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['lead', id] })
    void queryClient.invalidateQueries({ queryKey: ['leads'] })
    void queryClient.invalidateQueries({ queryKey: ['clients'] })
  }

  const qualify = useMutation({ mutationFn: () => qualifyLead(id!), onSuccess: invalidate })
  const convert = useMutation({ mutationFn: () => convertLead(id!), onSuccess: invalidate })
  const lose = useMutation({
    mutationFn: (reason: string) => loseLead(id!, reason),
    onSuccess: () => {
      invalidate()
      loseForm.reset()
    },
  })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Lead not found or you do not have LEAD_VIEW.</p>
  }
  if (!lead) {
    return <p className="text-sm text-slate-600">Loading lead…</p>
  }

  const actionError = (qualify.error ?? convert.error ?? lose.error) as AxiosError<ApiResponse<unknown>> | undefined
  const editable = lead.status === 'OPEN' || lead.status === 'QUALIFIED'

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/leads">
          Leads
        </Link>{' '}
        / {lead.leadNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{lead.organisationName}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {lead.status} · {lead.source}
        {lead.contactName ? ` · ${lead.contactName}` : ''}
      </p>
      {lead.convertedClientId && (
        <p className="mt-2 text-sm">
          Converted to{' '}
          <Link className="text-brand-500 underline" to={`/clients/${lead.convertedClientId}`}>
            client
          </Link>
        </p>
      )}
      {lead.lostReason && <p className="mt-2 text-sm text-slate-600">Lost: {lead.lostReason}</p>}
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The lead action was rejected.'}</p>
      )}
      {editable && hasPermission('LEAD_UPDATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          {lead.status === 'OPEN' && (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => qualify.mutate()}>
              Qualify
            </button>
          )}
          {hasPermission('CLIENT_CREATE') && (
            <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => convert.mutate()}>
              Convert to client
            </button>
          )}
        </div>
      )}
      {editable && hasPermission('LEAD_UPDATE') && (
        <form
          className="mt-6 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={loseForm.handleSubmit((values) => lose.mutate(values.reason))}
          aria-label="Mark lead lost"
        >
          <h3 className="text-lg font-medium">Mark lost</h3>
          <textarea className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" rows={2} placeholder="Reason" {...loseForm.register('reason')} />
          <button type="submit" className="rounded-md border border-red-300 px-3 py-1 text-sm text-red-700">
            Mark lost
          </button>
        </form>
      )}
    </section>
  )
}
