import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import {
  createNotificationJob,
  createNotificationTemplate,
  fetchNotificationChannels,
  fetchNotificationJobs,
  fetchNotificationTemplates,
  updateNotificationChannel,
} from '../api/notifications'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const templateSchema = z.object({
  code: z.string().min(1),
  name: z.string().min(1),
  subject: z.string().min(1),
  body: z.string().min(1),
})
const jobSchema = z.object({
  toAddress: z.string().min(1),
  subject: z.string().min(1),
  body: z.string().min(1),
})

export function NotificationsPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const templatesQuery = useQuery({ queryKey: ['notification-templates'], queryFn: fetchNotificationTemplates })
  const channelsQuery = useQuery({ queryKey: ['notification-channels'], queryFn: fetchNotificationChannels })
  const jobsQuery = useQuery({ queryKey: ['notification-jobs'], queryFn: fetchNotificationJobs })
  const templates = templatesQuery.data?.data?.content ?? []
  const channels = channelsQuery.data?.data ?? []
  const jobs = jobsQuery.data?.data?.content ?? []
  const templateForm = useForm({
    resolver: zodResolver(templateSchema),
    defaultValues: { code: '', name: '', subject: '', body: '' },
  })
  const jobForm = useForm({
    resolver: zodResolver(jobSchema),
    defaultValues: { toAddress: '', subject: '', body: '' },
  })

  const createT = useMutation({
    mutationFn: (values: z.infer<typeof templateSchema>) => createNotificationTemplate(values),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['notification-templates'] })
      templateForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      templateForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create template' })
    },
  })
  const createJ = useMutation({
    mutationFn: (values: z.infer<typeof jobSchema>) => createNotificationJob(values),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['notification-jobs'] })
      jobForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      jobForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create job' })
    },
  })
  const toggleChannel = useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) => updateNotificationChannel(id, { enabled }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['notification-channels'] })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Notifications</h2>
      <p className="mt-1 text-sm text-slate-600">
        Templates, channels, and outbound jobs. Email defaults to a logging adapter; set the mail provider to smtp for
        MailHog or SES. Placeholders use {'{{name}}'} syntax. Sending is explicit — there is no background worker yet.
      </p>
      {(templatesQuery.isError || jobsQuery.isError) && (
        <p className="mt-4 text-sm text-red-700">
          You do not have NOTIFICATION_VIEW, tenant scope is missing, or the API is unavailable.
        </p>
      )}

      <h3 className="mt-6 text-lg font-medium">Channels</h3>
      <ul className="mt-2 space-y-2">
        {channels.map((channel) => (
          <li key={channel.id} className="flex items-center justify-between rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm">
            <span>
              {channel.channel} · {channel.enabled ? 'Enabled' : 'Disabled'}
            </span>
            {hasPermission('NOTIFICATION_UPDATE') && (
              <button
                type="button"
                className="rounded-md border border-slate-300 px-3 py-1 text-sm"
                onClick={() => toggleChannel.mutate({ id: channel.id, enabled: !channel.enabled })}
              >
                {channel.enabled ? 'Disable' : 'Enable'}
              </button>
            )}
          </li>
        ))}
        {channels.length === 0 && !channelsQuery.isPending && <li className="text-sm text-slate-500">No channels yet.</li>}
      </ul>

      <h3 className="mt-8 text-lg font-medium">Templates</h3>
      <div className="mt-2 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Code</th>
              <th className="px-4 py-2">Name</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {templates.map((template) => (
              <tr key={template.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/notification-templates/${template.id}`}>
                    {template.code}
                  </Link>
                </td>
                <td className="px-4 py-2">{template.name}</td>
                <td className="px-4 py-2">{template.status}</td>
              </tr>
            ))}
            {templates.length === 0 && !templatesQuery.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={3}>
                  No templates in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {hasPermission('NOTIFICATION_UPDATE') && (
        <form
          className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={templateForm.handleSubmit((values) => createT.mutate(values))}
          aria-label="Create template"
        >
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Code" {...templateForm.register('code')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Name" {...templateForm.register('name')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Subject" {...templateForm.register('subject')} />
          <textarea className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Body" rows={3} {...templateForm.register('body')} />
          {templateForm.formState.errors.root && <p className="text-sm text-red-700">{templateForm.formState.errors.root.message}</p>}
          <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Create template
          </button>
        </form>
      )}

      <h3 className="mt-8 text-lg font-medium">Jobs</h3>
      <div className="mt-2 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">To</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {jobs.map((job) => (
              <tr key={job.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/notification-jobs/${job.id}`}>
                    {job.jobNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">{job.toAddress}</td>
                <td className="px-4 py-2">
                  {job.status}
                  {job.due ? ' · Due' : ''}
                </td>
              </tr>
            ))}
            {jobs.length === 0 && !jobsQuery.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={3}>
                  No notification jobs in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {hasPermission('NOTIFICATION_UPDATE') && (
        <form
          className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={jobForm.handleSubmit((values) => createJ.mutate(values))}
          aria-label="Create job"
        >
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="To address" {...jobForm.register('toAddress')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Subject" {...jobForm.register('subject')} />
          <textarea className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Body" rows={3} {...jobForm.register('body')} />
          {jobForm.formState.errors.root && <p className="text-sm text-red-700">{jobForm.formState.errors.root.message}</p>}
          <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Queue job
          </button>
        </form>
      )}
    </section>
  )
}
