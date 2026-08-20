import { useQuery } from '@tanstack/react-query'
import { fetchTenants } from '../api/identity'

export function TenantsPage() {
  const query = useQuery({ queryKey: ['tenants'], queryFn: fetchTenants })
  const rows = query.data?.data?.content ?? []

  return (
    <section>
      <h2 className="text-2xl font-semibold">Tenants</h2>
      <p className="mt-1 text-sm text-slate-600">Certification-body organisations. Non-platform users only see their own tenant.</p>
      <ul className="mt-4 space-y-2">
        {rows.map((tenant) => (
          <li key={tenant.id} className="rounded-lg border border-slate-200 bg-white p-4">
            <p className="font-medium">{tenant.name}</p>
            <p className="text-sm text-slate-500">
              {tenant.code} · {tenant.status}
            </p>
          </li>
        ))}
      </ul>
    </section>
  )
}
