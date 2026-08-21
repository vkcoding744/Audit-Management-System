import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { searchRecords } from '../api/search'

export function SearchPage() {
  const [q, setQ] = useState('')
  const [type, setType] = useState('')
  const enabled = q.trim().length >= 2
  const query = useQuery({
    queryKey: ['search', q, type],
    queryFn: () => searchRecords(q.trim(), type || undefined),
    enabled,
  })
  const hits = query.data?.data?.hits ?? []

  return (
    <section>
      <h2 className="text-2xl font-semibold">Search</h2>
      <p className="mt-1 text-sm text-slate-600">
        Tenant-scoped lookup of clients, leads, audits, findings, certificates, documents, and complaints. Default
        provider is MySQL; this is not an Elasticsearch cluster or a BI cube, and it does not include copyrighted clause
        text.
      </p>
      <form className="mt-4 flex flex-wrap gap-2" onSubmit={(event) => event.preventDefault()}>
        <input
          type="search"
          className="min-w-[16rem] flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm"
          placeholder="Search (min 2 characters)"
          value={q}
          onChange={(event) => setQ(event.target.value)}
          aria-label="Search query"
        />
        <select
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
          value={type}
          onChange={(event) => setType(event.target.value)}
          aria-label="Record type"
        >
          <option value="">All types</option>
          <option value="CLIENT">Clients</option>
          <option value="LEAD">Leads</option>
          <option value="AUDIT">Audits</option>
          <option value="FINDING">Findings</option>
          <option value="CERTIFICATE">Certificates</option>
          <option value="DOCUMENT">Documents</option>
          <option value="COMPLAINT">Complaints</option>
        </select>
      </form>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have SEARCH_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      {enabled && query.isPending && <p className="mt-4 text-sm text-slate-600">Searching…</p>}
      {enabled && hits.length === 0 && query.isSuccess && (
        <p className="mt-4 text-sm text-slate-600">No matching records in this tenant.</p>
      )}
      <ul className="mt-4 space-y-2">
        {hits.map((hit) => (
          <li key={`${hit.type}-${hit.id}`}>
            <Link to={hit.path} className="block rounded-lg border border-slate-200 bg-white p-4 hover:border-brand-500">
              <p className="text-xs uppercase tracking-wide text-slate-500">{hit.type}</p>
              <p className="font-medium text-slate-900">{hit.title}</p>
              {hit.subtitle && <p className="text-sm text-slate-600">{hit.subtitle}</p>}
            </Link>
          </li>
        ))}
      </ul>
    </section>
  )
}
