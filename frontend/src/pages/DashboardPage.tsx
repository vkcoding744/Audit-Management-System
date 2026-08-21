import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { fetchTenantDashboard } from '../api/dashboard'

const cards = [
  { key: 'clients', label: 'Clients', to: '/clients' },
  { key: 'upcomingAudits', label: 'Upcoming audits', to: '/programmes' },
  { key: 'completedAudits', label: 'Completed audits', to: '/programmes' },
  { key: 'openFindings', label: 'Open findings', to: '/findings' },
  { key: 'overdueCapa', label: 'Overdue CAPA', to: '/findings' },
  { key: 'activeCertificates', label: 'Active certificates', to: '/certificates' },
  { key: 'certificatesExpiringSoon', label: 'Certificates expiring (90d)', to: '/certificates' },
  { key: 'outstandingInvoices', label: 'Outstanding invoices', to: '/finance' },
  { key: 'openComplaints', label: 'Open complaints', to: '/governance' },
  { key: 'openAppeals', label: 'Open appeals', to: '/governance' },
  { key: 'pendingAiReviews', label: 'AI drafts pending review', to: '/ai' },
] as const

export function DashboardPage() {
  const query = useQuery({ queryKey: ['tenant-dashboard'], queryFn: fetchTenantDashboard })
  const data = query.data?.data

  return (
    <section>
      <h2 className="text-2xl font-semibold">Operations dashboard</h2>
      <p className="mt-1 text-sm text-slate-600">
        Live tenant-scoped counts from persisted rows. This is not a BI cube and does not include copyrighted clause text.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have DASHBOARD_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      {query.isPending && <p className="mt-4 text-sm text-slate-600">Loading dashboard…</p>}
      {data && (
        <div className="mt-6 grid gap-4 md:grid-cols-3">
          {cards.map((card) => (
            <Link
              key={card.key}
              to={card.to}
              className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm hover:border-brand-500"
            >
              <h3 className="text-sm font-medium text-slate-500">{card.label}</h3>
              <p className="mt-2 text-2xl font-semibold text-slate-900">{data[card.key]}</p>
            </Link>
          ))}
        </div>
      )}
    </section>
  )
}
