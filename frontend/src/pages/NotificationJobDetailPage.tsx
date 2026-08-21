import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useParams } from 'react-router-dom'
import { cancelNotificationJob, fetchNotificationJob, sendNotificationJob } from '../api/notifications'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function NotificationJobDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({
    queryKey: ['notification-job', id],
    queryFn: () => fetchNotificationJob(id!),
    enabled: Boolean(id),
  })
  const job = query.data?.data
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['notification-job', id] })
    void queryClient.invalidateQueries({ queryKey: ['notification-jobs'] })
  }
  const send = useMutation({ mutationFn: () => sendNotificationJob(id!), onSuccess: invalidate })
  const cancel = useMutation({ mutationFn: () => cancelNotificationJob(id!), onSuccess: invalidate })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Job not found or you do not have NOTIFICATION_VIEW.</p>
  }
  if (!job) {
    return <p className="text-sm text-slate-600">Loading job…</p>
  }
  const actionError = (send.error ?? cancel.error) as AxiosError<ApiResponse<unknown>> | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/notifications">
          Notifications
        </Link>{' '}
        / {job.jobNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{job.subject}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {job.status}
        {job.due ? ' · Due' : ''} · {job.channel} · {job.toAddress}
      </p>
      {job.errorMessage && <p className="mt-2 text-sm text-red-700">{job.errorMessage}</p>}
      <pre className="mt-4 whitespace-pre-wrap rounded-lg border border-slate-200 bg-white p-4 text-sm">{job.body}</pre>
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The job action was rejected.'}</p>
      )}
      {job.status === 'QUEUED' && hasPermission('NOTIFICATION_UPDATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => send.mutate()}>
            Send
          </button>
          <button type="button" className="rounded-md border border-red-300 px-3 py-1 text-sm text-red-700" onClick={() => cancel.mutate()}>
            Cancel
          </button>
        </div>
      )}
    </section>
  )
}
