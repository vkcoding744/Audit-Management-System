import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { createScheme, fetchSchemes } from '../api/standards'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  code: z.string().min(1).max(64),
  name: z.string().min(1).max(255),
  accreditationBody: z.string().max(255).optional(),
  cycleMonths: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

export function SchemesPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['schemes'], queryFn: () => fetchSchemes() })
  const rows = query.data?.data?.content ?? []
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { code: '', name: '', accreditationBody: '', cycleMonths: '' },
  })
  const create = useMutation({
    mutationFn: (values: FormValues) =>
      createScheme({
        code: values.code,
        name: values.name,
        accreditationBody: emptyToUndef(values.accreditationBody),
        cycleMonths: values.cycleMonths ? Number(values.cycleMonths) : undefined,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['schemes'] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create scheme' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Schemes</h2>
      <p className="mt-1 text-sm text-slate-600">
        Certification or inspection programmes that reference one or more standards. Cycle length is in months.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have SCHEME_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      <ul className="mt-4 space-y-2">
        {rows.map((scheme) => (
          <li key={scheme.id} className="rounded-lg border border-slate-200 bg-white p-4">
            <Link className="font-medium text-brand-500 underline" to={`/schemes/${scheme.id}`}>
              {scheme.name}
            </Link>
            <p className="text-sm text-slate-500">
              {scheme.code} · {scheme.status}
              {scheme.cycleMonths ? ` · ${scheme.cycleMonths} month cycle` : ''}
            </p>
          </li>
        ))}
        {rows.length === 0 && !query.isPending && <li className="text-sm text-slate-500">No schemes in this tenant.</li>}
      </ul>
      {hasPermission('SCHEME_CREATE') && (
        <form
          className="mt-8 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create scheme"
        >
          <h3 className="text-lg font-medium">New scheme</h3>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Code" {...form.register('code')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Name" {...form.register('name')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Accreditation body" {...form.register('accreditationBody')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Cycle months" {...form.register('cycleMonths')} />
          {form.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {form.formState.errors.root.message}
            </p>
          )}
          <button type="submit" disabled={create.isPending} className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Create scheme
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
