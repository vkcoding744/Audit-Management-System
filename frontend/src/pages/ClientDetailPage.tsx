import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import {
  createContact,
  createSite,
  fetchClientDashboard,
  fetchContacts,
  fetchSites,
  setClientStatus,
} from '../api/clients'
import { useAuth } from '../auth/AuthProvider'

const siteSchema = z.object({
  name: z.string().min(1, 'Name is required').max(255),
  city: z.string().max(128).optional(),
  country: z.string().max(128).optional(),
})

const contactSchema = z.object({
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  email: z.union([z.string().email(), z.literal('')]).optional(),
  designation: z.string().max(128).optional(),
  primaryContact: z.boolean(),
})

type SiteForm = z.infer<typeof siteSchema>
type ContactForm = z.infer<typeof contactSchema>

export function ClientDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const dashboardQuery = useQuery({
    queryKey: ['client-dashboard', id],
    queryFn: () => fetchClientDashboard(id!),
    enabled: Boolean(id),
  })
  const sitesQuery = useQuery({
    queryKey: ['client-sites', id],
    queryFn: () => fetchSites(id!),
    enabled: Boolean(id) && hasPermission('SITE_VIEW'),
  })
  const contactsQuery = useQuery({
    queryKey: ['client-contacts', id],
    queryFn: () => fetchContacts(id!),
    enabled: Boolean(id) && hasPermission('CONTACT_VIEW'),
  })

  const dashboard = dashboardQuery.data?.data
  const client = dashboard?.client
  const sites = sitesQuery.data?.data ?? []
  const contacts = contactsQuery.data?.data ?? []

  const siteForm = useForm<SiteForm>({
    resolver: zodResolver(siteSchema),
    defaultValues: { name: '', city: '', country: '' },
  })
  const contactForm = useForm<ContactForm>({
    resolver: zodResolver(contactSchema),
    defaultValues: { firstName: '', lastName: '', email: '', designation: '', primaryContact: false },
  })

  const statusMutation = useMutation({
    mutationFn: (action: 'activate' | 'suspend') => setClientStatus(id!, action),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['client-dashboard', id] })
      void queryClient.invalidateQueries({ queryKey: ['clients'] })
    },
  })
  const siteMutation = useMutation({
    mutationFn: (values: SiteForm) =>
      createSite(id!, {
        name: values.name,
        city: emptyToUndef(values.city),
        country: emptyToUndef(values.country),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['client-sites', id] })
      void queryClient.invalidateQueries({ queryKey: ['client-dashboard', id] })
      siteForm.reset()
    },
  })
  const contactMutation = useMutation({
    mutationFn: (values: ContactForm) =>
      createContact(id!, {
        firstName: values.firstName,
        lastName: values.lastName,
        email: emptyToUndef(values.email),
        designation: emptyToUndef(values.designation),
        primaryContact: values.primaryContact,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['client-contacts', id] })
      void queryClient.invalidateQueries({ queryKey: ['client-dashboard', id] })
      contactForm.reset()
    },
  })

  if (dashboardQuery.isError || !id) {
    return <p className="text-sm text-red-700">Client not found or you do not have CLIENT_VIEW.</p>
  }
  if (!client) {
    return <p className="text-sm text-slate-600">Loading client…</p>
  }

  const metrics: { label: string; value: number }[] = [
    { label: 'Sites', value: dashboard.siteCount },
    { label: 'Contacts', value: dashboard.contactCount },
    { label: 'Upcoming audits', value: dashboard.upcomingAudits },
    { label: 'Completed audits', value: dashboard.completedAudits },
    { label: 'Open findings', value: dashboard.openFindings },
    { label: 'Overdue CAPA', value: dashboard.overdueCapa },
    { label: 'Active certificates', value: dashboard.activeCertificates },
    { label: 'Certificates expiring soon', value: dashboard.certificatesExpiringSoon },
    { label: 'Outstanding payments', value: dashboard.outstandingPayments },
    { label: 'Documents', value: dashboard.documents },
    { label: 'Open complaints', value: dashboard.openComplaints },
    { label: 'Open appeals', value: dashboard.openAppeals },
  ]

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/clients">
          Clients
        </Link>{' '}
        / {client.legalName}
      </p>
      <header className="mt-2 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold">{client.legalName}</h2>
          <p className="mt-1 text-sm text-slate-600">
            {client.clientNumber} · {client.status}
            {client.tradingName ? ` · ${client.tradingName}` : ''}
          </p>
        </div>
        {hasPermission('CLIENT_UPDATE') && (
          <div className="flex gap-2">
            <button
              type="button"
              className="rounded-md border border-slate-300 px-3 py-1 text-sm"
              onClick={() => statusMutation.mutate('activate')}
            >
              Activate
            </button>
            <button
              type="button"
              className="rounded-md border border-slate-300 px-3 py-1 text-sm"
              onClick={() => statusMutation.mutate('suspend')}
            >
              Suspend
            </button>
          </div>
        )}
      </header>

      <dl className="mt-4 grid gap-2 text-sm md:grid-cols-2">
        <div>
          <dt className="text-slate-500">Registration</dt>
          <dd>{client.registrationNumber ?? '—'}</dd>
        </div>
        <div>
          <dt className="text-slate-500">Tax number</dt>
          <dd>{client.taxNumber ?? '—'}</dd>
        </div>
        <div>
          <dt className="text-slate-500">Industry</dt>
          <dd>{client.industry ?? '—'}</dd>
        </div>
        <div>
          <dt className="text-slate-500">Employees</dt>
          <dd>{client.employeeCount ?? '—'}</dd>
        </div>
        <div>
          <dt className="text-slate-500">Contact</dt>
          <dd>
            {client.email ?? '—'} {client.phone ? `· ${client.phone}` : ''}
          </dd>
        </div>
        <div>
          <dt className="text-slate-500">Address</dt>
          <dd>
            {[client.addressLine1, client.city, client.country].filter(Boolean).join(', ') || '—'}
          </dd>
        </div>
      </dl>

      <h3 className="mt-8 text-lg font-medium">Dashboard</h3>
      <p className="text-xs text-slate-500">
        Site and contact counts are live. Audit, certificate, finance, document, complaint, and appeal counts stay at zero
        until those modules persist data.
      </p>
      <div className="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {metrics.map((item) => (
          <article key={item.label} className="rounded-lg border border-slate-200 bg-white p-3">
            <p className="text-xs text-slate-500">{item.label}</p>
            <p className="mt-1 text-xl font-semibold">{item.value}</p>
          </article>
        ))}
      </div>

      {hasPermission('SITE_VIEW') && (
        <div className="mt-8">
          <h3 className="text-lg font-medium">Sites</h3>
          <ul className="mt-2 space-y-2">
            {sites.map((site) => (
              <li key={site.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
                <p className="font-medium">{site.name}</p>
                <p className="text-slate-500">
                  {[site.city, site.country].filter(Boolean).join(', ') || 'No address'} · {site.status}
                </p>
              </li>
            ))}
            {sites.length === 0 && <li className="text-sm text-slate-500">No sites yet.</li>}
          </ul>
          {hasPermission('SITE_CREATE') && (
            <form
              className="mt-4 max-w-lg space-y-2 rounded-lg border border-slate-200 bg-white p-4"
              onSubmit={siteForm.handleSubmit((values) => siteMutation.mutate(values))}
              aria-label="Create site"
            >
              <p className="text-sm font-medium">Add site</p>
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Name" {...siteForm.register('name')} />
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="City" {...siteForm.register('city')} />
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Country" {...siteForm.register('country')} />
              <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" disabled={siteMutation.isPending}>
                Save site
              </button>
            </form>
          )}
        </div>
      )}

      {hasPermission('CONTACT_VIEW') && (
        <div className="mt-8">
          <h3 className="text-lg font-medium">Contacts</h3>
          <ul className="mt-2 space-y-2">
            {contacts.map((contact) => (
              <li key={contact.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
                <p className="font-medium">
                  {contact.firstName} {contact.lastName}
                  {contact.primaryContact ? ' · Primary' : ''}
                </p>
                <p className="text-slate-500">
                  {contact.designation ?? 'No designation'} · {contact.email ?? 'No email'}
                </p>
              </li>
            ))}
            {contacts.length === 0 && <li className="text-sm text-slate-500">No contacts yet.</li>}
          </ul>
          {hasPermission('CONTACT_CREATE') && (
            <form
              className="mt-4 max-w-lg space-y-2 rounded-lg border border-slate-200 bg-white p-4"
              onSubmit={contactForm.handleSubmit((values) => contactMutation.mutate(values))}
              aria-label="Create contact"
            >
              <p className="text-sm font-medium">Add contact</p>
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="First name" {...contactForm.register('firstName')} />
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Last name" {...contactForm.register('lastName')} />
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Email" {...contactForm.register('email')} />
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Designation" {...contactForm.register('designation')} />
              <label className="flex items-center gap-2 text-sm">
                <input type="checkbox" {...contactForm.register('primaryContact')} />
                Primary contact
              </label>
              <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" disabled={contactMutation.isPending}>
                Save contact
              </button>
            </form>
          )}
        </div>
      )}
    </section>
  )
}

function emptyToUndef(value?: string) {
  if (!value || value.trim() === '') {
    return undefined
  }
  return value.trim()
}
