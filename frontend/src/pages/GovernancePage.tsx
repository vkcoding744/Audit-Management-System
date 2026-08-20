import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { fetchClients } from '../api/clients'
import {
  createAppeal,
  createComplaint,
  createImpartiality,
  createRisk,
  fetchAppeals,
  fetchComplaints,
  fetchImpartiality,
  fetchRisks,
} from '../api/governance'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const complaintSchema = z.object({ clientId: z.string().optional(), subject: z.string().min(1) })
const appealSchema = z.object({ clientId: z.string().optional(), subject: z.string().min(1) })
const riskSchema = z.object({ title: z.string().min(1), likelihood: z.string().optional(), impact: z.string().optional() })
const impartialitySchema = z.object({ title: z.string().min(1) })

export function GovernancePage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const complaintsQuery = useQuery({ queryKey: ['complaints'], queryFn: fetchComplaints, enabled: hasPermission('COMPLAINT_VIEW') })
  const appealsQuery = useQuery({ queryKey: ['appeals'], queryFn: fetchAppeals, enabled: hasPermission('APPEAL_VIEW') })
  const risksQuery = useQuery({ queryKey: ['risks'], queryFn: fetchRisks, enabled: hasPermission('RISK_VIEW') })
  const impartialityQuery = useQuery({
    queryKey: ['impartiality'],
    queryFn: fetchImpartiality,
    enabled: hasPermission('RISK_VIEW'),
  })
  const clientsQuery = useQuery({
    queryKey: ['clients'],
    queryFn: () => fetchClients(),
    enabled: hasPermission('CLIENT_VIEW') && (hasPermission('COMPLAINT_UPDATE') || hasPermission('APPEAL_UPDATE')),
  })
  const complaints = complaintsQuery.data?.data?.content ?? []
  const appeals = appealsQuery.data?.data?.content ?? []
  const risks = risksQuery.data?.data?.content ?? []
  const impartiality = impartialityQuery.data?.data?.content ?? []
  const clients = clientsQuery.data?.data?.content ?? []

  const complaintForm = useForm({ resolver: zodResolver(complaintSchema), defaultValues: { clientId: '', subject: '' } })
  const appealForm = useForm({ resolver: zodResolver(appealSchema), defaultValues: { clientId: '', subject: '' } })
  const riskForm = useForm({ resolver: zodResolver(riskSchema), defaultValues: { title: '', likelihood: '', impact: '' } })
  const impartialityForm = useForm({ resolver: zodResolver(impartialitySchema), defaultValues: { title: '' } })

  const createC = useMutation({
    mutationFn: (values: z.infer<typeof complaintSchema>) =>
      createComplaint({ subject: values.subject, clientId: values.clientId || undefined }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['complaints'] })
      complaintForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      complaintForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create complaint' })
    },
  })
  const createA = useMutation({
    mutationFn: (values: z.infer<typeof appealSchema>) =>
      createAppeal({ subject: values.subject, clientId: values.clientId || undefined }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['appeals'] })
      appealForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      appealForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create appeal' })
    },
  })
  const createR = useMutation({
    mutationFn: (values: z.infer<typeof riskSchema>) =>
      createRisk({
        title: values.title,
        likelihood: values.likelihood ? Number(values.likelihood) : undefined,
        impact: values.impact ? Number(values.impact) : undefined,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['risks'] })
      riskForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      riskForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create risk' })
    },
  })
  const createI = useMutation({
    mutationFn: (values: z.infer<typeof impartialitySchema>) => createImpartiality({ title: values.title }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['impartiality'] })
      impartialityForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      impartialityForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create record' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Governance</h2>
      <p className="mt-1 text-sm text-slate-600">
        Complaints, appeals, risks, and impartiality records. Closed complaints and decided appeals cannot be changed.
        Client dashboard open-complaint and open-appeal counts come from these rows.
      </p>
      {(complaintsQuery.isError || appealsQuery.isError) && (
        <p className="mt-4 text-sm text-red-700">You do not have governance view permission, tenant scope is missing, or the API is unavailable.</p>
      )}

      <h3 className="mt-6 text-lg font-medium">Complaints</h3>
      <Directory rows={complaints} empty="No complaints in this tenant." numberKey="complaintNumber" to={(id) => `/complaints/${id}`} />
      {hasPermission('COMPLAINT_UPDATE') && (
        <form className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4" onSubmit={complaintForm.handleSubmit((v) => createC.mutate(v))} aria-label="Create complaint">
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...complaintForm.register('clientId')}>
            <option value="">Client (optional)</option>
            {clients.map((client) => (
              <option key={client.id} value={client.id}>
                {client.clientNumber} · {client.legalName}
              </option>
            ))}
          </select>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Subject" {...complaintForm.register('subject')} />
          {complaintForm.formState.errors.root && <p className="text-sm text-red-700">{complaintForm.formState.errors.root.message}</p>}
          <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">Create complaint</button>
        </form>
      )}

      <h3 className="mt-8 text-lg font-medium">Appeals</h3>
      <Directory rows={appeals} empty="No appeals in this tenant." numberKey="appealNumber" to={(id) => `/appeals/${id}`} />
      {hasPermission('APPEAL_UPDATE') && (
        <form className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4" onSubmit={appealForm.handleSubmit((v) => createA.mutate(v))} aria-label="Create appeal">
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...appealForm.register('clientId')}>
            <option value="">Client (optional)</option>
            {clients.map((client) => (
              <option key={client.id} value={client.id}>
                {client.clientNumber} · {client.legalName}
              </option>
            ))}
          </select>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Subject" {...appealForm.register('subject')} />
          {appealForm.formState.errors.root && <p className="text-sm text-red-700">{appealForm.formState.errors.root.message}</p>}
          <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">Create appeal</button>
        </form>
      )}

      <h3 className="mt-8 text-lg font-medium">Risks</h3>
      <Directory rows={risks} empty="No risks in this tenant." numberKey="riskNumber" titleKey="title" to={(id) => `/risks/${id}`} />
      {hasPermission('RISK_UPDATE') && (
        <form className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4" onSubmit={riskForm.handleSubmit((v) => createR.mutate(v))} aria-label="Create risk">
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Title" {...riskForm.register('title')} />
          <div className="grid grid-cols-2 gap-2">
            <input className="rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Likelihood 1-5" {...riskForm.register('likelihood')} />
            <input className="rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Impact 1-5" {...riskForm.register('impact')} />
          </div>
          {riskForm.formState.errors.root && <p className="text-sm text-red-700">{riskForm.formState.errors.root.message}</p>}
          <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">Create risk</button>
        </form>
      )}

      <h3 className="mt-8 text-lg font-medium">Impartiality</h3>
      <Directory rows={impartiality} empty="No impartiality records in this tenant." numberKey="impartialityNumber" titleKey="title" to={(id) => `/impartiality/${id}`} />
      {hasPermission('RISK_UPDATE') && (
        <form className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4" onSubmit={impartialityForm.handleSubmit((v) => createI.mutate(v))} aria-label="Create impartiality">
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Title" {...impartialityForm.register('title')} />
          {impartialityForm.formState.errors.root && <p className="text-sm text-red-700">{impartialityForm.formState.errors.root.message}</p>}
          <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">Create record</button>
        </form>
      )}
    </section>
  )
}

function Directory({
  rows,
  empty,
  numberKey,
  titleKey,
  to,
}: {
  rows: { id: string; status: string }[]
  empty: string
  numberKey: 'complaintNumber' | 'appealNumber' | 'riskNumber' | 'impartialityNumber'
  titleKey?: 'title' | 'subject'
  to: (id: string) => string
}) {
  return (
    <div className="mt-2 overflow-x-auto rounded-lg border border-slate-200 bg-white">
      <table className="min-w-full text-left text-sm">
        <thead className="bg-slate-50 text-slate-600">
          <tr>
            <th className="px-4 py-2">Number</th>
            {titleKey && <th className="px-4 py-2">Title</th>}
            <th className="px-4 py-2">Status</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const record = row as Record<string, string>
            return (
              <tr key={row.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={to(row.id)}>
                    {record[numberKey]}
                  </Link>
                </td>
                {titleKey && <td className="px-4 py-2">{record[titleKey] ?? ''}</td>}
                <td className="px-4 py-2">{row.status}</td>
              </tr>
            )
          })}
          {rows.length === 0 && (
            <tr>
              <td className="px-4 py-6 text-slate-500" colSpan={titleKey ? 3 : 2}>
                {empty}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
