import { Link } from 'react-router-dom'

export function LoginPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-6">
      <main className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-xl font-semibold text-slate-900">Sign in</h1>
        <p className="mt-2 text-sm text-slate-600">
          Username and password authentication, JWT, and tenant-aware sessions are scheduled for Phase 2. This page is the
          reserved entry point and does not accept credentials yet.
        </p>
        <form className="mt-6 space-y-4" aria-label="Sign in (unavailable)">
          <label className="block text-sm font-medium text-slate-700">
            Email
            <input
              disabled
              type="email"
              className="mt-1 w-full rounded-md border border-slate-300 bg-slate-50 px-3 py-2 text-sm"
              placeholder="Available in Phase 2"
            />
          </label>
          <label className="block text-sm font-medium text-slate-700">
            Password
            <input
              disabled
              type="password"
              className="mt-1 w-full rounded-md border border-slate-300 bg-slate-50 px-3 py-2 text-sm"
            />
          </label>
          <button
            type="button"
            disabled
            className="w-full rounded-md bg-brand-500 px-4 py-2 text-sm font-medium text-white opacity-60"
          >
            Sign in unavailable
          </button>
        </form>
        <p className="mt-4 text-sm">
          <Link className="text-brand-500 underline" to="/">
            View system status
          </Link>
        </p>
      </main>
    </div>
  )
}
