const KEY = 'audit-platform.tenant-scope'

export const tenantScope = {
  get(): string | null {
    return sessionStorage.getItem(KEY)
  },
  set(id: string | null) {
    if (!id) {
      sessionStorage.removeItem(KEY)
      return
    }
    sessionStorage.setItem(KEY, id)
  },
}
