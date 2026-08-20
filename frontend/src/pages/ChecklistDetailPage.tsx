import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import { activateChecklist, addChecklistItem, fetchChecklist, fetchClauses } from '../api/standards'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  title: z.string().min(1).max(500),
  clauseId: z.string().optional(),
  guidance: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

export function ChecklistDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const checklistQuery = useQuery({
    queryKey: ['checklist', id],
    queryFn: () => fetchChecklist(id!),
    enabled: Boolean(id),
  })
  const checklist = checklistQuery.data?.data
  const clausesQuery = useQuery({
    queryKey: ['clauses', checklist?.standardId],
    queryFn: () => fetchClauses(checklist!.standardId!),
    enabled: Boolean(checklist?.standardId),
  })
  const clauses = clausesQuery.data?.data ?? []
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { title: '', clauseId: '', guidance: '' },
  })
  const activate = useMutation({
    mutationFn: () => activateChecklist(id!),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['checklist', id] }),
  })
  const addItem = useMutation({
    mutationFn: (values: FormValues) =>
      addChecklistItem(id!, {
        title: values.title,
        clauseId: emptyToUndef(values.clauseId),
        guidance: emptyToUndef(values.guidance),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['checklist', id] })
      form.reset()
    },
  })

  if (checklistQuery.isError || !id) {
    return <p className="text-sm text-red-700">Checklist not found or you do not have CHECKLIST_VIEW.</p>
  }
  if (!checklist) {
    return <p className="text-sm text-slate-600">Loading checklist…</p>
  }

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to={`/schemes/${checklist.schemeId}`}>
          Scheme
        </Link>{' '}
        / {checklist.name}
      </p>
      <header className="mt-2 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold">{checklist.name}</h2>
          <p className="mt-1 text-sm text-slate-600">
            Version {checklist.versionLabel} · {checklist.status}
          </p>
        </div>
        {hasPermission('CHECKLIST_UPDATE') && checklist.status === 'DRAFT' && (
          <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => activate.mutate()}>
            Activate
          </button>
        )}
      </header>

      <ul className="mt-6 space-y-2">
        {checklist.items.map((item) => (
          <li key={item.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            <p className="font-medium">{item.title}</p>
            <p className="text-slate-500">
              {item.itemType}
              {item.required ? ' · Required' : ''}
            </p>
            {item.guidance && <p className="mt-1 text-slate-600">{item.guidance}</p>}
          </li>
        ))}
        {checklist.items.length === 0 && <li className="text-sm text-slate-500">No items yet.</li>}
      </ul>

      {hasPermission('CHECKLIST_UPDATE') && checklist.status === 'DRAFT' && (
        <form
          className="mt-4 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => addItem.mutate(values))}
          aria-label="Create checklist item"
        >
          <p className="text-sm font-medium">Add item</p>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Question or evidence prompt" {...form.register('title')} />
          {clauses.length > 0 && (
            <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('clauseId')}>
              <option value="">Optional clause</option>
              {clauses.map((clause) => (
                <option key={clause.id} value={clause.id}>
                  {clause.clauseCode} {clause.title}
                </option>
              ))}
            </select>
          )}
          <textarea className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Guidance" {...form.register('guidance')} />
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
            Save item
          </button>
        </form>
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
