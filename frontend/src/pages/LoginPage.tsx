import { zodResolver } from '@hookform/resolvers/zod'
import { AxiosError } from 'axios'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, Navigate } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from '../auth/AuthProvider'
import type { ApiResponse } from '../api/types'

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(1, 'Password is required'),
  mfaCode: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const { user, login } = useAuth()
  const [mfaRequired, setMfaRequired] = useState(false)
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: '', password: '', mfaCode: '' },
  })

  if (user) {
    return <Navigate to="/" replace />
  }

  async function onSubmit(values: FormValues) {
    try {
      await login(values.email, values.password, values.mfaCode || undefined)
    } catch (error) {
      const axiosError = error as AxiosError<ApiResponse<unknown>>
      const code = axiosError.response?.data?.error?.code
      if (code === 'AUTH_MFA_REQUIRED') {
        setMfaRequired(true)
      }
      form.setError('root', {
        message: axiosError.response?.data?.error?.message ?? 'Invalid email or password',
      })
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-6">
      <main className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-xl font-semibold text-slate-900">Sign in</h1>
        <p className="mt-2 text-sm text-slate-600">Use your organisation account. Sessions use JWT access and rotating refresh tokens.</p>
        <form className="mt-6 space-y-4" onSubmit={form.handleSubmit(onSubmit)} aria-label="Sign in">
          <label className="block text-sm font-medium text-slate-700">
            Email
            <input
              type="email"
              autoComplete="username"
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
              {...form.register('email')}
            />
          </label>
          <label className="block text-sm font-medium text-slate-700">
            Password
            <input
              type="password"
              autoComplete="current-password"
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
              {...form.register('password')}
            />
          </label>
          {mfaRequired && (
            <label className="block text-sm font-medium text-slate-700">
              Authenticator code
              <input
                type="text"
                inputMode="numeric"
                autoComplete="one-time-code"
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
                {...form.register('mfaCode')}
              />
            </label>
          )}
          {form.formState.errors.root && (
            <p className="text-sm text-red-700" role="alert">
              {form.formState.errors.root.message}
            </p>
          )}
          <button
            type="submit"
            disabled={form.formState.isSubmitting}
            className="w-full rounded-md bg-brand-500 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
          >
            {form.formState.isSubmitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>
        <p className="mt-4 text-sm">
          <Link className="text-brand-500 underline" to="/forgot-password">
            Forgot password
          </Link>
        </p>
      </main>
    </div>
  )
}
