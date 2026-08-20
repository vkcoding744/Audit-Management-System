export type StoredSession = {
  accessToken: string
  refreshToken: string
}

const ACCESS_KEY = 'audit.accessToken'
const REFRESH_KEY = 'audit.refreshToken'

export const tokenStore = {
  get(): StoredSession | null {
    const accessToken = sessionStorage.getItem(ACCESS_KEY)
    const refreshToken = sessionStorage.getItem(REFRESH_KEY)
    if (!accessToken || !refreshToken) {
      return null
    }
    return { accessToken, refreshToken }
  },
  set(session: StoredSession) {
    sessionStorage.setItem(ACCESS_KEY, session.accessToken)
    sessionStorage.setItem(REFRESH_KEY, session.refreshToken)
  },
  clear() {
    sessionStorage.removeItem(ACCESS_KEY)
    sessionStorage.removeItem(REFRESH_KEY)
  },
}
