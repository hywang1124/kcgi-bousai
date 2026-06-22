import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { fetchShelters } from '../api/shelters'
import type { Shelter } from '../api/types'

/** 現在の言語に応じた避難所名を返す（無ければ日本語名にフォールバック）。 */
function localizedName(shelter: Shelter, lang: string): string {
  if (lang.startsWith('en') && shelter.nameEn) return shelter.nameEn
  if (lang.startsWith('zh') && shelter.nameZh) return shelter.nameZh
  return shelter.nameJa
}

/** 避難所一覧ページ。バックエンドから取得して表示する。 */
export function SheltersPage() {
  const { t, i18n } = useTranslation()
  const [shelters, setShelters] = useState<Shelter[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    fetchShelters()
      .then((data) => {
        if (active) setShelters(data)
      })
      .catch((e: unknown) => {
        if (active) setError(e instanceof Error ? e.message : String(e))
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [])

  if (loading) return <p>{t('shelters.loading')}</p>
  if (error) {
    return (
      <p role="alert">
        {t('shelters.error')}: {error}
      </p>
    )
  }
  if (shelters.length === 0) return <p>{t('shelters.empty')}</p>

  return (
    <section>
      <h2>{t('shelters.heading')}</h2>
      <ul className="shelter-list">
        {shelters.map((s) => (
          <li key={s.id} className="shelter-card">
            <h3>{localizedName(s, i18n.language)}</h3>
            {s.address && (
              <p>
                {t('shelters.address')}: {s.address}
              </p>
            )}
            {s.capacity != null && (
              <p>
                {t('shelters.capacity')}: {s.capacity} {t('shelters.people')}
              </p>
            )}
            {s.facilities.length > 0 && (
              <p>
                {t('shelters.facilities')}: {s.facilities.join(' / ')}
              </p>
            )}
          </li>
        ))}
      </ul>
    </section>
  )
}
