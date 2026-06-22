import type { Shelter } from './types'

/** 避難所の作成・更新ペイロード。 */
export interface ShelterInput {
  nameJa: string
  nameEn: string | null
  nameZh: string | null
  address: string | null
  lat: number
  lng: number
  capacity: number | null
  facilities: string[]
}

function authHeaders(token: string): HeadersInit {
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }
}

/**
 * 避難所一覧を取得する（公開）。
 * 開発時は Vite プロキシ経由で Spring Boot (/api/v1/shelters) に到達する。
 */
export async function fetchShelters(): Promise<Shelter[]> {
  const res = await fetch('/api/v1/shelters')
  if (!res.ok) {
    throw new Error(`避難所の取得に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as Shelter[]
}

/** 避難所を新規作成する（ADMIN）。 */
export async function createShelter(token: string, input: ShelterInput): Promise<Shelter> {
  const res = await fetch('/api/v1/shelters', {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(input),
  })
  if (!res.ok) {
    throw new Error(`避難所の作成に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as Shelter
}

/** 避難所を更新する（ADMIN）。 */
export async function updateShelter(token: string, id: number, input: ShelterInput): Promise<Shelter> {
  const res = await fetch(`/api/v1/shelters/${id}`, {
    method: 'PUT',
    headers: authHeaders(token),
    body: JSON.stringify(input),
  })
  if (!res.ok) {
    throw new Error(`避難所の更新に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as Shelter
}

/** 避難所を削除する（ADMIN）。 */
export async function deleteShelter(token: string, id: number): Promise<void> {
  const res = await fetch(`/api/v1/shelters/${id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!res.ok) {
    throw new Error(`避難所の削除に失敗しました (HTTP ${res.status})`)
  }
}
