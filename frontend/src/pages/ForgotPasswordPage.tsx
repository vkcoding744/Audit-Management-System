import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { forgotPassword } from '../api/auth'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [devToken, setDevToken] = useState<string | null>(null)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    const response = await forgotPassword(email)
    setMessage(response.data?.message ?? 'If the account exists, a reset message has been queued')
    setDevToken(response.data?.resetToken ?? null)
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-6">
      <main className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-xl font-semibold">Reset password</h1>
        <form className="mt-6 space-y-4" onSubmit={onSubmit}>
          <label className="block text-sm font-medium">
            Email
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            />
          </label>
          <button type="submit" className="w-full rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Send reset instructions
          </button>
        </form>
        {message && <p className="mt-4 text-sm text-slate-700">{message}</p>}
        {devToken && (
          <p className="mt-2 break-all text-xs text-slate-500">
            Dev token: {devToken}. Use the reset page with this token.
          </p>
        )}
        <Link className="mt-4 inline-block text-sm text-brand-500 underline" to="/login">
          Back to sign in
        </Link>
      </main>
    </div>
  )
}
