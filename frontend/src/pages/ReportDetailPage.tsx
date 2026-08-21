import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { archiveReport, fetchReport, publishReport, runReport } from '../api/reports'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function ReportDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const query = useQuery({
    queryKey: ['report', id],
    queryFn: () => fetchReport(id!),
    enabled: Boolean(id),
  })
  const report = query.data?.data
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['report', id] })
    void queryClient.invalidateQueries({ queryKey: ['reports'] })
  }
  const publish = useMutation({ mutationFn: () => publishReport(id!), onSuccess: invalidate })
  const archive = useMutation({ mutationFn: () => archiveReport(id!), onSuccess: invalidate })
  const run = useMutation({
    mutationFn: () => runReport(id!),
    onSuccess: (response) => {
      void queryClient.invalidateQueries({ queryKey: ['report-exports'] })
      const exportId = response.data?.id
      if (exportId) {
        navigate(`/report-exports/${exportId}`)
      }
    },
  })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Report not found or you do not have REPORT_VIEW.</p>
  }
  if (!report) {
    return <p className="text-sm text-slate-600">Loading report…</p>
  }
  const actionError = (publish.error ?? archive.error ?? run.error) as AxiosError<ApiResponse<unknown>> | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/reports">
          Reports
        </Link>{' '}
        / {report.reportNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{report.name}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {report.status} · {report.dataset} · {report.format}
        {report.statusFilter ? ` · filter ${report.statusFilter}` : ''}
      </p>
      {report.description && <p className="mt-4 text-sm">{report.description}</p>}
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The report action was rejected.'}</p>
      )}
      {hasPermission('REPORT_EXPORT') && (
        <div className="mt-4 flex flex-wrap gap-2">
          {report.status === 'DRAFT' && (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => publish.mutate()}>
              Publish
            </button>
          )}
          {report.status !== 'ARCHIVED' && (
            <>
              <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => run.mutate()}>
                Run export
              </button>
              <button type="button" className="rounded-md border border-red-300 px-3 py-1 text-sm text-red-700" onClick={() => archive.mutate()}>
                Archive
              </button>
            </>
          )}
        </div>
      )}
    </section>
  )
}
