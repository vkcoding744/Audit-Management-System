import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { createAiGeneration, fetchAiGenerations } from '../api/ai'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  purpose: z.enum(['GENERIC', 'FINDING_SUMMARY', 'AUDIT_NARRATIVE', 'COMPLAINT_RESPONSE']),
  prompt: z.string().min(1),
})

export function AiPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['ai-generations'], queryFn: fetchAiGenerations })
  const rows = query.data?.data?.content ?? []
  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: { purpose: 'GENERIC' as const, prompt: '' },
  })

  const create = useMutation({
    mutationFn: (values: z.infer<typeof schema>) => createAiGeneration(values),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['ai-generations'] })
      form.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      form.setError('root', { message: error.response?.data?.error?.message ?? 'Could not generate draft' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">AI drafts</h2>
      <p className="mt-1 text-sm text-slate-600">
        Provider-agnostic drafts with mandatory human review. Approving a draft does not issue a certificate or close a
        finding. The default adapter is a stub; vendor API keys stay in the environment.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have AI_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}

      <div className="mt-6 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Purpose</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2">Provider</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/ai-generations/${row.id}`}>
                    {row.generationNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">{row.purpose}</td>
                <td className="px-4 py-2">{row.status}</td>
                <td className="px-4 py-2">
                  {row.provider} · {row.model}
                </td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No AI drafts in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {hasPermission('AI_UPDATE') && (
        <form
          className="mt-3 max-w-xl space-y-2 rounded-lg border border-slate-200 bg-white p-4"
          onSubmit={form.handleSubmit((values) => create.mutate(values))}
          aria-label="Create AI draft"
        >
          <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...form.register('purpose')}>
            <option value="GENERIC">Generic</option>
            <option value="FINDING_SUMMARY">Finding summary</option>
            <option value="AUDIT_NARRATIVE">Audit narrative</option>
            <option value="COMPLAINT_RESPONSE">Complaint response</option>
          </select>
          <textarea
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            placeholder="Prompt"
            rows={4}
            {...form.register('prompt')}
          />
          {form.formState.errors.root && <p className="text-sm text-red-700">{form.formState.errors.root.message}</p>}
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-2 text-sm text-white">
            Generate draft
          </button>
        </form>
      )}
    </section>
  )
}
