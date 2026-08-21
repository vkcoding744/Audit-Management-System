import { useQuery } from '@tanstack/react-query'
import { fetchAuditLogs } from '../api/auditLogs'

export function AuditLogsPage() {
  const query = useQuery({ queryKey: ['audit-logs'], queryFn: fetchAuditLogs })
  const rows = query.data?.data?.content ?? []

  return (
    <section>
      <h2 className="text-2xl font-semibold">Audit logs</h2>
      <p className="mt-1 text-sm text-slate-600">
        Tenant-scoped change history. Platform admins must select a tenant. This is an append-only view; entries are not
        editable.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have AUDIT_LOG_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      <div className="mt-6 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">When</th>
              <th className="px-4 py-2">Action</th>
              <th className="px-4 py-2">Entity</th>
              <th className="px-4 py-2">User</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">{row.createdAt}</td>
                <td className="px-4 py-2">{row.action}</td>
                <td className="px-4 py-2">
                  {row.entityType}
                  {row.entityId ? ` · ${row.entityId}` : ''}
                </td>
                <td className="px-4 py-2 font-mono text-xs">{row.userId ?? '—'}</td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No audit log entries in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
