import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useForm, type UseFormRegisterReturn } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { fetchClients } from '../api/clients'
import { createInvoice, createQuote, fetchInvoices, fetchQuotes } from '../api/finance'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

const schema = z.object({
  clientId: z.string().min(1),
  description: z.string().min(1),
  amount: z.string().min(1),
})

export function FinancePage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const quotesQuery = useQuery({ queryKey: ['quotes'], queryFn: () => fetchQuotes() })
  const invoicesQuery = useQuery({ queryKey: ['invoices'], queryFn: () => fetchInvoices() })
  const clientsQuery = useQuery({
    queryKey: ['clients'],
    queryFn: () => fetchClients(),
    enabled: hasPermission('INVOICE_CREATE') && hasPermission('CLIENT_VIEW'),
  })
  const quotes = quotesQuery.data?.data?.content ?? []
  const invoices = invoicesQuery.data?.data?.content ?? []
  const clients = clientsQuery.data?.data?.content ?? []
  const quoteForm = useForm({ resolver: zodResolver(schema), defaultValues: { clientId: '', description: '', amount: '' } })
  const invoiceForm = useForm({ resolver: zodResolver(schema), defaultValues: { clientId: '', description: '', amount: '' } })

  const createQ = useMutation({
    mutationFn: (values: z.infer<typeof schema>) =>
      createQuote({
        clientId: values.clientId,
        lines: [{ description: values.description, quantity: 1, unitAmount: Number(values.amount) }],
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['quotes'] })
      quoteForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      quoteForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create quote' })
    },
  })
  const createI = useMutation({
    mutationFn: (values: z.infer<typeof schema>) =>
      createInvoice({
        clientId: values.clientId,
        lines: [{ description: values.description, quantity: 1, unitAmount: Number(values.amount) }],
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['invoices'] })
      invoiceForm.reset()
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      invoiceForm.setError('root', { message: error.response?.data?.error?.message ?? 'Could not create invoice' })
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Finance</h2>
      <p className="mt-1 text-sm text-slate-600">
        Quotes and invoices in the tenant currency of the record. Payments cannot exceed the amount due. Overdue means
        issued or partly paid and past the due date.
      </p>
      {(quotesQuery.isError || invoicesQuery.isError) && (
        <p className="mt-4 text-sm text-red-700">You do not have INVOICE_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}

      <h3 className="mt-6 text-lg font-medium">Quotes</h3>
      <div className="mt-2 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2">Total</th>
            </tr>
          </thead>
          <tbody>
            {quotes.map((quote) => (
              <tr key={quote.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/quotes/${quote.id}`}>
                    {quote.quoteNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">
                  {quote.status}
                  {quote.expired ? ' · Expired' : ''}
                </td>
                <td className="px-4 py-2">
                  {quote.currency} {quote.totalAmount}
                </td>
              </tr>
            ))}
            {quotes.length === 0 && !quotesQuery.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={3}>
                  No quotes in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <h3 className="mt-8 text-lg font-medium">Invoices</h3>
      <div className="mt-2 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2">Due</th>
              <th className="px-4 py-2">Amount due</th>
            </tr>
          </thead>
          <tbody>
            {invoices.map((invoice) => (
              <tr key={invoice.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/invoices/${invoice.id}`}>
                    {invoice.invoiceNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">
                  {invoice.status}
                  {invoice.overdue ? ' · Overdue' : ''}
                </td>
                <td className="px-4 py-2">{invoice.dueOn ?? '—'}</td>
                <td className="px-4 py-2">
                  {invoice.currency} {invoice.amountDue}
                </td>
              </tr>
            ))}
            {invoices.length === 0 && !invoicesQuery.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No invoices in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {hasPermission('INVOICE_CREATE') && (
        <div className="mt-8 grid gap-6 lg:grid-cols-2">
          <form
            className="space-y-3 rounded-lg border border-slate-200 bg-white p-4"
            onSubmit={quoteForm.handleSubmit((values) => createQ.mutate(values))}
            aria-label="Create quote"
          >
            <h3 className="text-lg font-medium">New quote</h3>
            <ClientSelect clients={clients} field={quoteForm.register('clientId')} />
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Line description" {...quoteForm.register('description')} />
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Amount" {...quoteForm.register('amount')} />
            {quoteForm.formState.errors.root && (
              <p className="text-sm text-red-700" role="alert">
                {quoteForm.formState.errors.root.message}
              </p>
            )}
            <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
              Create quote
            </button>
          </form>
          <form
            className="space-y-3 rounded-lg border border-slate-200 bg-white p-4"
            onSubmit={invoiceForm.handleSubmit((values) => createI.mutate(values))}
            aria-label="Create invoice"
          >
            <h3 className="text-lg font-medium">New invoice</h3>
            <ClientSelect clients={clients} field={invoiceForm.register('clientId')} />
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Line description" {...invoiceForm.register('description')} />
            <input className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" placeholder="Amount" {...invoiceForm.register('amount')} />
            {invoiceForm.formState.errors.root && (
              <p className="text-sm text-red-700" role="alert">
                {invoiceForm.formState.errors.root.message}
              </p>
            )}
            <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
              Create invoice
            </button>
          </form>
        </div>
      )}
    </section>
  )
}

function ClientSelect({
  clients,
  field,
}: {
  clients: { id: string; legalName: string; clientNumber: string }[]
  field: UseFormRegisterReturn
}) {
  return (
    <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" {...field}>
      <option value="">Select client</option>
      {clients.map((client) => (
        <option key={client.id} value={client.id}>
          {client.clientNumber} · {client.legalName}
        </option>
      ))}
    </select>
  )
}
