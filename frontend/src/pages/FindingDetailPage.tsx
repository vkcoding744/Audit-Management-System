import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import { closeFinding, completeCapa, createCapa, fetchFinding } from '../api/findings'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const capaSchema = z.object({
  description: z.string().min(1),
  dueOn: z.string().min(1),
})

export function FindingDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['finding', id], queryFn: () => fetchFinding(id!), enabled: Boolean(id) })
  const finding = query.data?.data
  const form = useForm({ resolver: zodResolver(capaSchema), defaultValues: { description: '', dueOn: '' } })
  const addCapa = useMutation({
    mutationFn: (values: z.infer<typeof capaSchema>) => createCapa(id!, values),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['finding', id] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', { message: error.response?.data?.error?.message ?? 'Could not add CAPA' })
    },
  })
  const close = useMutation({
    mutationFn: () => closeFinding(id!),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['finding', id] }),
  })
  const complete = useMutation({
    mutationFn: (capaId: string) => completeCapa(capaId),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['finding', id] }),
  })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Finding not found or you do not have AUDIT_VIEW.</p>
  }
  if (!finding) {
    return <p className="text-sm text-slate-600">Loading finding…</p>
  }

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/findings">
          Findings
        </Link>{' '}
        / {finding.findingNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{finding.title}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {finding.findingNumber} · {finding.severity} · {finding.status}
      </p>
      <p className="mt-3 max-w-3xl text-sm text-slate-700">{finding.description}</p>
      <p className="mt-2 text-sm">
        <Link className="text-brand-500 underline" to={`/audits/${finding.auditId}`}>
          Open audit
        </Link>
      </p>
      {finding.status === 'OPEN' && hasPermission('FINDING_CLOSE') && (
        <button
          type="button"
          className="mt-4 rounded-md bg-brand-500 px-3 py-1 text-sm text-white"
          onClick={() => close.mutate()}
        >
          Close finding
        </button>
      )}
      {close.isError && (
        <p className="mt-2 text-sm text-red-700">
          {(close.error as AxiosError<ApiResponse<unknown>>).response?.data?.error?.message ??
            'Major and minor findings need completed CAPA first.'}
        </p>
      )}

      <h3 className="mt-8 text-lg font-medium">CAPA</h3>
      <ul className="mt-2 space-y-2">
        {finding.capa.map((capa) => (
          <li key={capa.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            <p className="font-medium">
              {capa.capaNumber} · {capa.status} · due {capa.dueOn}
            </p>
            <p className="mt-1 text-slate-600">{capa.description}</p>
            {finding.status === 'OPEN' && capa.status === 'OPEN' && hasPermission('FINDING_UPDATE') && (
              <button
                type="button"
                className="mt-2 rounded-md border border-slate-300 px-3 py-1 text-sm"
                onClick={() => complete.mutate(capa.id)}
              >
                Complete CAPA
              </button>
            )}
          </li>
        ))}
        {finding.capa.length === 0 && <li className="text-sm text-slate-500">No corrective actions yet.</li>}
      </ul>
      {finding.status === 'OPEN' && hasPermission('FINDING_UPDATE') && (
        <form
          className="mt-4 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => addCapa.mutate(values))}
          aria-label="Add CAPA"
        >
          <h4 className="text-base font-medium">New CAPA</h4>
          <textarea
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            rows={3}
            placeholder="Action description"
            {...form.register('description')}
          />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" type="date" {...form.register('dueOn')} />
          {form.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {form.formState.errors.root.message}
            </p>
          )}
          <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Add CAPA
          </button>
        </form>
      )}
    </section>
  )
}
