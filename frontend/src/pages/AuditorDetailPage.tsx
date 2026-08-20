import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import {
  createAvailability,
  createCompetency,
  createQualification,
  fetchAuditor,
  fetchAvailability,
  fetchCompetencies,
  fetchEligibility,
  fetchQualifications,
} from '../api/auditors'
import { fetchStandards } from '../api/standards'
import { useAuth } from '../auth/AuthProvider'

const qualificationSchema = z.object({
  title: z.string().min(1).max(255),
  issuer: z.string().max(255).optional(),
})
const competencySchema = z.object({
  standardId: z.string().optional(),
  validFrom: z.string().min(1),
  validTo: z.string().optional(),
  competencyRole: z.enum(['LEAD', 'TEAM', 'TECHNICAL_EXPERT', 'TRAINEE']),
})
const availabilitySchema = z.object({
  startOn: z.string().min(1),
  endOn: z.string().min(1),
  kind: z.enum(['AVAILABLE', 'UNAVAILABLE']),
  reason: z.string().max(255).optional(),
})
const eligibilitySchema = z.object({
  standardId: z.string().min(1),
  on: z.string().min(1),
})

export function AuditorDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const auditorQuery = useQuery({ queryKey: ['auditor', id], queryFn: () => fetchAuditor(id!), enabled: Boolean(id) })
  const qualificationsQuery = useQuery({
    queryKey: ['qualifications', id],
    queryFn: () => fetchQualifications(id!),
    enabled: Boolean(id),
  })
  const competenciesQuery = useQuery({
    queryKey: ['competencies', id],
    queryFn: () => fetchCompetencies(id!),
    enabled: Boolean(id),
  })
  const availabilityQuery = useQuery({
    queryKey: ['availability', id],
    queryFn: () => fetchAvailability(id!),
    enabled: Boolean(id),
  })
  const standardsQuery = useQuery({
    queryKey: ['standards'],
    queryFn: () => fetchStandards(),
    enabled: hasPermission('STANDARD_VIEW'),
  })
  const auditor = auditorQuery.data?.data
  const qualifications = qualificationsQuery.data?.data ?? []
  const competencies = competenciesQuery.data?.data ?? []
  const windows = availabilityQuery.data?.data ?? []
  const standards = standardsQuery.data?.data?.content ?? []

  const qualificationForm = useForm({ resolver: zodResolver(qualificationSchema), defaultValues: { title: '', issuer: '' } })
  const competencyForm = useForm({
    resolver: zodResolver(competencySchema),
    defaultValues: { standardId: '', validFrom: '', validTo: '', competencyRole: 'TEAM' as const },
  })
  const availabilityForm = useForm({
    resolver: zodResolver(availabilitySchema),
    defaultValues: { startOn: '', endOn: '', kind: 'UNAVAILABLE' as const, reason: '' },
  })
  const eligibilityForm = useForm({ resolver: zodResolver(eligibilitySchema), defaultValues: { standardId: '', on: '' } })

  const addQualification = useMutation({
    mutationFn: (values: z.infer<typeof qualificationSchema>) =>
      createQualification(id!, { title: values.title, issuer: emptyToUndef(values.issuer) }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['qualifications', id] })
      qualificationForm.reset()
    },
  })
  const addCompetency = useMutation({
    mutationFn: (values: z.infer<typeof competencySchema>) =>
      createCompetency(id!, {
        standardId: emptyToUndef(values.standardId),
        competencyRole: values.competencyRole,
        validFrom: values.validFrom,
        validTo: emptyToUndef(values.validTo),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['competencies', id] })
      competencyForm.reset({ standardId: '', validFrom: '', validTo: '', competencyRole: 'TEAM' })
    },
  })
  const addAvailability = useMutation({
    mutationFn: (values: z.infer<typeof availabilitySchema>) =>
      createAvailability(id!, {
        startOn: values.startOn,
        endOn: values.endOn,
        kind: values.kind,
        reason: emptyToUndef(values.reason),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['availability', id] })
      availabilityForm.reset({ startOn: '', endOn: '', kind: 'UNAVAILABLE', reason: '' })
    },
  })
  const eligibility = useMutation({
    mutationFn: (values: z.infer<typeof eligibilitySchema>) =>
      fetchEligibility(id!, { standardId: values.standardId, on: values.on }),
  })

  if (auditorQuery.isError || !id) {
    return <p className="text-sm text-red-700">Auditor not found or you do not have AUDITOR_VIEW.</p>
  }
  if (!auditor) {
    return <p className="text-sm text-slate-600">Loading auditor…</p>
  }

  const eligibilityData = eligibility.data?.data

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/auditors">
          Auditors
        </Link>{' '}
        / {auditor.firstName} {auditor.lastName}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">
        {auditor.firstName} {auditor.lastName}
      </h2>
      <p className="mt-1 text-sm text-slate-600">
        {auditor.employeeNumber} · {auditor.status} · {auditor.employmentType}
        {auditor.jobTitle ? ` · ${auditor.jobTitle}` : ''}
        {hasPermission('TRAINING_VIEW') ? (
          <>
            {' '}
            ·{' '}
            <Link className="text-brand-500 underline" to="/training">
              Training
            </Link>
          </>
        ) : null}
      </p>

      <h3 className="mt-8 text-lg font-medium">Qualifications</h3>
      <ul className="mt-2 space-y-2">
        {qualifications.map((item) => (
          <li key={item.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            {item.title}
            {item.issuer ? ` · ${item.issuer}` : ''}
          </li>
        ))}
        {qualifications.length === 0 && <li className="text-sm text-slate-500">No qualifications recorded.</li>}
      </ul>
      {hasPermission('AUDITOR_UPDATE') && (
        <form
          className="mt-3 flex max-w-xl gap-2"
          onSubmit={qualificationForm.handleSubmit((values) => addQualification.mutate(values))}
          aria-label="Add qualification"
        >
          <input className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Title" {...qualificationForm.register('title')} />
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
            Add
          </button>
        </form>
      )}

      <h3 className="mt-8 text-lg font-medium">Competencies</h3>
      <ul className="mt-2 space-y-2">
        {competencies.map((item) => (
          <li key={item.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            <p className="font-medium">
              {item.competencyRole} · {item.status}
              {item.expired ? ' · Expired' : item.current ? ' · Current' : ''}
            </p>
            <p className="text-slate-500">
              {item.validFrom} → {item.validTo ?? 'open'}
            </p>
          </li>
        ))}
        {competencies.length === 0 && <li className="text-sm text-slate-500">No competencies recorded.</li>}
      </ul>
      {hasPermission('AUDITOR_UPDATE') && (
        <form
          className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={competencyForm.handleSubmit((values) => addCompetency.mutate(values))}
          aria-label="Add competency"
        >
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...competencyForm.register('standardId')}>
            <option value="">Select standard</option>
            {standards.map((standard) => (
              <option key={standard.id} value={standard.id}>
                {standard.code} · {standard.name}
              </option>
            ))}
          </select>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...competencyForm.register('competencyRole')}>
            <option value="LEAD">Lead</option>
            <option value="TEAM">Team</option>
            <option value="TECHNICAL_EXPERT">Technical expert</option>
            <option value="TRAINEE">Trainee</option>
          </select>
          <label className="block text-sm">
            Valid from
            <input type="date" className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...competencyForm.register('validFrom')} />
          </label>
          <label className="block text-sm">
            Valid to
            <input type="date" className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...competencyForm.register('validTo')} />
          </label>
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
            Save competency
          </button>
        </form>
      )}

      <h3 className="mt-8 text-lg font-medium">Availability</h3>
      <ul className="mt-2 space-y-2">
        {windows.map((item) => (
          <li key={item.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            {item.kind} · {item.startOn} → {item.endOn}
            {item.reason ? ` · ${item.reason}` : ''}
          </li>
        ))}
        {windows.length === 0 && <li className="text-sm text-slate-500">No availability windows.</li>}
      </ul>
      {hasPermission('AUDITOR_UPDATE') && (
        <form
          className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={availabilityForm.handleSubmit((values) => addAvailability.mutate(values))}
          aria-label="Add availability"
        >
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...availabilityForm.register('kind')}>
            <option value="UNAVAILABLE">Unavailable</option>
            <option value="AVAILABLE">Available</option>
          </select>
          <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...availabilityForm.register('startOn')} />
          <input type="date" className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...availabilityForm.register('endOn')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Reason" {...availabilityForm.register('reason')} />
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
            Save window
          </button>
        </form>
      )}

      <h3 className="mt-8 text-lg font-medium">Assignment eligibility</h3>
      <p className="text-xs text-slate-500">Expired or missing competency blocks assignment. Audit planning will use this check.</p>
      <form
        className="mt-3 flex max-w-xl flex-wrap gap-2"
        onSubmit={eligibilityForm.handleSubmit((values) => eligibility.mutate(values))}
        aria-label="Check eligibility"
      >
        <select className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm" {...eligibilityForm.register('standardId')}>
          <option value="">Standard</option>
          {standards.map((standard) => (
            <option key={standard.id} value={standard.id}>
              {standard.code}
            </option>
          ))}
        </select>
        <input type="date" className="rounded-md border border-slate-300 px-3 py-2 text-sm" {...eligibilityForm.register('on')} />
        <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
          Check
        </button>
      </form>
      {eligibilityData && (
        <p className={`mt-2 text-sm ${eligibilityData.eligible ? 'text-emerald-700' : 'text-red-700'}`}>
          {eligibilityData.eligible ? 'Eligible' : `Not eligible: ${eligibilityData.reasons.join(', ')}`}
        </p>
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
