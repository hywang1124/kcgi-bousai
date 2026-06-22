import type { HazardZone } from './types'

/** 危険区域一覧を取得する（公開）。 */
export async function fetchHazardZones(): Promise<HazardZone[]> {
  const res = await fetch('/api/v1/hazard-zones')
  if (!res.ok) {
    throw new Error(`危険区域の取得に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as HazardZone[]
}
