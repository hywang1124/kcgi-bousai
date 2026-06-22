import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { fetchUsers, updateUserRole } from '../api/users'
import type { Role, UserAccount } from '../api/types'

interface Props {
  token: string
}

const ROLES: Role[] = ['ADMIN', 'EDITOR', 'USER']

/** ユーザ一覧と役割変更（ADMIN）。 */
export function UserAdmin({ token }: Props) {
  const { t } = useTranslation()
  const [users, setUsers] = useState<UserAccount[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchUsers(token)
      .then(setUsers)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : String(e)))
  }, [token])

  async function handleRoleChange(id: number, role: Role) {
    setError(null)
    try {
      const updated = await updateUserRole(token, id, role)
      setUsers((prev) => prev.map((u) => (u.id === id ? updated : u)))
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : String(err))
    }
  }

  return (
    <div>
      <h3>{t('admin.userAdmin.title')}</h3>
      {error && <p role="alert">{error}</p>}
      <table className="admin-table">
        <thead>
          <tr>
            <th>#</th>
            <th>{t('admin.userAdmin.username')}</th>
            <th>{t('admin.userAdmin.role')}</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.id}</td>
              <td>{u.username}</td>
              <td>
                <select
                  value={u.role}
                  onChange={(e) => handleRoleChange(u.id, e.target.value as Role)}
                >
                  {ROLES.map((r) => (
                    <option key={r} value={r}>
                      {r}
                    </option>
                  ))}
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
