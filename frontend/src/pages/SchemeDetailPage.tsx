import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import {
  activateScheme,
  createChecklist,
  fetchChecklists,
  fetchScheme,
  fetchStandards,
  linkStandardToScheme,
} from '../api/standards'
import { useAuth } from '../auth/AuthProvider'

const linkSchema = z.object({ standardId: z.string().min(1) })
const checklistSchema = z.object({
  name: z.string().min(1).max(255),
  versionLabel: z.string().min(1).max(32),
  standardId: z.string().optional(),
})

type LinkForm = z.infer<typeof linkSchema>
type ChecklistForm = z.infer<typeof checklistSchema>

export function SchemeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const schemeQuery = useQuery({ queryKey: ['scheme', id], queryFn: () => fetchScheme(id!), enabled: Boolean(id) })
  const checklistsQuery = useQuery({
    queryKey: ['checklists', id],
    queryFn: () => fetchChecklists(id!),
    enabled: Boolean(id) && hasPermission('CHECKLIST_VIEW'),
  })
  const standardsQuery = useQuery({
    queryKey: ['standards'],
    queryFn: () => fetchStandards(),
    enabled: hasPermission('STANDARD_VIEW'),
  })
  const scheme = schemeQuery.data?.data
  const checklists = checklistsQuery.data?.data ?? []
  const standards = standardsQuery.data?.data?.content ?? []
  const linkForm = useForm<LinkForm>({ resolver: zodResolver(linkSchema), defaultValues: { standardId: '' } })
  const checklistForm = useForm<ChecklistForm>({
    resolver: zodResolver(checklistSchema),
    defaultValues: { name: '', versionLabel: '1.0', standardId: '' },
  })
  const activate = useMutation({
    mutationFn: () => activateScheme(id!),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['scheme', id] }),
  })
  const link = useMutation({
    mutationFn: (values: LinkForm) => linkStandardToScheme(id!, values.standardId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['scheme', id] })
      linkForm.reset()
    },
  })
  const create = useMutation({
    mutationFn: (values: ChecklistForm) =>
      createChecklist(id!, {
        name: values.name,
        versionLabel: values.versionLabel,
        standardId: emptyToUndef(values.standardId),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['checklists', id] })
      checklistForm.reset({ name: '', versionLabel: '1.0', standardId: '' })
    },
  })

  if (schemeQuery.isError || !id) {
    return <p className="text-sm text-red-700">Scheme not found or you do not have SCHEME_VIEW.</p>
  }
  if (!scheme) {
    return <p className="text-sm text-slate-600">Loading scheme…</p>
  }

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/schemes">
          Schemes
        </Link>{' '}
        / {scheme.name}
      </p>
      <header className="mt-2 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold">{scheme.name}</h2>
          <p className="mt-1 text-sm text-slate-600">
            {scheme.code} · {scheme.status}
            {scheme.accreditationBody ? ` · ${scheme.accreditationBody}` : ''}
          </p>
        </div>
        {hasPermission('SCHEME_UPDATE') && (scheme.status === 'DRAFT' || scheme.status === 'SUSPENDED') && (
          <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => activate.mutate()}>
            Activate
          </button>
        )}
      </header>

      <h3 className="mt-8 text-lg font-medium">Linked standards</h3>
      <ul className="mt-2 space-y-2">
        {scheme.standards.map((standard) => (
          <li key={standard.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            <Link className="underline" to={`/standards/${standard.id}`}>
              {standard.code} · {standard.name}
            </Link>
          </li>
        ))}
        {scheme.standards.length === 0 && <li className="text-sm text-slate-500">No standards linked.</li>}
      </ul>
      {hasPermission('SCHEME_UPDATE') && (
        <form
          className="mt-4 flex max-w-xl gap-2"
          onSubmit={linkForm.handleSubmit((values) => link.mutate(values))}
          aria-label="Link standard"
        >
          <select className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm" {...linkForm.register('standardId')}>
            <option value="">Select standard</option>
            {standards.map((standard) => (
              <option key={standard.id} value={standard.id}>
                {standard.code} · {standard.name}
              </option>
            ))}
          </select>
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
            Link
          </button>
        </form>
      )}

      {hasPermission('CHECKLIST_VIEW') && (
        <div className="mt-8">
          <h3 className="text-lg font-medium">Checklists</h3>
          <ul className="mt-2 space-y-2">
            {checklists.map((checklist) => (
              <li key={checklist.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
                <Link className="font-medium underline" to={`/checklists/${checklist.id}`}>
                  {checklist.name} {checklist.versionLabel}
                </Link>
                <p className="text-slate-500">{checklist.status}</p>
              </li>
            ))}
            {checklists.length === 0 && <li className="text-sm text-slate-500">No checklists yet.</li>}
          </ul>
          {hasPermission('CHECKLIST_CREATE') && (
            <form
              className="mt-4 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4"
              onSubmit={checklistForm.handleSubmit((values) => create.mutate(values))}
              aria-label="Create checklist"
            >
              <p className="text-sm font-medium">New checklist version</p>
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Name" {...checklistForm.register('name')} />
              <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Version" {...checklistForm.register('versionLabel')} />
              <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...checklistForm.register('standardId')}>
                <option value="">Optional linked standard</option>
                {scheme.standards.map((standard) => (
                  <option key={standard.id} value={standard.id}>
                    {standard.code}
                  </option>
                ))}
              </select>
              <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
                Create checklist
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
