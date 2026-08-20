import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { createClient, fetchClients } from '../api/clients'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  legalName: z.string().min(1, 'Legal name is required').max(255),
  tradingName: z.string().max(255).optional(),
  registrationNumber: z.string().max(64).optional(),
  industry: z.string().max(128).optional(),
  email: z.union([z.string().email(), z.literal('')]).optional(),
  phone: z.string().max(64).optional(),
  country: z.string().max(128).optional(),
})

type FormValues = z.infer<typeof schema>

export function ClientsPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const query = useQuery({ queryKey: ['clients', search], queryFn: () => fetchClients(search || undefined) })
  const rows = query.data?.data?.content ?? []
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      legalName: '',
      tradingName: '',
      registrationNumber: '',
      industry: '',
      email: '',
      phone: '',
      country: '',
    },
  })
  const create = useMutation({
    mutationFn: (values: FormValues) =>
      createClient({
        legalName: values.legalName,
        tradingName: emptyToUndef(values.tradingName),
        registrationNumber: emptyToUndef(values.registrationNumber),
        industry: emptyToUndef(values.industry),
        email: emptyToUndef(values.email),
        phone: emptyToUndef(values.phone),
        country: emptyToUndef(values.country),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['clients'] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', {
        message: error.response?.data?.error?.message ?? 'Could not create client',
      })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Clients</h2>
      <p className="mt-1 text-sm text-slate-600">
        Organisations under audit or certification. Numbers are assigned per tenant. Platform administrators must select a
        tenant in the header before listing or creating clients.
      </p>

      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have CLIENT_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}

      <div className="mt-4 flex gap-2">
        <input
          type="search"
          placeholder="Search name or client number"
          className="w-full max-w-md rounded-md border border-slate-300 px-3 py-2 text-sm"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          aria-label="Search clients"
        />
      </div>

      <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Legal name</th>
              <th className="px-4 py-2">Industry</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((client) => (
              <tr key={client.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">{client.clientNumber}</td>
                <td className="px-4 py-2">
                  <Link className="text-brand-500 underline" to={`/clients/${client.id}`}>
                    {client.legalName}
                  </Link>
                </td>
                <td className="px-4 py-2">{client.industry ?? '—'}</td>
                <td className="px-4 py-2">{client.status}</td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No clients in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {hasPermission('CLIENT_CREATE') && (
        <form
          className="mt-8 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create client"
        >
          <h3 className="text-lg font-medium">New client</h3>
          <label className="block text-sm font-medium">
            Legal name
            <input className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('legalName')} />
          </label>
          <label className="block text-sm font-medium">
            Trading name
            <input className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('tradingName')} />
          </label>
          <label className="block text-sm font-medium">
            Registration number
            <input className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('registrationNumber')} />
          </label>
          <label className="block text-sm font-medium">
            Industry
            <input className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('industry')} />
          </label>
          <label className="block text-sm font-medium">
            Email
            <input type="email" className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('email')} />
          </label>
          <label className="block text-sm font-medium">
            Phone
            <input className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('phone')} />
          </label>
          <label className="block text-sm font-medium">
            Country
            <input className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('country')} />
          </label>
          {form.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {form.formState.errors.root.message}
            </p>
          )}
          <button
            type="submit"
            disabled={create.isPending}
            className="rounded-md bg-brand-500 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
          >
            {create.isPending ? 'Saving…' : 'Create client'}
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
