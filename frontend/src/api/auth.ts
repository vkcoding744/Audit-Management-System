import { api } from './client'
import type { ApiResponse, AuthSession, TokenPayload, UserSummary } from './types'

export interface MfaStatus {
  mfaEnabled: boolean
}

export interface MfaSetup {
  secret: string
  otpauthUri: string
  mfaEnabled: boolean
}

export async function login(email: string, password: string, mfaCode?: string): Promise<ApiResponse<TokenPayload>> {
  const response = await api.post<ApiResponse<TokenPayload>>('/auth/login', {
    email,
    password,
    mfaCode: mfaCode || undefined,
  })
  return response.data
}

export async function logout(refreshToken: string | null): Promise<void> {
  await api.post('/auth/logout', { refreshToken })
}

export async function fetchCsrf(): Promise<ApiResponse<{ enabled: boolean; headerName: string | null; token: string | null }>> {
  const response = await api.get('/auth/csrf')
  return response.data
}

export async function fetchMe(): Promise<ApiResponse<UserSummary>> {
  const response = await api.get<ApiResponse<UserSummary>>('/auth/me')
  return response.data
}

export async function forgotPassword(email: string): Promise<ApiResponse<{ message: string; resetToken: string | null }>> {
  const response = await api.post('/auth/forgot-password', { email })
  return response.data
}

export async function resetPassword(token: string, newPassword: string): Promise<ApiResponse<null>> {
  const response = await api.post('/auth/reset-password', { token, newPassword })
  return response.data
}

export async function fetchSessions(): Promise<ApiResponse<AuthSession[]>> {
  const response = await api.get<ApiResponse<AuthSession[]>>('/auth/sessions')
  return response.data
}

export async function fetchMfaStatus(): Promise<ApiResponse<MfaStatus>> {
  const response = await api.get<ApiResponse<MfaStatus>>('/auth/mfa')
  return response.data
}

export async function setupMfa(): Promise<ApiResponse<MfaSetup>> {
  const response = await api.post<ApiResponse<MfaSetup>>('/auth/mfa/setup')
  return response.data
}

export async function enableMfa(code: string): Promise<ApiResponse<MfaStatus>> {
  const response = await api.post<ApiResponse<MfaStatus>>('/auth/mfa/enable', { code })
  return response.data
}

export async function disableMfa(code: string, password: string): Promise<ApiResponse<MfaStatus>> {
  const response = await api.post<ApiResponse<MfaStatus>>('/auth/mfa/disable', { code, password })
  return response.data
}
