import type { ReactNode } from 'react'

type WeatherKind = 'clear' | 'partly' | 'cloudy' | 'fog' | 'drizzle' | 'rain' | 'snow' | 'thunder' | 'unknown'

export type WeatherVisual = {
  kind: WeatherKind
  label: string
}

export function describeWeather(code: number | null | undefined): WeatherVisual {
  if (code == null) return { kind: 'unknown', label: '정보 없음' }
  if (code === 0) return { kind: 'clear', label: '맑음' }
  if (code === 1) return { kind: 'clear', label: '대체로 맑음' }
  if (code === 2) return { kind: 'partly', label: '구름 조금' }
  if (code === 3) return { kind: 'cloudy', label: '흐림' }
  if (code === 45 || code === 48) return { kind: 'fog', label: '안개' }
  if (code >= 51 && code <= 57) return { kind: 'drizzle', label: '이슬비' }
  if (code >= 61 && code <= 67) return { kind: 'rain', label: '비' }
  if (code >= 71 && code <= 77) return { kind: 'snow', label: '눈' }
  if (code >= 80 && code <= 82) return { kind: 'rain', label: '소나기' }
  if (code >= 85 && code <= 86) return { kind: 'snow', label: '눈 소나기' }
  if (code >= 95 && code <= 99) return { kind: 'thunder', label: '뇌우' }
  return { kind: 'unknown', label: `code ${code}` }
}

type IconProps = {
  kind: WeatherKind
  size?: number
}

export function WeatherIcon({ kind, size = 28 }: IconProps) {
  return (
    <span className={`wxIcon kind-${kind}`} style={{ width: size, height: size }}>
      {iconFor(kind)}
    </span>
  )
}

function iconFor(kind: WeatherKind): ReactNode {
  switch (kind) {
    case 'clear':
      return <Sun />
    case 'partly':
      return <PartlyCloudy />
    case 'cloudy':
      return <Cloud />
    case 'fog':
      return <Fog />
    case 'drizzle':
      return <Drizzle />
    case 'rain':
      return <Rain />
    case 'snow':
      return <Snow />
    case 'thunder':
      return <Thunder />
    default:
      return <Unknown />
  }
}

function Svg({ children }: { children: ReactNode }) {
  return (
    <svg viewBox="0 0 64 64" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
      {children}
    </svg>
  )
}

function Sun() {
  return (
    <Svg>
      <circle cx="32" cy="32" r="10" />
      <path d="M32 8v6M32 50v6M8 32h6M50 32h6M14 14l4 4M46 46l4 4M50 14l-4 4M18 46l-4 4" />
    </Svg>
  )
}

function Cloud() {
  return (
    <Svg>
      <path d="M20 42h26a10 10 0 0 0 0-20 14 14 0 0 0-27-2A9 9 0 0 0 20 42Z" />
    </Svg>
  )
}

function PartlyCloudy() {
  return (
    <Svg>
      <path d="M22 18a9 9 0 0 1 15-6" />
      <path d="M18 42h28a9 9 0 0 0 0-18 13 13 0 0 0-25-2A9 9 0 0 0 18 42Z" />
      <path d="M18 18l-3-3M14 24h-4M18 30l-3 3" />
    </Svg>
  )
}

function Fog() {
  return (
    <Svg>
      <path d="M18 30h28" />
      <path d="M14 38h36" />
      <path d="M18 46h28" />
      <path d="M20 28a12 12 0 0 1 23-4 9 9 0 0 1 3 17" />
    </Svg>
  )
}

function Rain() {
  return (
    <Svg>
      <path d="M18 34h28a9 9 0 0 0 0-18 13 13 0 0 0-25-2A9 9 0 0 0 18 34Z" />
      <path d="M22 42l-2 6" />
      <path d="M32 42l-2 6" />
      <path d="M42 42l-2 6" />
    </Svg>
  )
}

function Drizzle() {
  return (
    <Svg>
      <path d="M18 34h28a9 9 0 0 0 0-18 13 13 0 0 0-25-2A9 9 0 0 0 18 34Z" />
      <path d="M24 42v4" />
      <path d="M34 42v4" />
      <path d="M44 42v4" />
    </Svg>
  )
}

function Snow() {
  return (
    <Svg>
      <path d="M18 34h28a9 9 0 0 0 0-18 13 13 0 0 0-25-2A9 9 0 0 0 18 34Z" />
      <path d="M24 42v10M19 47h10M21 44l6 6M27 44l-6 6" />
      <path d="M40 42v10M35 47h10M37 44l6 6M43 44l-6 6" />
    </Svg>
  )
}

function Thunder() {
  return (
    <Svg>
      <path d="M18 32h28a9 9 0 0 0 0-18 13 13 0 0 0-25-2A9 9 0 0 0 18 32Z" />
      <path d="M30 36l-6 10h6l-2 10 12-16h-6l2-4z" />
    </Svg>
  )
}

function Unknown() {
  return (
    <Svg>
      <circle cx="32" cy="32" r="20" />
      <path d="M26 26a6 6 0 0 1 12 0c0 4-6 4-6 10" />
      <circle cx="32" cy="46" r="1.6" fill="currentColor" stroke="none" />
    </Svg>
  )
}
