import { useTranslation } from 'react-i18next'

const DISASTERS: { key: string; icon: string }[] = [
  { key: 'earthquake', icon: '🏚️' },
  { key: 'tsunami', icon: '🌊' },
  { key: 'volcano', icon: '🌋' },
  { key: 'typhoon', icon: '🌀' },
  { key: 'flood', icon: '💧' },
]

/** 防災知識ページ。日本で起こりやすい災害の紹介と備え方。 */
export function KnowledgePage() {
  const { t } = useTranslation()

  return (
    <section>
      <h2>{t('knowledge.heading')}</h2>
      <p className="home-lead">{t('knowledge.lead')}</p>

      <div className="knowledge-list">
        {DISASTERS.map(({ key, icon }) => {
          const tips = t(`knowledge.${key}.tips`, { returnObjects: true }) as unknown as string[]
          return (
            <article key={key} className="knowledge-card">
              <h3 className="knowledge-card-title">
                <span className="knowledge-icon" aria-hidden="true">
                  {icon}
                </span>
                {t(`knowledge.${key}.name`)}
              </h3>
              <p>{t(`knowledge.${key}.summary`)}</p>
              <p className="knowledge-tips-title">{t('knowledge.tipsTitle')}</p>
              <ul className="knowledge-tips">
                {tips.map((tip, i) => (
                  <li key={i}>{tip}</li>
                ))}
              </ul>
            </article>
          )
        })}
      </div>
    </section>
  )
}
