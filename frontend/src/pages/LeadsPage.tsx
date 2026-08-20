import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { createLead, fetchLeads } from '../api/leads'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  organisationName: z.string().min(1),
  contactName: z.string().optional(),
  email: z.union([z.string().email(), z.literal('')]).optional(),
  phone: z.string().optional(),
  source: z.enum(['WEBSITE', 'REFERRAL', 'TENDER', 'EVENT', 'OTHER']),
})

export function LeadsPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['leads'], queryFn: () => fetchLeads() })
  const rows = query.data?.data?.content ?? []
  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: { organisationName: '', contactName: '', email: '', phone: '', source: 'OTHER' as const },
  })
  const create = useMutation({
    mutationFn: (values: z.infer<typeof schema>) =>
      createLead({
        organisationName: values.organisationName,
        contactName: values.contactName || undefined,
        email: values.email || undefined,
        phone: values.phone || undefined,
        source: values.source,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['leads'] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create lead' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Leads</h2>
      <p className="mt-1 text-sm text-slate-600">
        Sales pipeline before a client record exists. Qualify, mark lost, or convert an open lead into a prospect client.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have LEAD_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Organisation</th>
              <th className="px-4 py-2">Source</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((lead) => (
              <tr key={lead.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/leads/${lead.id}`}>
                    {lead.leadNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">{lead.organisationName}</td>
                <td className="px-4 py-2">{lead.source}</td>
                <td className="px-4 py-2">{lead.status}</td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No leads in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {hasPermission('LEAD_CREATE') && (
        <form
          className="mt-8 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create lead"
        >
          <h3 className="text-lg font-medium">New lead</h3>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Organisation" {...form.register('organisationName')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Contact name" {...form.register('contactName')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Email" {...form.register('email')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Phone" {...form.register('phone')} />
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('source')}>
            <option value="WEBSITE">Website</option>
            <option value="REFERRAL">Referral</option>
            <option value="TENDER">Tender</option>
            <option value="EVENT">Event</option>
            <option value="OTHER">Other</option>
          </select>
          {form.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {form.formState.errors.root.message}
            </p>
          )}
          <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Create lead
          </button>
        </form>
      )}
    </section>
  )
}
