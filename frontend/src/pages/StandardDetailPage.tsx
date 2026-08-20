import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import { createClause, fetchClauses, fetchStandard, publishStandard } from '../api/standards'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  clauseCode: z.string().min(1).max(64),
  title: z.string().min(1).max(255),
  parentId: z.string().optional(),
  requirementText: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

export function StandardDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const standardQuery = useQuery({ queryKey: ['standard', id], queryFn: () => fetchStandard(id!), enabled: Boolean(id) })
  const clausesQuery = useQuery({
    queryKey: ['clauses', id],
    queryFn: () => fetchClauses(id!),
    enabled: Boolean(id),
  })
  const standard = standardQuery.data?.data
  const clauses = clausesQuery.data?.data ?? []
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { clauseCode: '', title: '', parentId: '', requirementText: '' },
  })
  const publish = useMutation({
    mutationFn: () => publishStandard(id!),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['standard', id] })
      void queryClient.invalidateQueries({ queryKey: ['standards'] })
    },
  })
  const create = useMutation({
    mutationFn: (values: FormValues) =>
      createClause(id!, {
        clauseCode: values.clauseCode,
        title: values.title,
        parentId: emptyToUndef(values.parentId),
        requirementText: emptyToUndef(values.requirementText),
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['clauses', id] })
      form.reset()
    },
  })

  if (standardQuery.isError || !id) {
    return <p className="text-sm text-red-700">Standard not found or you do not have STANDARD_VIEW.</p>
  }
  if (!standard) {
    return <p className="text-sm text-slate-600">Loading standard…</p>
  }

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/standards">
          Standards
        </Link>{' '}
        / {standard.name}
      </p>
      <header className="mt-2 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold">{standard.name}</h2>
          <p className="mt-1 text-sm text-slate-600">
            {standard.code} · {standard.status}
            {standard.publisher ? ` · ${standard.publisher}` : ''}
            {standard.edition ? ` · ${standard.edition}` : ''}
          </p>
        </div>
        {hasPermission('STANDARD_UPDATE') && standard.status === 'DRAFT' && (
          <button
            type="button"
            className="rounded-md border border-slate-300 px-3 py-1 text-sm"
            onClick={() => publish.mutate()}
          >
            Publish
          </button>
        )}
      </header>

      <h3 className="mt-8 text-lg font-medium">Clauses</h3>
      <p className="text-xs text-slate-500">Structure is yours to maintain. Clause text is not supplied by the platform.</p>
      <ul className="mt-2 space-y-2">
        {clauses.map((clause) => (
          <li key={clause.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            <p className="font-medium">
              {clause.clauseCode} · {clause.title}
            </p>
            {clause.requirementText && <p className="mt-1 text-slate-600">{clause.requirementText}</p>}
          </li>
        ))}
        {clauses.length === 0 && <li className="text-sm text-slate-500">No clauses yet.</li>}
      </ul>

      {hasPermission('STANDARD_UPDATE') && standard.status === 'DRAFT' && (
        <form
          className="mt-4 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create clause"
        >
          <p className="text-sm font-medium">Add clause</p>
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Clause code" {...form.register('clauseCode')} />
          <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Title" {...form.register('title')} />
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('parentId')}>
            <option value="">No parent</option>
            {clauses.map((clause) => (
              <option key={clause.id} value={clause.id}>
                {clause.clauseCode} {clause.title}
              </option>
            ))}
          </select>
          <textarea className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Requirement notes" {...form.register('requirementText')} />
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" disabled={create.isPending}>
            Save clause
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
