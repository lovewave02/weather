import type { HourlyWeatherPoint } from './api'

type Props = {
  points: HourlyWeatherPoint[]
  timeZone?: string
}

function formatHour(isoTime: string, timeZone: string) {
  try {
    const date = new Date(isoTime)
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false, timeZone })
  } catch {
    return isoTime
  }
}

function clamp(value: number, min: number, max: number) {
  return Math.max(min, Math.min(max, value))
}

function linePath(points: { x: number; y: number; ok: boolean }[]) {
  let d = ''
  let started = false
  for (const p of points) {
    if (!p.ok) {
      started = false
      continue
    }
    d += `${started ? 'L' : 'M'}${p.x.toFixed(1)} ${p.y.toFixed(1)} `
    started = true
  }
  return d.trim()
}

export function HourlyTempChart({ points, timeZone = 'Asia/Seoul' }: Props) {
  if (!points || points.length < 2) {
    return <div className="muted small">그래프를 표시할 데이터가 부족해.</div>
  }

  const width = 760
  const height = 240
  const margin = { top: 18, right: 16, bottom: 34, left: 44 }
  const innerW = width - margin.left - margin.right
  const innerH = height - margin.top - margin.bottom

  const temps = points.map((p) => p.temperatureC).filter((v): v is number => typeof v === 'number' && Number.isFinite(v))
  const feels = points
    .map((p) => p.apparentTemperatureC)
    .filter((v): v is number => typeof v === 'number' && Number.isFinite(v))
  const all = temps.length || feels.length ? [...temps, ...feels] : [0]

  const min = Math.min(...all)
  const max = Math.max(...all)
  const pad = Math.max(1.5, (max - min) * 0.1)
  const yMin = min - pad
  const yMax = max + pad

  const xAt = (i: number) => margin.left + (i / (points.length - 1)) * innerW
  const yAt = (v: number) => margin.top + (1 - (v - yMin) / (yMax - yMin || 1)) * innerH

  const tempPts = points.map((p, i) => {
    const v = p.temperatureC
    const ok = typeof v === 'number' && Number.isFinite(v)
    return { x: xAt(i), y: yAt(ok ? v : 0), ok }
  })

  const feelPts = points.map((p, i) => {
    const v = p.apparentTemperatureC
    const ok = typeof v === 'number' && Number.isFinite(v)
    return { x: xAt(i), y: yAt(ok ? v : 0), ok }
  })

  const gridLines = 4
  const yTicks = Array.from({ length: gridLines + 1 }, (_, idx) => yMin + ((yMax - yMin) * idx) / gridLines)
  const xTicks = Array.from(new Set([0, 6, 12, 18, 24].map((h) => clamp(h, 0, points.length - 1))))

  return (
    <div className="chart">
      <svg viewBox={`0 0 ${width} ${height}`} role="img" aria-label="Hourly temperature chart">
        <defs>
          <linearGradient id="tempLine" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stopColor="rgba(100,108,255,0.85)" />
            <stop offset="100%" stopColor="rgba(46,204,113,0.75)" />
          </linearGradient>
        </defs>

        {yTicks.map((t) => {
          const y = yAt(t)
          return (
            <g key={t}>
              <line x1={margin.left} x2={width - margin.right} y1={y} y2={y} stroke="rgba(255,255,255,0.08)" />
              <text x={margin.left - 10} y={y + 4} textAnchor="end" fontSize="10" fill="rgba(255,255,255,0.55)">
                {t.toFixed(0)}
              </text>
            </g>
          )
        })}

        <path d={linePath(tempPts)} fill="none" stroke="url(#tempLine)" strokeWidth="3.2" />
        <path
          d={linePath(feelPts)}
          fill="none"
          stroke="rgba(245,215,110,0.9)"
          strokeWidth="2.4"
          strokeDasharray="6 6"
        />

        {xTicks.map((idx) => {
          const p = points[idx]
          if (!p) return null
          const x = xAt(idx)
          return (
            <g key={`${idx}-${p.time}`}>
              <line x1={x} x2={x} y1={margin.top} y2={height - margin.bottom} stroke="rgba(255,255,255,0.06)" />
              <text x={x} y={height - 12} textAnchor="middle" fontSize="10" fill="rgba(255,255,255,0.6)">
                {formatHour(p.time, timeZone)}
              </text>
            </g>
          )
        })}
      </svg>

      <div className="legend">
        <span className="legendItem">
          <span className="swatch swatch-temp" /> 기온
        </span>
        <span className="legendItem">
          <span className="swatch swatch-feel" /> 체감
        </span>
      </div>
    </div>
  )
}
