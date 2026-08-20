import { api } from './client'
import type { ApiResponse, InvoiceSummary, PageResponse, PaymentSummary, QuoteSummary } from './types'

export async function fetchQuotes(): Promise<ApiResponse<PageResponse<QuoteSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  const response = await api.get<ApiResponse<PageResponse<QuoteSummary>>>(`/quotes?${params.toString()}`)
  return response.data
}

export async function fetchQuote(id: string): Promise<ApiResponse<QuoteSummary>> {
  const response = await api.get<ApiResponse<QuoteSummary>>(`/quotes/${id}`)
  return response.data
}

export async function createQuote(body: {
  clientId: string
  currency?: string
  validUntil?: string
  lines: { description: string; quantity: number; unitAmount: number }[]
}): Promise<ApiResponse<QuoteSummary>> {
  const response = await api.post<ApiResponse<QuoteSummary>>('/quotes', body)
  return response.data
}

export async function issueQuote(id: string): Promise<ApiResponse<QuoteSummary>> {
  const response = await api.post<ApiResponse<QuoteSummary>>(`/quotes/${id}/issue`)
  return response.data
}

export async function acceptQuote(id: string): Promise<ApiResponse<QuoteSummary>> {
  const response = await api.post<ApiResponse<QuoteSummary>>(`/quotes/${id}/accept`)
  return response.data
}

export async function declineQuote(id: string): Promise<ApiResponse<QuoteSummary>> {
  const response = await api.post<ApiResponse<QuoteSummary>>(`/quotes/${id}/decline`)
  return response.data
}

export async function invoiceFromQuote(id: string): Promise<ApiResponse<InvoiceSummary>> {
  const response = await api.post<ApiResponse<InvoiceSummary>>(`/quotes/${id}/invoice`)
  return response.data
}

export async function fetchInvoices(): Promise<ApiResponse<PageResponse<InvoiceSummary>>> {
  const params = new URLSearchParams({ size: '50' })
  const response = await api.get<ApiResponse<PageResponse<InvoiceSummary>>>(`/invoices?${params.toString()}`)
  return response.data
}

export async function fetchInvoice(id: string): Promise<ApiResponse<InvoiceSummary>> {
  const response = await api.get<ApiResponse<InvoiceSummary>>(`/invoices/${id}`)
  return response.data
}

export async function createInvoice(body: {
  clientId: string
  currency?: string
  dueOn?: string
  lines: { description: string; quantity: number; unitAmount: number }[]
}): Promise<ApiResponse<InvoiceSummary>> {
  const response = await api.post<ApiResponse<InvoiceSummary>>('/invoices', body)
  return response.data
}

export async function issueInvoice(id: string): Promise<ApiResponse<InvoiceSummary>> {
  const response = await api.post<ApiResponse<InvoiceSummary>>(`/invoices/${id}/issue`)
  return response.data
}

export async function voidInvoice(id: string): Promise<ApiResponse<InvoiceSummary>> {
  const response = await api.post<ApiResponse<InvoiceSummary>>(`/invoices/${id}/void`)
  return response.data
}

export async function recordPayment(
  invoiceId: string,
  body: { amount: number; method?: string; reference?: string },
): Promise<ApiResponse<PaymentSummary>> {
  const response = await api.post<ApiResponse<PaymentSummary>>(`/invoices/${invoiceId}/payments`, body)
  return response.data
}
