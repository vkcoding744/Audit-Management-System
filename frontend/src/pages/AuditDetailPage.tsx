import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import { addAuditSite, assignAuditor, cancelAudit, fetchAudit, scheduleAudit } from '../api/audits'
import { fetchAuditors } from '../api/auditors'
import { fetchSites } from '../api/clients'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const siteSchema = z.object({ siteId: z.string().min(1) })
const assignSchema = z.object({
  auditorId: z.string().min(1),
  assignmentRole: z.enum(['LEAD', 'TEAM', 'TECHNICAL_EXPERT', 'TRAINEE', 'OBSERVER']),
})

export function AuditDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const auditQuery = useQuery({ queryKey: ['audit', id], queryFn: () => fetchAudit(id!), enabled: Boolean(id) })
  const audit = auditQuery.data?.data
  const sitesQuery = useQuery({
    queryKey: ['client-sites', audit?.clientId],
    queryFn: () => fetchSites(audit!.clientId),
    enabled: Boolean(audit?.clientId) && hasPermission('SITE_VIEW'),
  })
  const auditorsQuery = useQuery({
    queryKey: ['auditors'],
    queryFn: () => fetchAuditors(),
    enabled: hasPermission('AUDITOR_VIEW'),
  })
  const clientSites = sitesQuery.data?.data ?? []
  const auditors = auditorsQuery.data?.data?.content ?? []
  const siteForm = useForm({ resolver: zodResolver(siteSchema), defaultValues: { siteId: '' } })
  const assignForm = useForm({
    resolver: zodResolver(assignSchema),
    defaultValues: { auditorId: '', assignmentRole: 'TEAM' as const },
  })
  const addSite = useMutation({
    mutationFn: (values: z.infer<typeof siteSchema>) => addAuditSite(id!, values.siteId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['audit', id] })
      siteForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      siteForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not add site' })
    },
  })
  const assign = useMutation({
    mutationFn: (values: z.infer<typeof assignSchema>) => assignAuditor(id!, values),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['audit', id] })
      assignForm.reset({ auditorId: '', assignmentRole: 'TEAM' })
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      assignForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not assign auditor' })
    },
  })
  const schedule = useMutation({
    mutationFn: () => scheduleAudit(id!),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['audit', id] }),
  })
  const cancel = useMutation({
    mutationFn: () => cancelAudit(id!),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['audit', id] }),
  })

  if (auditQuery.isError || !id) {
    return <p className="text-sm text-red-700">Audit not found or you do not have AUDIT_VIEW.</p>
  }
  if (!audit) {
    return <p className="text-sm text-slate-600">Loading audit…</p>
  }

  const plannable = audit.status === 'PLANNED' || audit.status === 'SCHEDULED'

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to={`/programmes/${audit.programmeId}`}>
          Programme
        </Link>{' '}
        / {audit.name}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{audit.name}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {audit.auditNumber} · {audit.status} · {audit.auditType} · {audit.stage}
      </p>
      <p className="mt-1 text-sm text-slate-500">
        Planned {audit.plannedStartOn ?? '—'} → {audit.plannedEndOn ?? '—'}
      </p>
      {hasPermission('AUDIT_UPDATE') && plannable && (
        <div className="mt-4 flex gap-2">
          {audit.status === 'PLANNED' && (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => schedule.mutate()}>
              Schedule
            </button>
          )}
          {audit.status !== 'CANCELLED' && (
            <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => cancel.mutate()}>
              Cancel
            </button>
          )}
        </div>
      )}
      {schedule.isError && (
        <p className="mt-2 text-sm text-red-700">
          {(schedule.error as AxiosError<ApiResponse<unknown>>).response?.data?.error?.message ??
            'Schedule requires planned dates and a lead auditor.'}
        </p>
      )}

      <h3 className="mt-8 text-lg font-medium">Sites in scope</h3>
      <ul className="mt-2 space-y-2">
        {audit.sites.map((site) => (
          <li key={site.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            Site {site.siteId}
          </li>
        ))}
        {audit.sites.length === 0 && <li className="text-sm text-slate-500">No sites in scope.</li>}
      </ul>
      {hasPermission('AUDIT_UPDATE') && plannable && (
        <form
          className="mt-3 flex max-w-xl gap-2"
          onSubmit={siteForm.handleSubmit((values) => addSite.mutate(values))}
          aria-label="Add audit site"
        >
          <select className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm" {...siteForm.register('siteId')}>
            <option value="">Select site</option>
            {clientSites.map((site) => (
              <option key={site.id} value={site.id}>
                {site.name}
              </option>
            ))}
          </select>
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
            Add
          </button>
        </form>
      )}
      {siteForm.formState.errors.root && (
        <p className="mt-2 text-sm text-red-700" role="alert">
          {siteForm.formState.errors.root.message}
        </p>
      )}

      <h3 className="mt-8 text-lg font-medium">Team</h3>
      <ul className="mt-2 space-y-2">
        {audit.assignments.map((assignment) => (
          <li key={assignment.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            {assignment.assignmentRole} · auditor {assignment.auditorId}
          </li>
        ))}
        {audit.assignments.length === 0 && <li className="text-sm text-slate-500">No assignments yet.</li>}
      </ul>
      {hasPermission('AUDIT_ASSIGN') && plannable && (
        <form
          className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={assignForm.handleSubmit((values) => assign.mutate(values))}
          aria-label="Assign auditor"
        >
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...assignForm.register('auditorId')}>
            <option value="">Select auditor</option>
            {auditors.map((auditor) => (
              <option key={auditor.id} value={auditor.id}>
                {auditor.firstName} {auditor.lastName} ({auditor.employeeNumber})
              </option>
            ))}
          </select>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...assignForm.register('assignmentRole')}>
            <option value="LEAD">Lead</option>
            <option value="TEAM">Team</option>
            <option value="TECHNICAL_EXPERT">Technical expert</option>
            <option value="TRAINEE">Trainee</option>
            <option value="OBSERVER">Observer</option>
          </select>
          {assignForm.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {assignForm.formState.errors.root.message}
            </p>
          )}
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
            Assign
          </button>
        </form>
      )}
    </section>
  )
}
