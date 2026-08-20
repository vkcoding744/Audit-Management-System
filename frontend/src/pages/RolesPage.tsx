import { useQuery } from '@tanstack/react-query'
import { fetchRoles } from '../api/identity'

export function RolesPage() {
  const query = useQuery({ queryKey: ['roles'], queryFn: fetchRoles })
  const rows = query.data?.data ?? []

  return (
    <section>
      <h2 className="text-2xl font-semibold">Roles</h2>
      <p className="mt-1 text-sm text-slate-600">System roles are bundles of permissions. APIs authorize on permission codes.</p>
      <div className="mt-4 grid gap-3 md:grid-cols-2">
        {rows.map((role) => (
          <article key={role.id} className="rounded-lg border border-slate-200 bg-white p-4">
            <h3 className="font-medium">{role.name}</h3>
            <p className="text-xs text-slate-500">{role.code}</p>
            <p className="mt-2 text-sm text-slate-600">{role.description}</p>
            <p className="mt-2 text-xs text-slate-500">{role.permissions.length} permissions</p>
          </article>
        ))}
      </div>
    </section>
  )
}
