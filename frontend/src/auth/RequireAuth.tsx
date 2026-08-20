import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'
import type { ReactNode } from 'react'

export function RequireAuth({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth()
  if (loading) {
    return <div className="p-8 text-sm text-slate-600">Loading session…</div>
  }
  if (!user) {
    return <Navigate to="/login" replace />
  }
  return children
}
