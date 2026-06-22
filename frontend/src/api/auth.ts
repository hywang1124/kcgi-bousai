import type { AdminMe, LoginResponse } from './types'

/** 認証エラー（資格情報の誤り）を表す番兵メッセージ。 */
export const UNAUTHORIZED = 'UNAUTHORIZED'

/** ログインして JWT を取得する。 */
export async function login(username: string, password: string): Promise<LoginResponse> {
  const res = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  })
  if (res.status === 401) {
    throw new Error(UNAUTHORIZED)
  }
  if (!res.ok) {
    throw new Error(`ログインに失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as LoginResponse
}

/** JWT を使って認証中の管理者情報を取得する。 */
export async function fetchAdminMe(token: string): Promise<AdminMe> {
  const res = await fetch('/api/v1/admin/me', {
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!res.ok) {
    throw new Error(`管理者情報の取得に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as AdminMe
}
