import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useParams } from 'react-router-dom'
import { activateTemplate, deactivateTemplate, fetchNotificationTemplate } from '../api/notifications'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function NotificationTemplateDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({
    queryKey: ['notification-template', id],
    queryFn: () => fetchNotificationTemplate(id!),
    enabled: Boolean(id),
  })
  const template = query.data?.data
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['notification-template', id] })
    void queryClient.invalidateQueries({ queryKey: ['notification-templates'] })
  }
  const activate = useMutation({ mutationFn: () => activateTemplate(id!), onSuccess: invalidate })
  const deactivate = useMutation({ mutationFn: () => deactivateTemplate(id!), onSuccess: invalidate })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Template not found or you do not have NOTIFICATION_VIEW.</p>
  }
  if (!template) {
    return <p className="text-sm text-slate-600">Loading template…</p>
  }
  const actionError = (activate.error ?? deactivate.error) as AxiosError<ApiResponse<unknown>> | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/notifications">
          Notifications
        </Link>{' '}
        / {template.code}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{template.name}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {template.status} · {template.channel} · {template.eventType}
      </p>
      <p className="mt-4 text-sm">{template.subject}</p>
      <pre className="mt-2 whitespace-pre-wrap rounded-lg border border-slate-200 bg-white p-4 text-sm">{template.body}</pre>
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The template action was rejected.'}</p>
      )}
      {hasPermission('NOTIFICATION_UPDATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          {template.status === 'INACTIVE' ? (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => activate.mutate()}>
              Activate
            </button>
          ) : (
            <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => deactivate.mutate()}>
              Deactivate
            </button>
          )}
        </div>
      )}
    </section>
  )
}
