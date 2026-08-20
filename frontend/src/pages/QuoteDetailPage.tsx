import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { acceptQuote, declineQuote, fetchQuote, invoiceFromQuote, issueQuote } from '../api/finance'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function QuoteDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['quote', id], queryFn: () => fetchQuote(id!), enabled: Boolean(id) })
  const quote = query.data?.data

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['quote', id] })
    void queryClient.invalidateQueries({ queryKey: ['quotes'] })
  }

  const issue = useMutation({ mutationFn: () => issueQuote(id!), onSuccess: invalidate })
  const accept = useMutation({ mutationFn: () => acceptQuote(id!), onSuccess: invalidate })
  const decline = useMutation({ mutationFn: () => declineQuote(id!), onSuccess: invalidate })
  const toInvoice = useMutation({
    mutationFn: () => invoiceFromQuote(id!),
    onSuccess: (response) => {
      void queryClient.invalidateQueries({ queryKey: ['invoices'] })
      const invoiceId = response.data?.id
      if (invoiceId) {
        navigate(`/invoices/${invoiceId}`)
      }
    },
  })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Quote not found or you do not have INVOICE_VIEW.</p>
  }
  if (!quote) {
    return <p className="text-sm text-slate-600">Loading quote…</p>
  }

  const actionError = (issue.error ?? accept.error ?? decline.error ?? toInvoice.error) as
    | AxiosError<ApiResponse<unknown>>
    | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/finance">
          Finance
        </Link>{' '}
        / {quote.quoteNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{quote.quoteNumber}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {quote.status}
        {quote.expired ? ' · Expired' : ''} · {quote.currency} {quote.totalAmount}
      </p>
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The quote action was rejected.'}</p>
      )}
      {hasPermission('INVOICE_CREATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          {quote.status === 'DRAFT' && (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => issue.mutate()}>
              Issue
            </button>
          )}
          {quote.status === 'ISSUED' && (
            <>
              <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => accept.mutate()}>
                Accept
              </button>
              <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm" onClick={() => decline.mutate()}>
                Decline
              </button>
            </>
          )}
          {quote.status === 'ACCEPTED' && (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => toInvoice.mutate()}>
              Create invoice
            </button>
          )}
        </div>
      )}
      <ul className="mt-6 space-y-2">
        {quote.lines.map((line) => (
          <li key={line.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            {line.description} · {line.quantity} × {line.unitAmount} = {line.lineAmount}
          </li>
        ))}
      </ul>
    </section>
  )
}
