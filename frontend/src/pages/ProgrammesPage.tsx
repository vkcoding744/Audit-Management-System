import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { createProgramme, fetchProgrammes } from '../api/audits'
import { fetchClients } from '../api/clients'
import { fetchSchemes } from '../api/standards'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  name: z.string().min(1).max(255),
  clientId: z.string().min(1),
  schemeId: z.string().min(1),
})

type FormValues = z.infer<typeof schema>

export function ProgrammesPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['programmes'], queryFn: () => fetchProgrammes() })
  const clientsQuery = useQuery({
    queryKey: ['clients'],
    queryFn: () => fetchClients(),
    enabled: hasPermission('CLIENT_VIEW'),
  })
  const schemesQuery = useQuery({
    queryKey: ['schemes'],
    queryFn: () => fetchSchemes(),
    enabled: hasPermission('SCHEME_VIEW'),
  })
  const rows = query.data?.data?.content ?? []
  const clients = clientsQuery.data?.data?.content ?? []
  const schemes = schemesQuery.data?.data?.content ?? []
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { name: '', clientId: '', schemeId: '' },
  })
  const create = useMutation({
    mutationFn: (values: FormValues) => createProgramme(values),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['programmes'] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create programme' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Audit programmes</h2>
      <p className="mt-1 text-sm text-slate-600">
        Plan certification cycles for a client and scheme. Individual visits are scheduled as audits under a programme.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have AUDIT_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Name</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2">Cycle</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((programme) => (
              <tr key={programme.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">{programme.programmeNumber}</td>
                <td className="px-4 py-2">
                  <Link className="text-brand-500 underline" to={`/programmes/${programme.id}`}>
                    {programme.name}
                  </Link>
                </td>
                <td className="px-4 py-2">{programme.status}</td>
                <td className="px-4 py-2">
                  {programme.cycleStartOn ?? '—'} → {programme.cycleEndOn ?? '—'}
                </td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No programmes in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {hasPermission('AUDIT_CREATE') && (
        <form
          className="mt-8 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create programme"
        >
          <h3 className="text-lg font-medium">New programme</h3>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Name" {...form.register('name')} />
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('clientId')}>
            <option value="">Select client</option>
            {clients.map((client) => (
              <option key={client.id} value={client.id}>
                {client.legalName}
              </option>
            ))}
          </select>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('schemeId')}>
            <option value="">Select scheme</option>
            {schemes.map((scheme) => (
              <option key={scheme.id} value={scheme.id}>
                {scheme.code} · {scheme.name}
              </option>
            ))}
          </select>
          {form.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {form.formState.errors.root.message}
            </p>
          )}
          <button type="submit" disabled={create.isPending} className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Create programme
          </button>
        </form>
      )}
    </section>
  )
}
