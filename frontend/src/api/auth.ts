import { api } from './client'
import type { ApiResponse, AuthSession, TokenPayload, UserSummary } from './types'

export async function login(email: string, password: string): Promise<ApiResponse<TokenPayload>> {
  const response = await api.post<ApiResponse<TokenPayload>>('/auth/login', { email, password })
  return response.data
}

export async function logout(refreshToken: string | null): Promise<void> {
  await api.post('/auth/logout', { refreshToken })
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
