import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { resetPassword } from '../api/auth'

export function ResetPasswordPage() {
  const [params] = useSearchParams()
  const [token, setToken] = useState(params.get('token') ?? '')
  const [password, setPassword] = useState('')
  const [done, setDone] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    try {
      await resetPassword(token, password)
      setDone(true)
    } catch {
      setError('The token is invalid or the password does not meet policy (12+ chars, mixed case, digit).')
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-6">
      <main className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-xl font-semibold">Choose a new password</h1>
        {done ? (
          <p className="mt-4 text-sm">
            Password updated.{' '}
            <Link className="text-brand-500 underline" to="/login">
              Sign in
            </Link>
          </p>
        ) : (
          <form className="mt-6 space-y-4" onSubmit={onSubmit}>
            <label className="block text-sm font-medium">
              Token
              <input
                required
                value={token}
                onChange={(e) => setToken(e.target.value)}
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
              />
            </label>
            <label className="block text-sm font-medium">
              New password
              <input
                type="password"
                required
                minLength={12}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
              />
            </label>
            {error && <p className="text-sm text-red-700">{error}</p>}
            <button type="submit" className="w-full rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
              Update password
            </button>
          </form>
        )}
      </main>
    </div>
  )
}
