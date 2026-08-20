import { NavLink, Outlet } from 'react-router-dom'

const nav = [
  { to: '/', label: 'System status', ready: true },
  { to: '/login', label: 'Sign in', ready: true },
]

const upcoming = [
  'Clients',
  'Audits',
  'Auditors',
  'Findings',
  'Certificates',
  'Documents',
  'Finance',
  'Reports',
]

export function AppShell() {
  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[16rem_1fr]">
      <aside className="border-b border-slate-200 bg-brand-900 text-white lg:border-b-0 lg:border-r lg:border-brand-700">
        <div className="px-5 py-5">
          <p className="text-xs uppercase tracking-[0.2em] text-brand-100">SaaS</p>
          <h1 className="text-lg font-semibold">Audit Platform</h1>
        </div>
        <nav className="px-3 pb-6" aria-label="Primary">
          {nav.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `mb-1 block rounded-md px-3 py-2 text-sm ${
                  isActive ? 'bg-brand-700' : 'text-brand-50 hover:bg-brand-700/60'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
          <p className="mt-6 px-3 text-xs uppercase tracking-wide text-brand-100">Later phases</p>
          <ul className="mt-2 space-y-1 px-3 text-sm text-brand-100/70">
            {upcoming.map((label) => (
              <li key={label}>{label}</li>
            ))}
          </ul>
        </nav>
      </aside>
      <div className="flex min-h-screen flex-col">
        <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-4">
          <div>
            <p className="text-xs text-slate-500">Foundation</p>
            <p className="text-sm font-medium">Phase 1 · Platform operations</p>
          </div>
          <span className="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-600">Unauthenticated</span>
        </header>
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
