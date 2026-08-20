import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const correlationId = crypto.randomUUID()
  config.headers.set('X-Correlation-Id', correlationId)
  return config
})
