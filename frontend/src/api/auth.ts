import type { AdminMe, LoginResponse, UserAccount } from './types'

/** 認証エラー（資格情報の誤り / 未認証）。 */
export const UNAUTHORIZED = 'UNAUTHORIZED'
/** 権限不足（認証済みだが ADMIN ではない）。 */
export const FORBIDDEN = 'FORBIDDEN'
/** ユーザ名重複。 */
export const CONFLICT = 'CONFLICT'

/** セルフ登録（公開・既定ロール USER）。 */
export async function register(username: string, password: string): Promise<UserAccount> {
  const res = await fetch('/api/v1/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  })
  if (res.status === 409) {
    throw new Error(CONFLICT)
  }
  if (!res.ok) {
    throw new Error(`登録に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as UserAccount
}

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

/** JWT を使って認証中の管理者情報を取得する。401=無効トークン / 403=非管理者。 */
export async function fetchAdminMe(token: string): Promise<AdminMe> {
  const res = await fetch('/api/v1/admin/me', {
    headers: { Authorization: `Bearer ${token}` },
  })
  if (res.status === 401) {
    throw new Error(UNAUTHORIZED)
  }
  if (res.status === 403) {
    throw new Error(FORBIDDEN)
  }
  if (!res.ok) {
    throw new Error(`管理者情報の取得に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as AdminMe
}
