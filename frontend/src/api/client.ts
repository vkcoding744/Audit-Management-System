import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { cookieSessionsEnabled, readCookie } from '../auth/cookieSessions'
import { tenantScope } from '../auth/tenantScope'
import { tokenStore } from '../auth/tokenStore'
import type { ApiResponse, TokenPayload } from './types'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  withCredentials: cookieSessionsEnabled,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const correlationId = crypto.randomUUID()
  config.headers.set('X-Correlation-Id', correlationId)
  if (!cookieSessionsEnabled) {
    const session = tokenStore.get()
    if (session?.accessToken) {
      config.headers.set('Authorization', `Bearer ${session.accessToken}`)
    }
  } else {
    const xsrf = readCookie('XSRF-TOKEN')
    if (xsrf) {
      config.headers.set('X-XSRF-TOKEN', xsrf)
    }
  }
  const tenantId = tenantScope.get()
  if (tenantId) {
    config.headers.set('X-Tenant-Id', tenantId)
  }
  if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
    config.headers.delete('Content-Type')
  }
  return config
})

let refreshInFlight: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  if (cookieSessionsEnabled) {
    const response = await axios.post<ApiResponse<TokenPayload>>(
      `${api.defaults.baseURL}/auth/refresh`,
      {},
      { withCredentials: true, headers: { 'X-XSRF-TOKEN': readCookie('XSRF-TOKEN') ?? '' } },
    )
    return response.data.success ? 'cookie' : null
  }
  const session = tokenStore.get()
  if (!session?.refreshToken) {
    return null
  }
  const response = await axios.post<ApiResponse<TokenPayload>>(
    `${api.defaults.baseURL}/auth/refresh`,
    { refreshToken: session.refreshToken },
  )
  const payload = response.data.data
  if (!payload?.accessToken || !payload.refreshToken) {
    tokenStore.clear()
    return null
  }
  tokenStore.set({ accessToken: payload.accessToken, refreshToken: payload.refreshToken })
  return payload.accessToken
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    if (!original || original._retry || error.response?.status !== 401) {
      return Promise.reject(error)
    }
    if (original.url?.includes('/auth/login') || original.url?.includes('/auth/refresh')) {
      return Promise.reject(error)
    }
    original._retry = true
    refreshInFlight = refreshInFlight ?? refreshAccessToken().finally(() => {
      refreshInFlight = null
    })
    const access = await refreshInFlight
    if (!access) {
      tokenStore.clear()
      return Promise.reject(error)
    }
    if (!cookieSessionsEnabled) {
      original.headers.set('Authorization', `Bearer ${access}`)
    }
    return api.request(original)
  },
)
