import { useQuery, useQueryClient } from '@tanstack/react-query'
import { NavLink, Outlet } from 'react-router-dom'
import { fetchTenants } from '../api/identity'
import { useAuth } from '../auth/AuthProvider'
import { tenantScope } from '../auth/tenantScope'
import { useState } from 'react'

const nav = [
  { to: '/', label: 'System status', permission: null },
  { to: '/clients', label: 'Clients', permission: 'CLIENT_VIEW' },
  { to: '/standards', label: 'Standards', permission: 'STANDARD_VIEW' },
  { to: '/schemes', label: 'Schemes', permission: 'SCHEME_VIEW' },
  { to: '/auditors', label: 'Auditors', permission: 'AUDITOR_VIEW' },
  { to: '/programmes', label: 'Audits', permission: 'AUDIT_VIEW' },
  { to: '/users', label: 'Users', permission: 'USER_VIEW' },
  { to: '/roles', label: 'Roles', permission: 'ROLE_VIEW' },
  { to: '/tenants', label: 'Tenants', permission: 'TENANT_VIEW' },
  { to: '/sessions', label: 'Sessions', permission: null },
]

const upcoming = ['Findings', 'Certificates', 'Documents', 'Finance']

export function AppShell() {
  const { user, logout, hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const [selectedTenant, setSelectedTenant] = useState(() => tenantScope.get() ?? '')
  const tenantsQuery = useQuery({
    queryKey: ['tenants'],
    queryFn: fetchTenants,
    enabled: Boolean(user?.platformAdmin && hasPermission('TENANT_VIEW')),
  })
  const tenants = tenantsQuery.data?.data?.content ?? []

  function onTenantChange(value: string) {
    setSelectedTenant(value)
    tenantScope.set(value || null)
    void queryClient.invalidateQueries()
  }

  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[16rem_1fr]">
      <aside className="border-b border-slate-200 bg-brand-900 text-white lg:border-b-0 lg:border-r lg:border-brand-700">
        <div className="px-5 py-5">
          <p className="text-xs uppercase tracking-[0.2em] text-brand-100">SaaS</p>
          <h1 className="text-lg font-semibold">Audit Platform</h1>
        </div>
        <nav className="px-3 pb-6" aria-label="Primary">
          {nav
            .filter((item) => item.permission == null || hasPermission(item.permission))
            .map((item) => (
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
            <p className="text-xs text-slate-500">Planning</p>
            <p className="text-sm font-medium">Phase 6 · Programmes, audits, and assignment</p>
          </div>
          <div className="flex items-center gap-3">
            {user?.platformAdmin && (
              <label className="flex items-center gap-2 text-sm text-slate-600">
                Tenant
                <select
                  className="rounded-md border border-slate-300 px-2 py-1 text-sm"
                  value={selectedTenant}
                  onChange={(event) => onTenantChange(event.target.value)}
                  aria-label="Operating tenant"
                >
                  <option value="">Select tenant</option>
                  {tenants.map((tenant) => (
                    <option key={tenant.id} value={tenant.id}>
                      {tenant.name}
                    </option>
                  ))}
                </select>
              </label>
            )}
            <span className="text-sm text-slate-600">{user?.email}</span>
            <button
              type="button"
              onClick={() => void logout()}
              className="rounded-md border border-slate-300 px-3 py-1 text-sm"
            >
              Sign out
            </button>
          </div>
        </header>
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
