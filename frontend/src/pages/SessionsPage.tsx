import { useQuery } from '@tanstack/react-query'
import { fetchSessions } from '../api/auth'

export function SessionsPage() {
  const query = useQuery({ queryKey: ['sessions'], queryFn: fetchSessions })
  const rows = query.data?.data ?? []

  return (
    <section>
      <h2 className="text-2xl font-semibold">Device sessions</h2>
      <p className="mt-1 text-sm text-slate-600">Refresh tokens are stored hashed. Rotation invalidates the previous token.</p>
      <ul className="mt-4 space-y-2">
        {rows.map((session) => (
          <li key={session.id} className="rounded-lg border border-slate-200 bg-white p-4 text-sm">
            <p>
              {session.current ? 'Current session' : 'Session'} · {session.revoked ? 'revoked' : 'active'}
            </p>
            <p className="text-slate-500">{session.ipAddress ?? 'unknown IP'}</p>
            <p className="truncate text-slate-500">{session.userAgent}</p>
          </li>
        ))}
      </ul>
    </section>
  )
}
