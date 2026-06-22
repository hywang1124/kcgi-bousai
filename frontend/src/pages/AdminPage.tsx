import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { fetchAdminMe, login as apiLogin, UNAUTHORIZED } from '../api/auth'
import type { AdminMe } from '../api/types'
import { clearToken, getToken, setToken } from '../auth/token'

/** 管理者ログインページ。未ログインならフォーム、ログイン済みなら管理者情報を表示する。 */
export function AdminPage() {
  const { t } = useTranslation()
  const [token, setTok] = useState<string | null>(() => getToken())
  const [me, setMe] = useState<AdminMe | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  // token があれば管理者情報を取得（無効なら破棄してログイン画面に戻す）
  useEffect(() => {
    if (!token) return
    let active = true
    fetchAdminMe(token)
      .then((data) => {
        if (active) setMe(data)
      })
      .catch(() => {
        if (active) {
          clearToken()
          setTok(null)
          setMe(null)
        }
      })
    return () => {
      active = false
    }
  }, [token])

  async function handleLogin(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const res = await apiLogin(username, password)
      setToken(res.token)
      setTok(res.token)
      setPassword('')
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err)
      setError(message === UNAUTHORIZED ? t('admin.invalidCredentials') : t('admin.loginError'))
    } finally {
      setLoading(false)
    }
  }

  function handleLogout() {
    clearToken()
    setTok(null)
    setMe(null)
  }

  if (token && me) {
    return (
      <section>
        <h2>{t('admin.heading')}</h2>
        <p>
          {t('admin.loggedInAs')}: <strong>{me.username}</strong> ({me.roles.join(', ')})
        </p>
        <button type="button" onClick={handleLogout}>
          {t('admin.logout')}
        </button>
      </section>
    )
  }

  return (
    <section>
      <h2>{t('admin.loginHeading')}</h2>
      <form onSubmit={handleLogin} className="login-form">
        <input
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder={t('admin.username')}
          autoComplete="username"
        />
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder={t('admin.password')}
          autoComplete="current-password"
        />
        <button type="submit" disabled={loading || !username || !password}>
          {loading ? t('admin.loggingIn') : t('admin.login')}
        </button>
      </form>
      {error && <p role="alert">{error}</p>}
    </section>
  )
}
