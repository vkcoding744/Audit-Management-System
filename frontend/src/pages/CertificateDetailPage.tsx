import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import {
  addSurveillance,
  completeSurveillance,
  fetchCertificate,
  issueCertificate,
  reinstateCertificate,
  suspendCertificate,
  withdrawCertificate,
} from '../api/certificates'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const reasonSchema = z.object({ reason: z.string().min(1) })
const surveillanceSchema = z.object({ plannedOn: z.string().min(1) })

export function CertificateDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['certificate', id], queryFn: () => fetchCertificate(id!), enabled: Boolean(id) })
  const certificate = query.data?.data
  const reasonForm = useForm({ resolver: zodResolver(reasonSchema), defaultValues: { reason: '' } })
  const surveillanceForm = useForm({ resolver: zodResolver(surveillanceSchema), defaultValues: { plannedOn: '' } })

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['certificate', id] })
    void queryClient.invalidateQueries({ queryKey: ['certificates'] })
  }

  const issue = useMutation({
    mutationFn: () => issueCertificate(id!),
    onSuccess: invalidate,
  })
  const suspend = useMutation({
    mutationFn: (reason: string) => suspendCertificate(id!, reason),
    onSuccess: () => {
      invalidate()
      reasonForm.reset()
    },
  })
  const reinstate = useMutation({
    mutationFn: (reason: string) => reinstateCertificate(id!, reason),
    onSuccess: () => {
      invalidate()
      reasonForm.reset()
    },
  })
  const withdraw = useMutation({
    mutationFn: (reason: string) => withdrawCertificate(id!, reason),
    onSuccess: () => {
      invalidate()
      reasonForm.reset()
    },
  })
  const plan = useMutation({
    mutationFn: (values: z.infer<typeof surveillanceSchema>) => addSurveillance(id!, values),
    onSuccess: () => {
      invalidate()
      surveillanceForm.reset()
    },
  })
  const completeVisit = useMutation({
    mutationFn: (visitId: string) => completeSurveillance(visitId),
    onSuccess: invalidate,
  })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Certificate not found or you do not have CERTIFICATE_VIEW.</p>
  }
  if (!certificate) {
    return <p className="text-sm text-slate-600">Loading certificate…</p>
  }

  const actionError =
    (issue.error ?? suspend.error ?? reinstate.error ?? withdraw.error) as AxiosError<ApiResponse<unknown>> | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/certificates">
          Certificates
        </Link>{' '}
        / {certificate.certificateNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{certificate.certificateNumber}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {certificate.status}
        {certificate.expired ? ' · Expired' : ''} · {certificate.validFrom} → {certificate.expiresOn}
      </p>
      {certificate.scopeText && <p className="mt-2 max-w-3xl text-sm text-slate-700">{certificate.scopeText}</p>}
      <p className="mt-2 text-sm">
        <Link className="text-brand-500 underline" to={`/audits/${certificate.auditId}`}>
          Source audit
        </Link>
      </p>

      {certificate.status === 'DRAFT' && hasPermission('CERTIFICATE_ISSUE') && (
        <button type="button" className="mt-4 rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => issue.mutate()}>
          Issue certificate
        </button>
      )}
      {actionError && (
        <p className="mt-2 text-sm text-red-700">
          {actionError.response?.data?.error?.message ?? 'The certification decision was rejected.'}
        </p>
      )}

      {(certificate.status === 'ACTIVE' || certificate.status === 'SUSPENDED') && (
        <form
          className="mt-6 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={reasonForm.handleSubmit((values) => {
            if (certificate.status === 'ACTIVE' && hasPermission('CERTIFICATE_SUSPEND')) {
              suspend.mutate(values.reason)
            } else if (certificate.status === 'SUSPENDED' && hasPermission('CERTIFICATE_ISSUE')) {
              reinstate.mutate(values.reason)
            }
          })}
          aria-label="Certification decision"
        >
          <h3 className="text-lg font-medium">Decision</h3>
          <textarea
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            rows={2}
            placeholder="Reason"
            {...reasonForm.register('reason')}
          />
          <div className="flex flex-wrap gap-2">
            {certificate.status === 'ACTIVE' && hasPermission('CERTIFICATE_SUSPEND') && (
              <button type="submit" className="rounded-md border border-slate-300 px-3 py-1 text-sm">
                Suspend
              </button>
            )}
            {certificate.status === 'SUSPENDED' && hasPermission('CERTIFICATE_ISSUE') && (
              <button type="submit" className="rounded-md border border-slate-300 px-3 py-1 text-sm">
                Reinstate
              </button>
            )}
            {hasPermission('CERTIFICATE_WITHDRAW') && (
              <button
                type="button"
                className="rounded-md border border-red-300 px-3 py-1 text-sm text-red-700"
                onClick={reasonForm.handleSubmit((values) => withdraw.mutate(values.reason))}
              >
                Withdraw
              </button>
            )}
          </div>
        </form>
      )}

      <h3 className="mt-8 text-lg font-medium">Decisions</h3>
      <ul className="mt-2 space-y-2">
        {certificate.decisions.map((decision) => (
          <li key={decision.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            {decision.decisionType} · {decision.decidedOn}
            {decision.reason ? ` · ${decision.reason}` : ''}
          </li>
        ))}
        {certificate.decisions.length === 0 && <li className="text-sm text-slate-500">No decisions recorded yet.</li>}
      </ul>

      <h3 className="mt-8 text-lg font-medium">Surveillance</h3>
      <ul className="mt-2 space-y-2">
        {certificate.surveillance.map((visit) => (
          <li key={visit.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            {visit.status} · planned {visit.plannedOn}
            {visit.completedOn ? ` · completed ${visit.completedOn}` : ''}
            {visit.status === 'PLANNED' && hasPermission('CERTIFICATE_ISSUE') && (
              <button
                type="button"
                className="ml-3 rounded-md border border-slate-300 px-2 py-0.5 text-xs"
                onClick={() => completeVisit.mutate(visit.id)}
              >
                Complete
              </button>
            )}
          </li>
        ))}
        {certificate.surveillance.length === 0 && <li className="text-sm text-slate-500">No surveillance visits planned.</li>}
      </ul>
      {certificate.status !== 'WITHDRAWN' && hasPermission('CERTIFICATE_ISSUE') && (
        <form
          className="mt-4 flex max-w-xl gap-2"
          onSubmit={surveillanceForm.handleSubmit((values) => plan.mutate(values))}
          aria-label="Plan surveillance"
        >
          <input className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm" type="date" {...surveillanceForm.register('plannedOn')} />
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
            Plan visit
          </button>
        </form>
      )}
    </section>
  )
}
