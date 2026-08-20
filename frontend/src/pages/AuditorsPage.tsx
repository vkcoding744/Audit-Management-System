import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { createAuditor, fetchAuditors } from '../api/auditors'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  email: z.union([z.string().email(), z.literal('')]).optional(),
  jobTitle: z.string().max(128).optional(),
})

type FormValues = z.infer<typeof schema>

export function AuditorsPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['auditors'], queryFn: () => fetchAuditors() })
  const rows = query.data?.data?.content ?? []
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { firstName: '', lastName: '', email: '', jobTitle: '' },
  })
  const create = useMutation({
    mutationFn: (values: FormValues) =>
      createAuditor({
        firstName: values.firstName,
        lastName: values.lastName,
        email: emptyToUndef(values.email),
        jobTitle: emptyToUndef(values.jobTitle),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['auditors'] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create auditor' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Auditors</h2>
      <p className="mt-1 text-sm text-slate-600">
        Staff and contractor profiles used for later audit assignment. Competency expiry is evaluated here so planning cannot
        ignore lapsed records.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have AUDITOR_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Name</th>
              <th className="px-4 py-2">Title</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((auditor) => (
              <tr key={auditor.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">{auditor.employeeNumber}</td>
                <td className="px-4 py-2">
                  <Link className="text-brand-500 underline" to={`/auditors/${auditor.id}`}>
                    {auditor.firstName} {auditor.lastName}
                  </Link>
                </td>
                <td className="px-4 py-2">{auditor.jobTitle ?? '—'}</td>
                <td className="px-4 py-2">{auditor.status}</td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No auditors in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {hasPermission('AUDITOR_CREATE') && (
        <form
          className="mt-8 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create auditor"
        >
          <h3 className="text-lg font-medium">New auditor</h3>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="First name" {...form.register('firstName')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Last name" {...form.register('lastName')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Email" {...form.register('email')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Job title" {...form.register('jobTitle')} />
          {form.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {form.formState.errors.root.message}
            </p>
          )}
          <button type="submit" disabled={create.isPending} className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Create auditor
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
