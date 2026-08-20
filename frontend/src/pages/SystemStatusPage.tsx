import { useQuery } from '@tanstack/react-query'
import { fetchSystemHealth, fetchSystemInfo } from '../api/system'

export function SystemStatusPage() {
  const healthQuery = useQuery({
    queryKey: ['system-health'],
    queryFn: fetchSystemHealth,
  })
  const infoQuery = useQuery({
    queryKey: ['system-info'],
    queryFn: fetchSystemInfo,
  })

  const health = healthQuery.data?.data
  const info = infoQuery.data?.data
  const loading = healthQuery.isPending || infoQuery.isPending
  const failed = healthQuery.isError || infoQuery.isError

  return (
    <section>
      <nav className="mb-4 text-sm text-slate-500" aria-label="Breadcrumb">
        Home / System status
      </nav>
      <header className="mb-6">
        <h2 className="text-2xl font-semibold text-slate-900">System status</h2>
        <p className="mt-1 max-w-2xl text-sm text-slate-600">
          Live connectivity to the API and database. Authentication and business modules are introduced in later phases.
        </p>
      </header>

      {loading && (
        <div className="grid gap-4 md:grid-cols-3" aria-busy="true">
          {[0, 1, 2].map((key) => (
            <div key={key} className="h-28 animate-pulse rounded-lg bg-white shadow-sm" />
          ))}
        </div>
      )}

      {failed && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800" role="alert">
          The API could not be reached. Start the backend on port 8081 or use Docker Compose.
        </div>
      )}

      {!loading && !failed && (
        <div className="grid gap-4 md:grid-cols-3">
          <StatusCard title="Application" value={health?.status ?? 'UNKNOWN'} />
          <StatusCard title="Database" value={health?.database ?? 'UNKNOWN'} />
          <StatusCard title="Tenants" value={String(health?.tenantCount ?? 0)} />
          <StatusCard title="API version" value={info?.apiVersion ?? '—'} />
          <StatusCard title="Environment" value={info?.environment ?? '—'} />
          <StatusCard title="Service" value={info?.application ?? '—'} />
        </div>
      )}
    </section>
  )
}

function StatusCard({ title, value }: { title: string; value: string }) {
  const healthy = value === 'UP'
  return (
    <article className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <h3 className="text-sm font-medium text-slate-500">{title}</h3>
      <p className={`mt-2 text-xl font-semibold ${healthy ? 'text-emerald-700' : 'text-slate-900'}`}>{value}</p>
    </article>
  )
}
