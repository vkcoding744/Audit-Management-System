import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { fetchFindings } from '../api/findings'
import { useAuth } from '../auth/AuthProvider'

export function FindingsPage() {
  const { hasPermission } = useAuth()
  const query = useQuery({ queryKey: ['findings'], queryFn: () => fetchFindings() })
  const rows = query.data?.data?.content ?? []

  return (
    <section>
      <h2 className="text-2xl font-semibold">Findings</h2>
      <p className="mt-1 text-sm text-slate-600">
        Nonconformities and observations raised during fieldwork. Major and minor items need completed CAPA before close.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have AUDIT_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Title</th>
              <th className="px-4 py-2">Severity</th>
              <th className="px-4 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((finding) => (
              <tr key={finding.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">{finding.findingNumber}</td>
                <td className="px-4 py-2">
                  <Link className="text-brand-500 underline" to={`/findings/${finding.id}`}>
                    {finding.title}
                  </Link>
                </td>
                <td className="px-4 py-2">{finding.severity}</td>
                <td className="px-4 py-2">{finding.status}</td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No findings in this tenant. Raise them from an in-progress or completed audit.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {!hasPermission('FINDING_CREATE') && (
        <p className="mt-3 text-sm text-slate-500">FINDING_CREATE is required to raise new findings from an audit.</p>
      )}
    </section>
  )
}
