import { describe, expect, it } from 'vitest'
import { cookieSessionsEnabled, readCookie } from './cookieSessions'

describe('cookieSessions', () => {
  it('defaults cookie mode off in tests', () => {
    expect(cookieSessionsEnabled).toBe(false)
  })

  it('reads a document cookie', () => {
    document.cookie = 'XSRF-TOKEN=abc%2Fdef'
    expect(readCookie('XSRF-TOKEN')).toBe('abc/def')
  })
})
