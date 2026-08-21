import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { fetchCsrf, fetchMe, login as loginApi, logout as logoutApi } from '../api/auth'
import type { UserSummary } from '../api/types'
import { cookieSessionsEnabled } from './cookieSessions'
import { tokenStore } from './tokenStore'

type AuthContextValue = {
  user: UserSummary | null
  loading: boolean
  login: (email: string, password: string, mfaCode?: string) => Promise<void>
  logout: () => Promise<void>
  hasPermission: (code: string) => boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(null)
  const [loading, setLoading] = useState(true)

  const hydrate = useCallback(async () => {
    if (cookieSessionsEnabled) {
      try {
        await fetchCsrf()
        const response = await fetchMe()
        setUser(response.data)
      } catch {
        setUser(null)
      } finally {
        setLoading(false)
      }
      return
    }
    if (!tokenStore.get()) {
      setUser(null)
      setLoading(false)
      return
    }
    try {
      const response = await fetchMe()
      setUser(response.data)
    } catch {
      tokenStore.clear()
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void hydrate()
  }, [hydrate])

  const login = useCallback(async (email: string, password: string, mfaCode?: string) => {
    if (cookieSessionsEnabled) {
      await fetchCsrf()
    }
    const response = await loginApi(email, password, mfaCode)
    if (!response.success || !response.data) {
      throw new Error(response.error?.message ?? 'Sign in failed')
    }
    if (!cookieSessionsEnabled) {
      const accessToken = response.data.accessToken
      const refreshToken = response.data.refreshToken
      if (!accessToken || !refreshToken) {
        throw new Error('Sign in failed')
      }
      tokenStore.set({ accessToken, refreshToken })
    }
    setUser(response.data.user)
  }, [])

  const logout = useCallback(async () => {
    const session = tokenStore.get()
    try {
      if (cookieSessionsEnabled) {
        await fetchCsrf()
      }
      await logoutApi(session?.refreshToken ?? null)
    } finally {
      tokenStore.clear()
      setUser(null)
    }
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      loading,
      login,
      logout,
      hasPermission: (code: string) => Boolean(user?.permissions.includes(code)),
    }),
    [user, loading, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
