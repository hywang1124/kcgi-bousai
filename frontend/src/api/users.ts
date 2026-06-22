import type { Role, UserAccount } from './types'

/** ユーザ一覧を取得する（ADMIN）。 */
export async function fetchUsers(token: string): Promise<UserAccount[]> {
  const res = await fetch('/api/v1/admin/users', {
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!res.ok) {
    throw new Error(`ユーザ一覧の取得に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as UserAccount[]
}

/** ユーザの役割を変更する（ADMIN）。 */
export async function updateUserRole(token: string, id: number, role: Role): Promise<UserAccount> {
  const res = await fetch(`/api/v1/admin/users/${id}/role`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify({ role }),
  })
  if (!res.ok) {
    throw new Error(`役割の変更に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as UserAccount
}
