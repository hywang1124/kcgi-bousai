import type { Shelter } from './types'

/**
 * 避難所一覧を取得する。
 * 開発時は Vite プロキシ経由で Spring Boot (/api/v1/shelters) に到達する。
 */
export async function fetchShelters(): Promise<Shelter[]> {
  const res = await fetch('/api/v1/shelters')
  if (!res.ok) {
    throw new Error(`避難所の取得に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as Shelter[]
}
