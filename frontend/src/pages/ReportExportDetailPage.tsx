import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useParams } from 'react-router-dom'
import { cancelReportExport, downloadReportExport, fetchReportExport } from '../api/reports'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function ReportExportDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({
    queryKey: ['report-export', id],
    queryFn: () => fetchReportExport(id!),
    enabled: Boolean(id),
  })
  const item = query.data?.data
  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['report-export', id] })
    void queryClient.invalidateQueries({ queryKey: ['report-exports'] })
  }
  const cancel = useMutation({ mutationFn: () => cancelReportExport(id!), onSuccess: invalidate })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Export not found or you do not have REPORT_VIEW.</p>
  }
  if (!item) {
    return <p className="text-sm text-slate-600">Loading export…</p>
  }
  const actionError = cancel.error as AxiosError<ApiResponse<unknown>> | undefined
  const filename = `${item.exportNumber}.${item.format.toLowerCase()}`

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/reports">
          Reports
        </Link>{' '}
        / {item.exportNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{item.exportNumber}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {item.status} · {item.format}
        {item.rowCount != null ? ` · ${item.rowCount} rows` : ''}
        {item.byteSize != null ? ` · ${item.byteSize} bytes` : ''}
      </p>
      {item.errorMessage && <p className="mt-2 text-sm text-red-700">{item.errorMessage}</p>}
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The export action was rejected.'}</p>
      )}
      <div className="mt-4 flex flex-wrap gap-2">
        {item.status === 'COMPLETED' && hasPermission('REPORT_EXPORT') && (
          <button
            type="button"
            className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white"
            onClick={() => void downloadReportExport(id, filename)}
          >
            Download
          </button>
        )}
        {item.status === 'QUEUED' && hasPermission('REPORT_EXPORT') && (
          <button type="button" className="rounded-md border border-red-300 px-3 py-1 text-sm text-red-700" onClick={() => cancel.mutate()}>
            Cancel
          </button>
        )}
      </div>
    </section>
  )
}
