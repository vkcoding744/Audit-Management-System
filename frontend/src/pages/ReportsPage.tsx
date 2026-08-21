import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { createReport, fetchReportExports, fetchReports } from '../api/reports'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const reportSchema = z.object({
  name: z.string().min(1),
  dataset: z.enum(['CLIENTS', 'AUDITS', 'FINDINGS', 'CERTIFICATES', 'INVOICES', 'COMPLAINTS']),
  format: z.enum(['CSV', 'JSON']),
  statusFilter: z.string().optional(),
  description: z.string().optional(),
})

export function ReportsPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const reportsQuery = useQuery({ queryKey: ['reports'], queryFn: fetchReports })
  const exportsQuery = useQuery({ queryKey: ['report-exports'], queryFn: fetchReportExports })
  const reports = reportsQuery.data?.data?.content ?? []
  const exports = exportsQuery.data?.data?.content ?? []
  const form = useForm({
    resolver: zodResolver(reportSchema),
    defaultValues: { name: '', dataset: 'CLIENTS' as const, format: 'CSV' as const, statusFilter: '', description: '' },
  })

  const create = useMutation({
    mutationFn: (values: z.infer<typeof reportSchema>) =>
      createReport({
        name: values.name,
        dataset: values.dataset,
        format: values.format,
        statusFilter: values.statusFilter || undefined,
        description: values.description || undefined,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['reports'] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create report' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Reports</h2>
      <p className="mt-1 text-sm text-slate-600">
        Tenant-scoped definitions and CSV/JSON exports. Run generates the file immediately (no background worker).
        Download requires REPORT_EXPORT. Datasets are operational snapshots, not a BI cube.
      </p>
      {(reportsQuery.isError || exportsQuery.isError) && (
        <p className="mt-4 text-sm text-red-700">
          You do not have REPORT_VIEW, tenant scope is missing, or the API is unavailable.
        </p>
      )}

      <h3 className="mt-6 text-lg font-medium">Definitions</h3>
      <div className="mt-2 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Name</th>
              <th className="px-4 py-2">Dataset</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {reports.map((report) => (
              <tr key={report.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/reports/${report.id}`}>
                    {report.reportNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">{report.name}</td>
                <td className="px-4 py-2">
                  {report.dataset} · {report.format}
                </td>
                <td className="px-4 py-2">{report.status}</td>
              </tr>
            ))}
            {reports.length === 0 && !reportsQuery.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No report definitions in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {hasPermission('REPORT_EXPORT') && (
        <form
          className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create report"
        >
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Name" {...form.register('name')} />
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('dataset')}>
            <option value="CLIENTS">Clients</option>
            <option value="AUDITS">Audits</option>
            <option value="FINDINGS">Findings</option>
            <option value="CERTIFICATES">Certificates</option>
            <option value="INVOICES">Invoices</option>
            <option value="COMPLAINTS">Complaints</option>
          </select>
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('format')}>
            <option value="CSV">CSV</option>
            <option value="JSON">JSON</option>
          </select>
          <input
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            placeholder="Status filter (optional)"
            {...form.register('statusFilter')}
          />
          <textarea
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            placeholder="Description"
            rows={2}
            {...form.register('description')}
          />
          {form.formState.errors.root && <p className="text-sm text-red-700">{form.formState.errors.root.message}</p>}
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-2 text-sm text-white">
            Create definition
          </button>
        </form>
      )}

      <h3 className="mt-8 text-lg font-medium">Exports</h3>
      <div className="mt-2 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2">Rows</th>
            </tr>
          </thead>
          <tbody>
            {exports.map((item) => (
              <tr key={item.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/report-exports/${item.id}`}>
                    {item.exportNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">{item.status}</td>
                <td className="px-4 py-2">{item.rowCount ?? '—'}</td>
              </tr>
            ))}
            {exports.length === 0 && !exportsQuery.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={3}>
                  No exports yet. Run a definition to generate a file.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
