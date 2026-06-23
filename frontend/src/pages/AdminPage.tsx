import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import {
  CONFLICT,
  FORBIDDEN,
  fetchAdminMe,
  login as apiLogin,
  register as apiRegister,
  UNAUTHORIZED,
} from '../api/auth'
import type { AdminMe } from '../api/types'
import { clearToken, getToken, setToken } from '../auth/token'
import { UserAdmin } from '../components/UserAdmin'

type AuthMode = 'login' | 'register'

export function AdminPage() {
  const { t } = useTranslation()
  const [token, setTok] = useState<string | null>(() => getToken())
  const [me, setMe] = useState<AdminMe | null>(null)
  const [accessDenied, setAccessDenied] = useState(false)
  const [authMode, setAuthMode] = useState<AuthMode>('login')

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [info, setInfo] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  // token があれば管理者情報を取得。403=非管理者（トークンは保持）、401=破棄。
  useEffect(() => {
    if (!token) return
    let active = true
    fetchAdminMe(token)
      .then((data) => {
        if (!active) return
        setMe(data)
        setAccessDenied(false)
      })
      .catch((e: unknown) => {
        if (!active) return
        const msg = e instanceof Error ? e.message : ''
        if (msg === FORBIDDEN) {
          setAccessDenied(true)
          setMe(null)
        } else {
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
    setInfo(null)
    try {
      const res = await apiLogin(username, password)
      setToken(res.token)
      setTok(res.token)
      setPassword('')
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err)
      setError(msg === UNAUTHORIZED ? t('admin.invalidCredentials') : t('admin.loginError'))
    } finally {
      setLoading(false)
    }
  }

  async function handleRegister(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    setInfo(null)
    try {
      await apiRegister(username, password)
      setInfo(t('register.success'))
      setAuthMode('login')
      setPassword('')
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err)
      setError(msg === CONFLICT ? t('register.duplicate') : t('register.error'))
    } finally {
      setLoading(false)
    }
  }

  function handleLogout() {
    clearToken()
    setTok(null)
    setMe(null)
    setAccessDenied(false)
  }

  // --- 管理者としてログイン済み ---
  if (token && me) {
    return (
      <section>
        <h2>{t('admin.console')}</h2>
        <p>
          {t('admin.loggedInAs')}: <strong>{me.username}</strong> ({me.roles.join(', ')}){' '}
          <button type="button" onClick={handleLogout}>
            {t('admin.logout')}
          </button>
        </p>
        <UserAdmin token={token} />
      </section>
    )
  }

  // --- ログイン済みだが管理者権限なし ---
  if (token && accessDenied) {
    return (
      <section>
        <h2>{t('admin.console')}</h2>
        <p role="alert">{t('admin.noAccess')}</p>
        <button type="button" onClick={handleLogout}>
          {t('admin.logout')}
        </button>
      </section>
    )
  }

  // --- 未ログイン: ログイン / 登録フォーム ---
  const isRegister = authMode === 'register'
  return (
    <section>
      <h2>{isRegister ? t('register.title') : t('admin.loginHeading')}</h2>
      <form onSubmit={isRegister ? handleRegister : handleLogin} className="login-form">
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
          autoComplete={isRegister ? 'new-password' : 'current-password'}
        />
        <button type="submit" disabled={loading || !username || !password}>
          {isRegister ? t('register.submit') : loading ? t('admin.loggingIn') : t('admin.login')}
        </button>
      </form>

      <button
        type="button"
        className="link-button"
        onClick={() => {
          setAuthMode(isRegister ? 'login' : 'register')
          setError(null)
          setInfo(null)
        }}
      >
        {isRegister ? t('register.toLogin') : t('register.toRegister')}
      </button>

      {info && <p className="info">{info}</p>}
      {error && <p role="alert">{error}</p>}
    </section>
  )
}
