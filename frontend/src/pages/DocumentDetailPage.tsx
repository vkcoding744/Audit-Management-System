import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deleteDocument, downloadDocument, fetchDocument } from '../api/documents'
import type { ApiResponse } from '../api/types'
import { useAuth } from '../auth/AuthProvider'

function linkedPath(type: string, id: string | null): string | null {
  if (!id) {
    return null
  }
  switch (type) {
    case 'CLIENT':
      return `/clients/${id}`
    case 'AUDIT':
      return `/audits/${id}`
    case 'FINDING':
      return `/findings/${id}`
    case 'CERTIFICATE':
      return `/certificates/${id}`
    default:
      return null
  }
}

export function DocumentDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { hasPermission } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['document', id], queryFn: () => fetchDocument(id!), enabled: Boolean(id) })
  const documentRow = query.data?.data

  const remove = useMutation({
    mutationFn: () => deleteDocument(id!),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['documents'] })
      navigate('/documents')
    },
  })

  if (query.isError || !id) {
    return <p className="text-sm text-red-700">Document not found or you do not have DOCUMENT_VIEW.</p>
  }
  if (!documentRow) {
    return <p className="text-sm text-slate-600">Loading document…</p>
  }

  const href = linkedPath(documentRow.linkedType, documentRow.linkedId)
  const deleteError = remove.error as AxiosError<ApiResponse<unknown>> | undefined

  return (
    <section>
      <p className="text-sm text-slate-500">
        <Link className="underline" to="/documents">
          Documents
        </Link>{' '}
        / {documentRow.documentNumber}
      </p>
      <h2 className="mt-2 text-2xl font-semibold">{documentRow.title}</h2>
      <p className="mt-1 text-sm text-slate-600">
        {documentRow.category} · {documentRow.originalFilename} · {documentRow.contentType} · {documentRow.sizeBytes} bytes
      </p>
      {href && (
        <p className="mt-2 text-sm">
          Linked {documentRow.linkedType.toLowerCase()}:{' '}
          <Link className="text-brand-500 underline" to={href}>
            open record
          </Link>
        </p>
      )}
      <p className="mt-2 font-mono text-xs text-slate-500">SHA-256 {documentRow.checksumSha256}</p>
      <div className="mt-4 flex flex-wrap gap-2">
        {hasPermission('DOCUMENT_DOWNLOAD') && (
          <button
            type="button"
            className="rounded-md bg-brand-500 px-3 py-1 text-sm text-white"
            onClick={() => void downloadDocument(id, documentRow.originalFilename)}
          >
            Download
          </button>
        )}
        {hasPermission('DOCUMENT_DELETE') && (
          <button
            type="button"
            className="rounded-md border border-red-300 px-3 py-1 text-sm text-red-700"
            onClick={() => remove.mutate()}
          >
            Delete
          </button>
        )}
      </div>
      {deleteError && (
        <p className="mt-2 text-sm text-red-700">{deleteError.response?.data?.error?.message ?? 'Could not delete document'}</p>
      )}
    </section>
  )
}
