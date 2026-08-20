import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import { createAudit, fetchProgramme, fetchProgrammeAudits, setProgrammeStatus } from '../api/audits'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  name: z.string().min(1).max(255),
  auditType: z.enum(['INITIAL', 'SURVEILLANCE', 'RECERTIFICATION', 'SPECIAL', 'TRANSFER']),
  stage: z.enum(['NOT_APPLICABLE', 'STAGE_1', 'STAGE_2']),
  plannedStartOn: z.string().optional(),
  plannedEndOn: z.string().optional(),
})

export function ProgrammeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const programmeQuery = useQuery({ queryKey: ['programme', id], queryFn: () => fetchProgramme(id!), enabled: Boolean(id) })
  const auditsQuery = useQuery({
    queryKey: ['programme-audits', id],
    queryFn: () => fetchProgrammeAudits(id!),
    enabled: Boolean(id),
  })
  const programme = programmeQuery.data?.data
  const audits = auditsQuery.data?.data ?? []
  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: '',
      auditType: 'INITIAL' as const,
      stage: 'NOT_APPLICABLE' as const,
      plannedStartOn: '',
      plannedEndOn: '',
    },
  })
  const create = useMutation({
    mutationFn: (values: z.infer<typeof schema>) =>
      createAudit({
        programmeId: id!,
        name: values.name,
        auditType: values.auditType,
        stage: values.stage,
        plannedStartOn: emptyToUndef(values.plannedStartOn),
        plannedEndOn: emptyToUndef(values.plannedEndOn),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['programme-audits', id] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create audit' })
    },
  })
  const status = useMutation({
    mutationFn: (action: 'activate' | 'complete' | 'cancel') => setProgrammeStatus(id!, action),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['programme', id] }),
  })

  if (programmeQuery.isError || !id) {
    return <p className="text-sm text-red-700">Programme not found or you do not have AUDIT_VIEW.</p>
  }
  if (!programme) {
    return <p className="text-sm text-slate-600">Loading programme…</p>
  }

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/programmes">
          Programmes
        </Link>{' '}
        / {programme.name}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{programme.name}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {programme.programmeNumber} · {programme.status}
      </p>
      {hasPermission('AUDIT_UPDATE') && (
        <div className="mt-4 flex gap-2">
          {programme.status === 'DRAFT' && (
            <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => status.mutate('activate')}>
              Activate
            </button>
          )}
          {programme.status === 'ACTIVE' && (
            <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => status.mutate('complete')}>
              Complete
            </button>
          )}
          {programme.status !== 'COMPLETED' && programme.status !== 'CANCELLED' && (
            <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => status.mutate('cancel')}>
              Cancel
            </button>
          )}
        </div>
      )}

      <h3 className="mt-8 text-lg font-medium">Audits</h3>
      <ul className="mt-2 space-y-2">
        {audits.map((audit) => (
          <li key={audit.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            <Link className="font-medium text-brand-500 underline" to={`/audits/${audit.id}`}>
              {audit.auditNumber} · {audit.name}
            </Link>
            <p className="text-slate-500">
              {audit.status} · {audit.auditType} · {audit.plannedStartOn ?? 'no dates'}
            </p>
          </li>
        ))}
        {audits.length === 0 && <li className="text-sm text-slate-500">No audits planned yet.</li>}
      </ul>
      {hasPermission('AUDIT_CREATE') && (
        <form
          className="mt-4 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create audit"
        >
          <h4 className="text-base font-medium">Plan an audit</h4>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Audit name" {...form.register('name')} />
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('auditType')}>
            <option value="INITIAL">Initial</option>
            <option value="SURVEILLANCE">Surveillance</option>
            <option value="RECERTIFICATION">Recertification</option>
            <option value="SPECIAL">Special</option>
            <option value="TRANSFER">Transfer</option>
          </select>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('stage')}>
            <option value="NOT_APPLICABLE">No stage</option>
            <option value="STAGE_1">Stage 1</option>
            <option value="STAGE_2">Stage 2</option>
          </select>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" type="date" {...form.register('plannedStartOn')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" type="date" {...form.register('plannedEndOn')} />
          {form.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {form.formState.errors.root.message}
            </p>
          )}
          <button type="submit" disabled={create.isPending} className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Create audit
          </button>
        </form>
      )}
    </section>
  )
}

function emptyToUndef(value?: string) {
  if (!value || value.trim() === '') {
    return undefined
  }
  return value.trim()
}
