import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import { closeComplaint, fetchComplaint, reviewComplaint } from '../api/governance'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({ resolution: z.string().min(1) })

export function ComplaintDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['complaint', id], queryFn: () => fetchComplaint(id!), enabled: Boolean(id) })
  const complaint = query.data?.data
  const form = useForm({ resolver: zodResolver(schema), defaultValues: { resolution: '' } })
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['complaint', id] })
    void queryClient.invalidateQueries({ queryKey: ['complaints'] })
  }
  const review = useMutation({ mutationFn: () => reviewComplaint(id!), onSuccess: invalidate })
  const close = useMutation({
    mutationFn: (resolution: string) => closeComplaint(id!, resolution),
    onSuccess: () => {
      invalidate()
      form.reset()
    },
  })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Complaint not found or you do not have COMPLAINT_VIEW.</p>
  }
  if (!complaint) {
    return <p className="text-sm text-slate-600">Loading complaint…</p>
  }
  const actionError = (review.error ?? close.error) as AxiosError<ApiResponse<unknown>> | undefined
  const open = complaint.status === 'OPEN' || complaint.status === 'IN_REVIEW'

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/governance">
          Governance
        </Link>{' '}
        / {complaint.complaintNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{complaint.subject}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {complaint.status} · {complaint.source} · received {complaint.receivedOn}
      </p>
      {complaint.resolution && <p className="mt-2 text-sm text-slate-600">Resolution: {complaint.resolution}</p>}
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The complaint action was rejected.'}</p>
      )}
      {open && hasPermission('COMPLAINT_UPDATE') && (
        <div className="mt-4 space-y-3">
          {complaint.status === 'OPEN' && (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => review.mutate()}>
              Start review
            </button>
          )}
          <form className="flex max-w-xl gap-2" onSubmit={form.handleSubmit((values) => close.mutate(values.resolution))} aria-label="Close complaint">
            <input className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Resolution" {...form.register('resolution')} />
            <button type="submit" className="rounded-md border border-slate-300 px-3 py-1 text-sm">Close</button>
          </form>
        </div>
      )}
    </section>
  )
}
