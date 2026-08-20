import { useQuery } from '@tanstack/react-query'
import { fetchUsers } from '../api/identity'

export function UsersPage() {
  const query = useQuery({ queryKey: ['users'], queryFn: fetchUsers })
  const rows = query.data?.data?.content ?? []

  return (
    <section>
      <h2 className="text-2xl font-semibold">Users</h2>
      <p className="mt-1 text-sm text-slate-600">Tenant-scoped directory. Platform admins can filter with X-Tenant-Id.</p>
      {query.isError && <p className="mt-4 text-sm text-red-700">You do not have USER_VIEW or the API is unavailable.</p>}
      <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Name</th>
              <th className="px-4 py-2">Email</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2">Roles</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((user) => (
              <tr key={user.id} className="border-t border-slate-100">
                <td className="px-4 py-2">
                  {user.firstName} {user.lastName}
                </td>
                <td className="px-4 py-2">{user.email}</td>
                <td className="px-4 py-2">{user.status}</td>
                <td className="px-4 py-2">{user.roles.join(', ')}</td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No users in this scope.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
