import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useState } from 'react'
import { disableMfa, enableMfa, fetchMfaStatus, fetchSessions, setupMfa } from '../api/auth'
import type { ApiResponse } from '../api/types'

export function SessionsPage() {
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['sessions'], queryFn: fetchSessions })
  const mfaQuery = useQuery({ queryKey: ['mfa'], queryFn: fetchMfaStatus })
  const rows = query.data?.data ?? []
  const mfaEnabled = mfaQuery.data?.data?.mfaEnabled ?? false

  const [setupSecret, setSetupSecret] = useState<string | null>(null)
  const [setupUri, setSetupUri] = useState<string | null>(null)
  const [enableCode, setEnableCode] = useState('')
  const [disableCode, setDisableCode] = useState('')
  const [disablePassword, setDisablePassword] = useState('')
  const [mfaMessage, setMfaMessage] = useState<string | null>(null)

  const setupMutation = useMutation({
    mutationFn: setupMfa,
    onSuccess: (response) => {
      setSetupSecret(response.data?.secret ?? null)
      setSetupUri(response.data?.otpauthUri ?? null)
      setMfaMessage('Scan the otpauth URI or enter the secret in your authenticator, then confirm with a code.')
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      setMfaMessage(error.response?.data?.error?.message ?? 'Unable to start MFA setup')
    },
  })

  const enableMutation = useMutation({
    mutationFn: () => enableMfa(enableCode),
    onSuccess: async () => {
      setSetupSecret(null)
      setSetupUri(null)
      setEnableCode('')
      setMfaMessage('Authenticator MFA is enabled.')
      await queryClient.invalidateQueries({ queryKey: ['mfa'] })
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      setMfaMessage(error.response?.data?.error?.message ?? 'Unable to enable MFA')
    },
  })

  const disableMutation = useMutation({
    mutationFn: () => disableMfa(disableCode, disablePassword),
    onSuccess: async () => {
      setDisableCode('')
      setDisablePassword('')
      setMfaMessage('Authenticator MFA is disabled.')
      await queryClient.invalidateQueries({ queryKey: ['mfa'] })
    },
    onError: (error: AxiosError<ApiResponse<unknown>>) => {
      setMfaMessage(error.response?.data?.error?.message ?? 'Unable to disable MFA')
    },
  })

  return (
    <section>
      <h2 className="text-2xl font-semibold">Device sessions</h2>
      <p className="mt-1 text-sm text-slate-600">Refresh tokens are stored hashed. Rotation invalidates the previous token.</p>
      <ul className="mt-4 space-y-2">
        {rows.map((session) => (
          <li key={session.id} className="rounded-lg border border-slate-200 bg-white p-4 text-sm">
            <p>
              {session.current ? 'Current session' : 'Session'} · {session.revoked ? 'revoked' : 'active'}
            </p>
            <p className="text-slate-500">{session.ipAddress ?? 'unknown IP'}</p>
            <p className="truncate text-slate-500">{session.userAgent}</p>
          </li>
        ))}
      </ul>

      <div className="mt-8 rounded-lg border border-slate-200 bg-white p-4">
        <h3 className="text-lg font-medium">Authenticator MFA</h3>
        <p className="mt-1 text-sm text-slate-600">
          {mfaEnabled ? 'TOTP is enabled for this account.' : 'TOTP is optional. Setup stores an encrypted secret; login requires a code only after you enable it.'}
        </p>
        {mfaMessage && (
          <p className="mt-2 text-sm text-slate-700" role="status">
            {mfaMessage}
          </p>
        )}
        {!mfaEnabled && (
          <div className="mt-4 space-y-3">
            <button
              type="button"
              className="rounded-md bg-brand-500 px-3 py-1.5 text-sm text-white"
              onClick={() => setupMutation.mutate()}
            >
              Generate authenticator secret
            </button>
            {setupSecret && (
              <div className="space-y-2 text-sm">
                <p className="break-all font-mono text-xs">{setupSecret}</p>
                {setupUri && <p className="break-all text-xs text-slate-500">{setupUri}</p>}
                <label className="block font-medium text-slate-700">
                  Confirmation code
                  <input
                    className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2"
                    value={enableCode}
                    onChange={(event) => setEnableCode(event.target.value)}
                    inputMode="numeric"
                    autoComplete="one-time-code"
                  />
                </label>
                <button
                  type="button"
                  className="rounded-md border border-slate-300 px-3 py-1.5"
                  onClick={() => enableMutation.mutate()}
                >
                  Enable MFA
                </button>
              </div>
            )}
          </div>
        )}
        {mfaEnabled && (
          <form
            className="mt-4 space-y-3"
            onSubmit={(event) => {
              event.preventDefault()
              disableMutation.mutate()
            }}
          >
            <label className="block text-sm font-medium text-slate-700">
              Authenticator code
              <input
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                value={disableCode}
                onChange={(event) => setDisableCode(event.target.value)}
                inputMode="numeric"
                autoComplete="one-time-code"
              />
            </label>
            <label className="block text-sm font-medium text-slate-700">
              Password
              <input
                type="password"
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                value={disablePassword}
                onChange={(event) => setDisablePassword(event.target.value)}
                autoComplete="current-password"
              />
            </label>
            <button type="submit" className="rounded-md border border-red-300 px-3 py-1.5 text-sm text-red-700">
              Disable MFA
            </button>
          </form>
        )}
      </div>
    </section>
  )
}
