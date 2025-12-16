import './App.css'
import { useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  ApiError,
  createLocation,
  getCurrentWeather,
  getHealth,
  getHourlyWeather,
  listLocations,
  runIngest,
  type CurrentWeatherResponse,
  type HourlyWeatherResponse,
  type LocationResponse,
} from './api'
import { HourlyTempChart } from './HourlyTempChart'
import { KOREA_CITIES } from './koreaCities'
import { describeWeather, WeatherIcon } from './weatherVisuals'

type LoadState = 'idle' | 'loading' | 'ready' | 'empty' | 'error'

type CurrentEntry =
  | { state: 'idle' | 'loading' }
  | { state: 'ready'; data: CurrentWeatherResponse }
  | { state: 'empty' }
  | { state: 'error'; error: string }

const TIME_ZONE = 'Asia/Seoul'

function errorMessage(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return String(error)
}

function formatKst(value: string) {
  try {
    return new Date(value).toLocaleString('ko-KR', { timeZone: TIME_ZONE, hour12: false })
  } catch {
    return value
  }
}

function fmtNumber(value: number | null | undefined, unit = '', digits = 1) {
  if (value == null || !Number.isFinite(value)) return '-'
  return `${value.toFixed(digits)}${unit}`
}

function App() {
  const [health, setHealth] = useState<string>('unknown')
  const [healthHint, setHealthHint] = useState<string | null>(null)

  const [locations, setLocations] = useState<LocationResponse[]>([])
  const [locationsState, setLocationsState] = useState<LoadState>('idle')
  const [selectedLocationId, setSelectedLocationId] = useState<string | null>(null)
  const [filter, setFilter] = useState<string>('')

  const [currentById, setCurrentById] = useState<Record<string, CurrentEntry>>({})

  const [hourly, setHourly] = useState<HourlyWeatherResponse | null>(null)
  const [hourlyState, setHourlyState] = useState<LoadState>('idle')
  const [hourlyHint, setHourlyHint] = useState<string | null>(null)

  const [formName, setFormName] = useState('서울')
  const [formLat, setFormLat] = useState('37.5665')
  const [formLon, setFormLon] = useState('126.9780')
  const [formHint, setFormHint] = useState<string | null>(null)

  const [seedHint, setSeedHint] = useState<string | null>(null)
  const [seeding, setSeeding] = useState(false)
  const [ingesting, setIngesting] = useState(false)

  const selectedLocation = useMemo(() => {
    if (!selectedLocationId) return null
    return locations.find((loc) => loc.id === selectedLocationId) ?? null
  }, [locations, selectedLocationId])

  const selectedCurrent = useMemo(() => {
    if (!selectedLocationId) return null
    const entry = currentById[selectedLocationId]
    return entry && entry.state === 'ready' ? entry.data : null
  }, [currentById, selectedLocationId])

  const visibleLocations = useMemo(() => {
    const query = filter.trim()
    if (!query) return locations
    return locations.filter((l) => l.name.toLowerCase().includes(query.toLowerCase()))
  }, [filter, locations])

  async function refreshHealth() {
    setHealthHint(null)
    try {
      const res = await getHealth()
      setHealth(res.status)
    } catch (error) {
      setHealth('unreachable')
      setHealthHint(errorMessage(error))
    }
  }

  async function refreshCurrentAll(target: LocationResponse[]) {
    const ids = target.map((l) => l.id)
    setCurrentById((prev) => {
      const next: Record<string, CurrentEntry> = { ...prev }
      for (const id of ids) {
        next[id] = { state: 'loading' }
      }
      return next
    })

    const results = await Promise.allSettled(target.map((l) => getCurrentWeather(l.id)))
    const updated: Record<string, CurrentEntry> = {}
    results.forEach((res, idx) => {
      const id = ids[idx]
      if (res.status === 'fulfilled') {
        updated[id] = { state: 'ready', data: res.value }
        return
      }
      const err = res.reason as unknown
      if (err instanceof ApiError && err.status === 404) {
        updated[id] = { state: 'empty' }
        return
      }
      updated[id] = { state: 'error', error: errorMessage(err) }
    })
    setCurrentById((prev) => ({ ...prev, ...updated }))
  }

  async function refreshLocations(preserveSelection = true): Promise<LocationResponse[]> {
    setLocationsState('loading')
    setFormHint(null)
    try {
      const res = await listLocations()
      setLocations(res)
      setLocationsState('ready')

      let nextSelected = selectedLocationId
      if (!preserveSelection || !selectedLocationId) {
        nextSelected = res[0]?.id ?? null
      } else if (!res.some((l) => l.id === selectedLocationId)) {
        nextSelected = res[0]?.id ?? null
      }
      setSelectedLocationId(nextSelected)

      void refreshCurrentAll(res)
      return res
    } catch (error) {
      setLocationsState('error')
      setFormHint(errorMessage(error))
      return []
    }
  }

  async function refreshHourly(locationId: string) {
    setHourlyState('loading')
    setHourlyHint(null)
    try {
      const res = await getHourlyWeather(locationId, 24)
      setHourly(res)
      setHourlyState('ready')
    } catch (error) {
      setHourly(null)
      setHourlyState('error')
      setHourlyHint(errorMessage(error))
    }
  }

  async function handleCreateLocation(event: FormEvent) {
    event.preventDefault()
    setFormHint(null)

    const latitude = Number(formLat)
    const longitude = Number(formLon)
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      setFormHint('위도/경도는 숫자로 입력해줘.')
      return
    }

    try {
      const created = await createLocation({ name: formName.trim(), latitude, longitude })
      const res = await refreshLocations(true)
      setSelectedLocationId(created.id)
      void refreshCurrentAll(res)
      setFormHint(null)
    } catch (error) {
      setFormHint(errorMessage(error))
    }
  }

  async function ingestAndRefresh(targetLocations: LocationResponse[] = locations) {
    if (targetLocations.length === 0) return
    setIngesting(true)
    try {
      await runIngest()
      await refreshCurrentAll(targetLocations)
      if (selectedLocationId) {
        void refreshHourly(selectedLocationId)
      }
    } catch (error) {
      setSeedHint(errorMessage(error))
    } finally {
      setIngesting(false)
    }
  }

  async function handleSeedKorea() {
    setSeeding(true)
    setSeedHint(null)

    let created = 0
    let existed = 0
    let failed = 0

    for (const city of KOREA_CITIES) {
      try {
        await createLocation(city)
        created++
      } catch (error) {
        if (error instanceof ApiError && error.status === 409) {
          existed++
          continue
        }
        failed++
      }
    }

    const res = await refreshLocations(true)
    setSeedHint(`Korea preset: 추가 ${created}, 이미 있음 ${existed}${failed ? `, 실패 ${failed}` : ''}`)
    setSeeding(false)

    if (res.length > 0) void ingestAndRefresh(res)
  }

  useEffect(() => {
    void refreshHealth()
    void refreshLocations(true)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (!selectedLocationId) {
      setHourly(null)
      setHourlyState('idle')
      return
    }
    void refreshHourly(selectedLocationId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedLocationId])

  const selectedVisual = describeWeather(selectedCurrent?.weatherCode)

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <div className="title">Korea Weather Dashboard</div>
          <div className="subtitle">React + Spring Boot · current(ingest+cache) + hourly(forecast+cache)</div>
        </div>
        <div className="status">
          <span className={`badge ${health === 'UP' ? 'ok' : health === 'unreachable' ? 'bad' : 'warn'}`}>
            backend {health}
          </span>
          <button className="ghost" onClick={() => void refreshHealth()}>
            Health
          </button>
        </div>
      </header>

      {healthHint && <div className="notice">Backend health error: {healthHint}</div>}

      <main className="layoutWide">
        <section className="panel">
          <div className="panelHeader">
            <h2>전국</h2>
            <div className="row">
              <button className="ghost" onClick={() => void refreshLocations(true)} disabled={locationsState === 'loading'}>
                Refresh
              </button>
              <button onClick={() => void ingestAndRefresh()} disabled={locations.length === 0 || ingesting}>
                {ingesting ? 'Ingesting…' : 'Ingest Now'}
              </button>
              <button className="ghost" onClick={() => void handleSeedKorea()} disabled={seeding}>
                {seeding ? 'Loading…' : 'Load Korea'}
              </button>
            </div>
          </div>

          <div className="toolbar">
            <input value={filter} onChange={(e) => setFilter(e.target.value)} placeholder="도시 검색 (예: 서울, 부산)" />
            <div className="hint">{seedHint ?? formHint}</div>
          </div>

          {locationsState === 'loading' && <div className="muted">Loading locations…</div>}
          {locationsState !== 'loading' && locations.length === 0 && (
            <div className="emptyState">
              <div className="emptyTitle">아직 저장된 지역이 없어.</div>
              <div className="muted small">먼저 전국 프리셋을 추가하거나, 아래에서 커스텀 위치를 추가해줘.</div>
              <div className="row">
                <button onClick={() => void handleSeedKorea()} disabled={seeding}>
                  {seeding ? 'Loading…' : 'Load Korea (17)'}
                </button>
              </div>
            </div>
          )}

          <div className="cityGrid">
            {visibleLocations.map((loc) => {
              const entry = currentById[loc.id] ?? { state: 'idle' }
              const current = entry.state === 'ready' ? entry.data : null
              const visual = describeWeather(current?.weatherCode)

              return (
                <button
                  key={loc.id}
                  className={`cityCard ${selectedLocationId === loc.id ? 'selected' : ''} kind-${visual.kind}`}
                  onClick={() => setSelectedLocationId(loc.id)}
                >
                  <div className="cityTop">
                    <div className="cityName">{loc.name}</div>
                    <WeatherIcon kind={visual.kind} />
                  </div>

                  <div className="cityTempRow">
                    <div className="cityTemp">{fmtNumber(current?.temperatureC, '°', 0)}</div>
                    <div className="cityFeels">체감 {fmtNumber(current?.apparentTemperatureC, '°', 0)}</div>
                  </div>

                  <div className="cityMeta">
                    <span className="pill">{visual.label}</span>
                    <span className="pill">강수 {fmtNumber(current?.precipitationMm, 'mm', 1)}</span>
                  </div>

                  <div className="cityTime">
                    {entry.state === 'loading' && '불러오는 중…'}
                    {entry.state === 'empty' && '스냅샷 없음 (Ingest 필요)'}
                    {entry.state === 'error' && '오류'}
                    {entry.state === 'ready' && current && `관측 ${formatKst(current.observedAt)}`}
                  </div>
                </button>
              )
            })}
          </div>

          <details className="collapse">
            <summary>커스텀 위치 추가</summary>
            <form className="form" onSubmit={(e) => void handleCreateLocation(e)}>
              <div className="grid">
                <label>
                  Name
                  <input value={formName} onChange={(e) => setFormName(e.target.value)} placeholder="서울" />
                </label>
                <label>
                  Lat
                  <input value={formLat} onChange={(e) => setFormLat(e.target.value)} placeholder="37.5665" />
                </label>
                <label>
                  Lon
                  <input value={formLon} onChange={(e) => setFormLon(e.target.value)} placeholder="126.9780" />
                </label>
              </div>
              <div className="row">
                <button type="submit">Add</button>
                <div className="hint">{formHint}</div>
              </div>
            </form>
          </details>
        </section>

        <section className="panel">
          <div className="panelHeader">
            <h2>상세</h2>
            <div className="row">
              <button
                className="ghost"
                onClick={() => selectedLocationId && void refreshHourly(selectedLocationId)}
                disabled={!selectedLocationId || hourlyState === 'loading'}
              >
                {hourlyState === 'loading' ? 'Loading…' : 'Reload Hourly'}
              </button>
            </div>
          </div>

          {!selectedLocation && <div className="muted">왼쪽에서 도시를 선택해줘.</div>}

          {selectedLocation && (
            <>
              <div className={`hero kind-${selectedVisual.kind}`}>
                <div className="heroLeft">
                  <div className="heroTitle">{selectedLocation.name}</div>
                  <div className="heroSub">
                    {selectedLocation.latitude}, {selectedLocation.longitude}
                  </div>
                  <div className="heroSub">{selectedCurrent ? `관측 ${formatKst(selectedCurrent.observedAt)}` : '현재 스냅샷 없음'}</div>
                </div>
                <div className="heroRight">
                  <WeatherIcon kind={selectedVisual.kind} size={52} />
                  <div className="heroTemp">{fmtNumber(selectedCurrent?.temperatureC, '°', 0)}</div>
                  <div className="heroFeels">{selectedVisual.label} · 체감 {fmtNumber(selectedCurrent?.apparentTemperatureC, '°', 0)}</div>
                </div>
              </div>

              <div className="statsGrid">
                <div className="stat">
                  <div className="statLabel">최저기온</div>
                  <div className="statValue">{fmtNumber(hourly?.temperature.min, '°', 0)}</div>
                </div>
                <div className="stat">
                  <div className="statLabel">최고기온</div>
                  <div className="statValue">{fmtNumber(hourly?.temperature.max, '°', 0)}</div>
                </div>
                <div className="stat">
                  <div className="statLabel">평균기온</div>
                  <div className="statValue">{fmtNumber(hourly?.temperature.avg, '°', 0)}</div>
                </div>
                <div className="stat">
                  <div className="statLabel">체감기온</div>
                  <div className="statValue">{fmtNumber(selectedCurrent?.apparentTemperatureC, '°', 0)}</div>
                </div>
              </div>

              {hourlyState === 'error' && hourlyHint && <div className="notice">Hourly error: {hourlyHint}</div>}
              {hourlyState === 'ready' && hourly && <HourlyTempChart points={hourly.points} timeZone={TIME_ZONE} />}

              {hourlyState !== 'ready' && <div className="muted small">시간별 예보는 선택한 도시 기준으로 가져와.</div>}
            </>
          )}
        </section>
      </main>
    </div>
  )
}

export default App
