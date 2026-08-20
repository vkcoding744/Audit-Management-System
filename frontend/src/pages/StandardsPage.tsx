import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { createStandard, fetchStandards } from '../api/standards'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  code: z.string().min(1).max(64),
  name: z.string().min(1).max(255),
  publisher: z.string().max(255).optional(),
  edition: z.string().max(64).optional(),
})

type FormValues = z.infer<typeof schema>

export function StandardsPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['standards'], queryFn: () => fetchStandards() })
  const rows = query.data?.data?.content ?? []
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { code: '', name: '', publisher: '', edition: '' },
  })
  const create = useMutation({
    mutationFn: (values: FormValues) =>
      createStandard({
        code: values.code,
        name: values.name,
        publisher: emptyToUndef(values.publisher),
        edition: emptyToUndef(values.edition),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['standards'] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create standard' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Standards</h2>
      <p className="mt-1 text-sm text-slate-600">
        Tenant-owned catalogues of normative documents. Enter your own codes, titles, and clause text. This product does not
        ship copyrighted standard content.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have STANDARD_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Code</th>
              <th className="px-4 py-2">Name</th>
              <th className="px-4 py-2">Publisher</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((standard) => (
              <tr key={standard.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">{standard.code}</td>
                <td className="px-4 py-2">
                  <Link className="text-brand-500 underline" to={`/standards/${standard.id}`}>
                    {standard.name}
                  </Link>
                </td>
                <td className="px-4 py-2">{standard.publisher ?? '—'}</td>
                <td className="px-4 py-2">{standard.status}</td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No standards in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {hasPermission('STANDARD_CREATE') && (
        <form
          className="mt-8 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create standard"
        >
          <h3 className="text-lg font-medium">New standard</h3>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Code" {...form.register('code')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Name" {...form.register('name')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Publisher" {...form.register('publisher')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Edition" {...form.register('edition')} />
          {form.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {form.formState.errors.root.message}
            </p>
          )}
          <button type="submit" disabled={create.isPending} className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Create standard
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
