import './App.css'
import { useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  ApiError,
  createSubscription,
  createUser,
  createLocation,
  disableSubscription,
  enableSubscription,
  getCurrentWeather,
  getHealth,
  getHourlyWeather,
  getUserByEmail,
  listAlerts,
  listLocations,
  listSubscriptions,
  runIngest,
  type AlertEventResponse,
  type CurrentWeatherResponse,
  type CreateSubscriptionRequest,
  type IngestRunResponse,
  type HourlyWeatherResponse,
  type LocationResponse,
  type RuleType,
  type SubscriptionResponse,
  type UserResponse,
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

type AlertLoadState = 'idle' | 'loading' | 'ready' | 'empty' | 'error'

const TIME_ZONE = 'Asia/Seoul'
const ALERT_EMAIL_STORAGE_KEY = 'weather:last-alert-email'

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

function readSavedAlertEmail() {
  if (typeof window === 'undefined') return null
  const value = window.localStorage.getItem(ALERT_EMAIL_STORAGE_KEY)?.trim()
  return value ? value : null
}

function saveAlertEmail(email: string) {
  if (typeof window === 'undefined') return
  const value = email.trim()
  if (!value) return
  window.localStorage.setItem(ALERT_EMAIL_STORAGE_KEY, value)
}

function clearSavedAlertEmail() {
  if (typeof window === 'undefined') return
  window.localStorage.removeItem(ALERT_EMAIL_STORAGE_KEY)
}

function ingestSummaryMessage(result: IngestRunResponse) {
  return `Ingest 결과: 대상 ${result.totalLocations}, 수집 ${result.fetchedLocations}, 신규 ${result.insertedSnapshots}, 갱신 ${result.updatedSnapshots}, 변경없음 ${result.unchangedSnapshots}, 미수집 ${result.providerMisses}, alert ${result.alertsCreated}`
}

function subscriptionLabel(ruleType: RuleType, threshold: string) {
  switch (ruleType) {
    case 'TEMP_BELOW':
      return `기온 ${threshold}°C 아래`
    case 'TEMP_ABOVE':
      return `기온 ${threshold}°C 위`
    case 'PRECIP_ABOVE':
      return `강수 ${threshold}mm 위`
  }
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

  const [userEmail, setUserEmail] = useState(() => readSavedAlertEmail() ?? 'alerts@example.com')
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null)
  const [userHint, setUserHint] = useState<string | null>(null)
  const [creatingUser, setCreatingUser] = useState(false)
  const [autoReconnectChecked, setAutoReconnectChecked] = useState(false)

  const [subscriptionRuleType, setSubscriptionRuleType] = useState<RuleType>('TEMP_BELOW')
  const [subscriptionThreshold, setSubscriptionThreshold] = useState('20')
  const [lastSubscription, setLastSubscription] = useState<SubscriptionResponse | null>(null)
  const [subscriptionHint, setSubscriptionHint] = useState<string | null>(null)
  const [creatingSubscription, setCreatingSubscription] = useState(false)

  const [alerts, setAlerts] = useState<AlertEventResponse[]>([])
  const [alertsState, setAlertsState] = useState<AlertLoadState>('idle')
  const [alertsHint, setAlertsHint] = useState<string | null>(null)
  const [subscriptions, setSubscriptions] = useState<SubscriptionResponse[]>([])
  const [subscriptionsHint, setSubscriptionsHint] = useState<string | null>(null)
  const [subscriptionsLoading, setSubscriptionsLoading] = useState(false)
  const [updatingSubscriptionId, setUpdatingSubscriptionId] = useState<string | null>(null)

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

  async function refreshAlerts(userId: string) {
    setAlertsState('loading')
    setAlertsHint(null)
    try {
      const res = await listAlerts(userId)
      setAlerts(res)
      setAlertsState(res.length === 0 ? 'empty' : 'ready')
    } catch (error) {
      setAlerts([])
      setAlertsState('error')
      setAlertsHint(errorMessage(error))
    }
  }

  async function refreshSubscriptions(userId: string) {
    setSubscriptionsLoading(true)
    setSubscriptionsHint(null)
    try {
      const res = await listSubscriptions(userId)
      setSubscriptions(res)
    } catch (error) {
      setSubscriptions([])
      setSubscriptionsHint(errorMessage(error))
    } finally {
      setSubscriptionsLoading(false)
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
      const result = await runIngest()
      setSeedHint(ingestSummaryMessage(result))
      await refreshCurrentAll(targetLocations)
      if (selectedLocationId) {
        void refreshHourly(selectedLocationId)
      }
      if (currentUser) {
        void refreshAlerts(currentUser.id)
      }
    } catch (error) {
      setSeedHint(errorMessage(error))
    } finally {
      setIngesting(false)
    }
  }

  async function handleCreateUser(event: FormEvent) {
    event.preventDefault()
    setCreatingUser(true)
    setUserHint(null)
    try {
      const email = userEmail.trim()
      const user = await createUser({ email })
      setCurrentUser(user)
      saveAlertEmail(email)
      setUserHint(`알림 사용자 연결: ${user.email}`)
      void refreshAlerts(user.id)
      void refreshSubscriptions(user.id)
    } catch (error) {
      setUserHint(errorMessage(error))
    } finally {
      setCreatingUser(false)
    }
  }

  async function handleLoadUserByEmail() {
    setCreatingUser(true)
    setUserHint(null)
    try {
      const email = userEmail.trim()
      const user = await getUserByEmail(email)
      setCurrentUser(user)
      saveAlertEmail(email)
      setUserHint(`기존 사용자 연결: ${user.email}`)
      void refreshAlerts(user.id)
      void refreshSubscriptions(user.id)
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) {
        clearSavedAlertEmail()
      }
      setUserHint(errorMessage(error))
    } finally {
      setCreatingUser(false)
    }
  }

  async function handleCreateSubscription(event: FormEvent) {
    event.preventDefault()
    if (!currentUser || !selectedLocationId) {
      setSubscriptionHint('먼저 사용자와 도시를 선택해줘.')
      return
    }

    const threshold = Number(subscriptionThreshold)
    if (!Number.isFinite(threshold)) {
      setSubscriptionHint('threshold는 숫자로 입력해줘.')
      return
    }

    setCreatingSubscription(true)
    setSubscriptionHint(null)
    try {
      const payload: CreateSubscriptionRequest = {
        userId: currentUser.id,
        locationId: selectedLocationId,
        ruleType: subscriptionRuleType,
        threshold,
      }
      const subscription = await createSubscription(payload)
      setLastSubscription(subscription)
      setSubscriptionHint(
        `${subscription.enabled ? '구독 저장 완료' : '구독 상태 갱신 완료'}: ${subscriptionLabel(subscription.ruleType, String(subscription.threshold))}`,
      )
      void refreshAlerts(currentUser.id)
      void refreshSubscriptions(currentUser.id)
    } catch (error) {
      setSubscriptionHint(errorMessage(error))
    } finally {
      setCreatingSubscription(false)
    }
  }

  async function handleDisableSubscription(subscription: SubscriptionResponse) {
    setUpdatingSubscriptionId(subscription.id)
    setSubscriptionsHint(null)
    try {
      const updated = await disableSubscription(subscription.id)
      setSubscriptions((prev) => prev.map((item) => (item.id === updated.id ? updated : item)))
      if (lastSubscription?.id === updated.id) {
        setLastSubscription(updated)
      }
      setSubscriptionHint(`rule 비활성화 완료: ${subscriptionLabel(updated.ruleType, String(updated.threshold))}`)
    } catch (error) {
      setSubscriptionsHint(errorMessage(error))
    } finally {
      setUpdatingSubscriptionId(null)
    }
  }

  async function handleEnableSubscription(subscription: SubscriptionResponse) {
    setUpdatingSubscriptionId(subscription.id)
    setSubscriptionsHint(null)
    try {
      const updated = await enableSubscription(subscription.id)
      setSubscriptions((prev) => prev.map((item) => (item.id === updated.id ? updated : item)))
      if (lastSubscription?.id === updated.id) {
        setLastSubscription(updated)
      }
      setSubscriptionHint(`rule 재활성화 완료: ${subscriptionLabel(updated.ruleType, String(updated.threshold))}`)
    } catch (error) {
      setSubscriptionsHint(errorMessage(error))
    } finally {
      setUpdatingSubscriptionId(null)
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
    if (autoReconnectChecked) return
    const savedEmail = readSavedAlertEmail()
    if (!savedEmail) {
      setAutoReconnectChecked(true)
      return
    }

    setUserEmail(savedEmail)
    setCreatingUser(true)
    setUserHint('마지막 alert 사용자를 다시 연결하는 중…')

    void getUserByEmail(savedEmail)
      .then((user) => {
        setCurrentUser(user)
        saveAlertEmail(savedEmail)
        setUserHint(`마지막 alert 사용자를 다시 연결했어: ${user.email}`)
        void refreshAlerts(user.id)
        void refreshSubscriptions(user.id)
      })
      .catch((error: unknown) => {
        setCurrentUser(null)
        setAlerts([])
        setSubscriptions([])
        if (error instanceof ApiError && error.status === 404) {
          clearSavedAlertEmail()
          setUserHint('저장된 alert 사용자를 찾지 못해서 자동 재연결 정보를 비웠어.')
          return
        }
        setUserHint(`자동 재연결 실패: ${errorMessage(error)}`)
      })
      .finally(() => {
        setCreatingUser(false)
        setAutoReconnectChecked(true)
      })
  }, [autoReconnectChecked])

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

          <div className="card alertCard">
            <div className="cardTitle">Alert Flow</div>
            <div className="cardSub">사용자 생성 → 조건 등록 → Ingest 후 alert 이벤트 확인</div>

            <form className="form" onSubmit={(e) => void handleCreateUser(e)}>
              <div className="gridSingle">
                <label>
                  Alert Email
                  <input value={userEmail} onChange={(e) => setUserEmail(e.target.value)} placeholder="alerts@example.com" />
                </label>
              </div>
              <div className="row">
                <button type="submit" disabled={creatingUser}>
                  {creatingUser ? 'Creating…' : currentUser ? 'Create Another User' : 'Create Alert User'}
                </button>
                <button className="ghost" type="button" onClick={() => void handleLoadUserByEmail()} disabled={creatingUser}>
                  {creatingUser ? 'Loading…' : 'Load Existing'}
                </button>
                <div className="hint">{userHint}</div>
              </div>
            </form>

            <form className="form" onSubmit={(e) => void handleCreateSubscription(e)}>
              <div className="gridAlert">
                <label>
                  Target
                  <input value={selectedLocation?.name ?? '도시를 먼저 선택해줘'} disabled />
                </label>
                <label>
                  Rule
                  <select value={subscriptionRuleType} onChange={(e) => setSubscriptionRuleType(e.target.value as RuleType)}>
                    <option value="TEMP_BELOW">TEMP_BELOW</option>
                    <option value="TEMP_ABOVE">TEMP_ABOVE</option>
                    <option value="PRECIP_ABOVE">PRECIP_ABOVE</option>
                  </select>
                </label>
                <label>
                  Threshold
                  <input value={subscriptionThreshold} onChange={(e) => setSubscriptionThreshold(e.target.value)} placeholder="20" />
                </label>
              </div>
              <div className="row">
                <button type="submit" disabled={creatingSubscription || !currentUser || !selectedLocation}>
                  {creatingSubscription ? 'Saving…' : 'Create Alert Rule'}
                </button>
                <div className="hint">{subscriptionHint}</div>
              </div>
            </form>

            {currentUser && (
              <div className="muted small">
                Active user: {currentUser.email} · {currentUser.id}
              </div>
            )}

            {lastSubscription && (
              <div className="muted small">
                Last rule: {subscriptionLabel(lastSubscription.ruleType, String(lastSubscription.threshold))}
              </div>
            )}

            {currentUser && (
              <div className="subscriptionsPanel">
                <div className="row spread">
                  <div className="muted small">Current rules</div>
                  <button
                    className="ghost"
                    type="button"
                    onClick={() => void refreshSubscriptions(currentUser.id)}
                    disabled={subscriptionsLoading}
                  >
                    {subscriptionsLoading ? 'Loading…' : 'Reload Rules'}
                  </button>
                </div>
                {subscriptionsHint && <div className="notice">Subscription error: {subscriptionsHint}</div>}
                {subscriptions.length === 0 && !subscriptionsHint && (
                  <div className="muted small">아직 저장된 rule이 없어. 위에서 rule을 추가해봐.</div>
                )}
                {subscriptions.length > 0 && (
                  <div className="alertList">
                    {subscriptions.slice(0, 8).map((subscription) => (
                      <div key={subscription.id} className="alertItem">
                        <div className="row spread">
                          <span className={`badge ${subscription.enabled ? 'ok' : 'warn'}`}>
                            {subscription.enabled ? 'enabled' : 'disabled'}
                          </span>
                          <span className="muted tiny">{formatKst(subscription.createdAt)}</span>
                        </div>
                        <div className="alertMessage">
                          {subscriptionLabel(subscription.ruleType, String(subscription.threshold))}
                        </div>
                        <div className="row actionRow">
                          {subscription.enabled && (
                            <button
                              className="ghost danger"
                              type="button"
                              onClick={() => void handleDisableSubscription(subscription)}
                              disabled={updatingSubscriptionId === subscription.id}
                            >
                              {updatingSubscriptionId === subscription.id ? 'Updating…' : 'Disable Rule'}
                            </button>
                          )}
                          {!subscription.enabled && (
                            <button
                              className="ghost success"
                              type="button"
                              onClick={() => void handleEnableSubscription(subscription)}
                              disabled={updatingSubscriptionId === subscription.id}
                            >
                              {updatingSubscriptionId === subscription.id ? 'Updating…' : 'Enable Rule'}
                            </button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            <div className="panelHeader alertHeader">
              <h2>Alerts</h2>
              <div className="row">
                <button
                  className="ghost"
                  onClick={() => currentUser && void refreshAlerts(currentUser.id)}
                  disabled={!currentUser || alertsState === 'loading'}
                >
                  {alertsState === 'loading' ? 'Loading…' : 'Reload Alerts'}
                </button>
              </div>
            </div>

            {!currentUser && <div className="muted small">먼저 alert 사용자를 만들어야 알림 이벤트를 볼 수 있어.</div>}
            {currentUser && alertsState === 'empty' && <div className="muted small">아직 alert 이벤트가 없어. 규칙을 만든 뒤 Ingest Now를 실행해봐.</div>}
            {currentUser && alertsState === 'error' && alertsHint && <div className="notice">Alert error: {alertsHint}</div>}

            {alerts.length > 0 && (
              <div className="alertList">
                {alerts.slice(0, 8).map((alert) => (
                  <div key={alert.id} className="alertItem">
                    <div className="row spread">
                      <span className={`badge ${alert.status === 'SENT' ? 'ok' : 'warn'}`}>{alert.status.toLowerCase()}</span>
                      <span className="muted tiny">{formatKst(alert.createdAt)}</span>
                    </div>
                    <div className="alertMessage">{alert.message}</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>
      </main>
    </div>
  )
}

export default App
