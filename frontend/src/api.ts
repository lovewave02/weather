export type ProblemDetail = {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
}

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
  })

  if (!response.ok) {
    const message = await readErrorMessage(response)
    throw new ApiError(response.status, message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  if (!text) {
    return undefined as T
  }
  return JSON.parse(text) as T
}

async function readErrorMessage(response: Response): Promise<string> {
  try {
    const contentType = response.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
      const body = (await response.json()) as ProblemDetail
      return body.detail || body.title || response.statusText || 'request failed'
    }
  } catch {
    // ignore
  }
  try {
    const text = await response.text()
    if (text) return text
  } catch {
    // ignore
  }
  return response.statusText || 'request failed'
}

export type HealthResponse = {
  status: string
}

export type LocationResponse = {
  id: string
  name: string
  latitude: number
  longitude: number
  createdAt: string
}

export type CreateLocationRequest = {
  name: string
  latitude: number
  longitude: number
}

export type CurrentWeatherResponse = {
  locationId: string
  observedAt: string
  temperatureC: number | null
  apparentTemperatureC: number | null
  precipitationMm: number | null
  weatherCode: number | null
  source: string
}

export type HourlyWeatherPoint = {
  time: string
  temperatureC: number | null
  apparentTemperatureC: number | null
  weatherCode: number | null
}

export type TemperatureStats = {
  min: number | null
  max: number | null
  avg: number | null
}

export type HourlyWeatherResponse = {
  locationId: string
  hours: number
  fetchedAt: string
  points: HourlyWeatherPoint[]
  temperature: TemperatureStats
  apparentTemperature: TemperatureStats
}

export async function getHealth(): Promise<HealthResponse> {
  return request<HealthResponse>('/actuator/health')
}

export async function listLocations(): Promise<LocationResponse[]> {
  return request<LocationResponse[]>('/api/v1/locations')
}

export async function createLocation(payload: CreateLocationRequest): Promise<LocationResponse> {
  return request<LocationResponse>('/api/v1/locations', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function runIngest(): Promise<void> {
  await request<void>('/api/v1/ingest/run', { method: 'POST' })
}

export async function getCurrentWeather(locationId: string): Promise<CurrentWeatherResponse> {
  return request<CurrentWeatherResponse>(`/api/v1/locations/${locationId}/weather/current`)
}

export async function getHourlyWeather(locationId: string, hours = 24): Promise<HourlyWeatherResponse> {
  return request<HourlyWeatherResponse>(`/api/v1/locations/${locationId}/weather/hourly?hours=${hours}`)
}
