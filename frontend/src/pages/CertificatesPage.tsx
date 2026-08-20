import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { fetchAudits } from '../api/audits'
import { createCertificate, fetchCertificates } from '../api/certificates'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  auditId: z.string().min(1),
  expiresOn: z.string().min(1),
  scopeText: z.string().optional(),
})

export function CertificatesPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['certificates'], queryFn: () => fetchCertificates() })
  const auditsQuery = useQuery({
    queryKey: ['audits', 'COMPLETED'],
    queryFn: () => fetchAudits('COMPLETED'),
    enabled: hasPermission('CERTIFICATE_ISSUE') && hasPermission('AUDIT_VIEW'),
  })
  const rows = query.data?.data?.content ?? []
  const audits = auditsQuery.data?.data?.content ?? []
  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: { auditId: '', expiresOn: '', scopeText: '' },
  })
  const create = useMutation({
    mutationFn: (values: z.infer<typeof schema>) =>
      createCertificate({
        auditId: values.auditId,
        expiresOn: values.expiresOn,
        scopeText: values.scopeText || undefined,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['certificates'] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create certificate' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Certificates</h2>
      <p className="mt-1 text-sm text-slate-600">
        Drafts are based on a completed audit. Issue is blocked while major or minor findings remain open.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have CERTIFICATE_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2">Valid</th>
              <th className="px-4 py-2">Expiry</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((certificate) => (
              <tr key={certificate.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/certificates/${certificate.id}`}>
                    {certificate.certificateNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">
                  {certificate.status}
                  {certificate.expired ? ' · Expired' : ''}
                </td>
                <td className="px-4 py-2">{certificate.validFrom}</td>
                <td className="px-4 py-2">{certificate.expiresOn}</td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No certificates in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {hasPermission('CERTIFICATE_ISSUE') && (
        <form
          className="mt-8 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create certificate"
        >
          <h3 className="text-lg font-medium">New draft certificate</h3>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('auditId')}>
            <option value="">Select completed audit</option>
            {audits.map((audit) => (
              <option key={audit.id} value={audit.id}>
                {audit.auditNumber} · {audit.name}
              </option>
            ))}
          </select>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" type="date" {...form.register('expiresOn')} />
          <textarea
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            rows={2}
            placeholder="Scope"
            {...form.register('scopeText')}
          />
          {form.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {form.formState.errors.root.message}
            </p>
          )}
          <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Create draft
          </button>
        </form>
      )}
    </section>
  )
}
