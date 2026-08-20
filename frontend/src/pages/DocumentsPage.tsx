import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { FormEvent, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchDocuments, uploadDocument } from '../api/documents'
import type { ApiResponse, DocumentCategory, DocumentLinkType } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

export function DocumentsPage() {
  const { hasPermission } = useAuth()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['documents'], queryFn: () => fetchDocuments() })
  const rows = query.data?.data?.content ?? []
  const [title, setTitle] = useState('')
  const [category, setCategory] = useState<DocumentCategory>('EVIDENCE')
  const [linkedType, setLinkedType] = useState<DocumentLinkType>('GENERAL')
  const [linkedId, setLinkedId] = useState('')
  const [file, setFile] = useState<File | null>(null)
  const [error, setError] = useState<string | null>(null)

  const upload = useMutation({
    mutationFn: () => {
      if (!file) {
        return Promise.reject(new Error('Choose a file'))
      }
      const form = new FormData()
      form.append('file', file)
      if (title) {
        form.append('title', title)
      }
      form.append('category', category)
      form.append('linkedType', linkedType)
      if (linkedType !== 'GENERAL' && linkedId) {
        form.append('linkedId', linkedId)
      }
      return uploadDocument(form)
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['documents'] })
      setTitle('')
      setLinkedId('')
      setFile(null)
      setError(null)
    },
    onError: (err: AxiosError<ApiResponse<unknown>> | Error) => {
      if (err instanceof AxiosError) {
        setError(err.response?.data?.error?.message ?? 'Could not upload document')
      } else {
        setError(err.message)
      }
    },
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    upload.mutate()
  }

  return (
    <section>
      <h2 className="text-2xl font-semibold">Documents</h2>
      <p className="mt-1 text-sm text-slate-600">
        Tenant-owned files stored through a local or S3 storage adapter. Download is authenticated; executables are rejected.
      </p>
      {query.isError && (
        <p className="mt-4 text-sm text-red-700">You do not have DOCUMENT_VIEW, tenant scope is missing, or the API is unavailable.</p>
      )}
      <div className="mt-4 overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-2">Number</th>
              <th className="px-4 py-2">Title</th>
              <th className="px-4 py-2">Category</th>
              <th className="px-4 py-2">Link</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((doc) => (
              <tr key={doc.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-mono text-xs">
                  <Link className="text-brand-500 underline" to={`/documents/${doc.id}`}>
                    {doc.documentNumber}
                  </Link>
                </td>
                <td className="px-4 py-2">{doc.title}</td>
                <td className="px-4 py-2">{doc.category}</td>
                <td className="px-4 py-2">
                  {doc.linkedType}
                  {doc.linkedId ? ` · ${doc.linkedId.slice(0, 8)}` : ''}
                </td>
              </tr>
            ))}
            {rows.length === 0 && !query.isPending && (
              <tr>
                <td className="px-4 py-6 text-slate-500" colSpan={4}>
                  No documents in this tenant.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {hasPermission('DOCUMENT_UPLOAD') && (
        <form className="mt-8 max-w-xl space-y-3 rounded-lg border border-slate-200 bg-white p-4" onSubmit={onSubmit} aria-label="Upload document">
          <h3 className="text-lg font-medium">Upload</h3>
          <input
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            placeholder="Title"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
          />
          <select
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            value={category}
            onChange={(event) => setCategory(event.target.value as DocumentCategory)}
          >
            <option value="EVIDENCE">Evidence</option>
            <option value="CONTROLLED">Controlled</option>
            <option value="REPORT">Report</option>
            <option value="OTHER">Other</option>
          </select>
          <select
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            value={linkedType}
            onChange={(event) => setLinkedType(event.target.value as DocumentLinkType)}
          >
            <option value="GENERAL">General</option>
            <option value="CLIENT">Client</option>
            <option value="AUDIT">Audit</option>
            <option value="FINDING">Finding</option>
            <option value="CERTIFICATE">Certificate</option>
          </select>
          {linkedType !== 'GENERAL' && (
            <input
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
              placeholder="Linked record id"
              value={linkedId}
              onChange={(event) => setLinkedId(event.target.value)}
            />
          )}
          <input
            className="w-full text-sm"
            type="file"
            onChange={(event) => setFile(event.target.files?.[0] ?? null)}
          />
          {error && (
            <p className="text-sm text-red-700" role="alert">
              {error}
            </p>
          )}
          <button type="submit" className="rounded-md bg-brand-500 px-4 py-2 text-sm text-white">
            Upload
          </button>
        </form>
      )}
    </section>
  )
}
