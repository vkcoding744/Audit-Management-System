import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import { z } from 'zod'
import { fetchInvoice, issueInvoice, recordPayment, voidInvoice } from '../api/finance'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const paymentSchema = z.object({ amount: z.string().min(1) })

export function InvoiceDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['invoice', id], queryFn: () => fetchInvoice(id!), enabled: Boolean(id) })
  const invoice = query.data?.data
  const form = useForm({ resolver: zodResolver(paymentSchema), defaultValues: { amount: '' } })

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['invoice', id] })
    void queryClient.invalidateQueries({ queryKey: ['invoices'] })
  }

  const issue = useMutation({ mutationFn: () => issueInvoice(id!), onSuccess: invalidate })
  const voidIt = useMutation({ mutationFn: () => voidInvoice(id!), onSuccess: invalidate })
  const pay = useMutation({
    mutationFn: (amount: string) => recordPayment(id!, { amount: Number(amount) }),
    onSuccess: () => {
      invalidate()
      form.reset()
    },
  })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Invoice not found or you do not have INVOICE_VIEW.</p>
  }
  if (!invoice) {
    return <p className="text-sm text-slate-600">Loading invoice…</p>
  }

  const actionError = (issue.error ?? voidIt.error ?? pay.error) as AxiosError<ApiResponse<unknown>> | undefined
  const canPay = invoice.status === 'ISSUED' || invoice.status === 'PARTIALLY_PAID'

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/finance">
          Finance
        </Link>{' '}
        / {invoice.invoiceNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{invoice.invoiceNumber}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {invoice.status}
        {invoice.overdue ? ' · Overdue' : ''} · due {invoice.dueOn ?? '—'} · {invoice.currency} {invoice.amountDue} due
      </p>
      {actionError && (
        <p className="mt-2 text-sm text-red-700">{actionError.response?.data?.error?.message ?? 'The invoice action was rejected.'}</p>
      )}
      {hasPermission('INVOICE_CREATE') && (
        <div className="mt-4 flex flex-wrap gap-2">
          {invoice.status === 'DRAFT' && (
            <button type="button" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white" onClick={() => issue.mutate()}>
              Issue
            </button>
          )}
          {(invoice.status === 'DRAFT' || invoice.status === 'ISSUED') && (
            <button type="button" className="rounded-md border border-red-300 px-3 py-1 text-sm text-red-700" onClick={() => voidIt.mutate()}>
              Void
            </button>
          )}
        </div>
      )}
      {canPay && hasPermission('PAYMENT_RECORD') && (
        <form
          className="mt-6 flex max-w-xl gap-2"
          onSubmit={form.handleSubmit((values) => pay.mutate(values.amount))}
          aria-label="Record payment"
        >
          <input className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Amount" {...form.register('amount')} />
          <button type="submit" className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white">
            Record payment
          </button>
        </form>
      )}
      <h3 className="mt-8 text-lg font-medium">Payments</h3>
      <ul className="mt-2 space-y-2">
        {invoice.payments.map((payment) => (
          <li key={payment.id} className="rounded-lg border border-slate-200 bg-white p-3 text-sm">
            {payment.paymentNumber} · {payment.amount} · {payment.paidOn}
          </li>
        ))}
        {invoice.payments.length === 0 && <li className="text-sm text-slate-500">No payments recorded.</li>}
      </ul>
    </section>
  )
}
