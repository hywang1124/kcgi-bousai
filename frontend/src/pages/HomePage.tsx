import { useTranslation } from 'react-i18next'
import type { View } from '../App'

interface Props {
  onNavigate: (view: View) => void
}

/** トップページ。ヒーロー・日本の災害紹介・機能カードを表示する。 */
export function HomePage({ onNavigate }: Props) {
  const { t } = useTranslation()

  const features: { view: View; icon: string; title: string; desc: string }[] = [
    { view: 'shelters', icon: '🗺️', title: t('home.featureMapTitle'), desc: t('home.featureMapDesc') },
    { view: 'knowledge', icon: '📖', title: t('home.featureKnowledgeTitle'), desc: t('home.featureKnowledgeDesc') },
    { view: 'chat', icon: '💬', title: t('home.featureAiTitle'), desc: t('home.featureAiDesc') },
  ]

  return (
    <div className="home">
      <section className="hero">
        <h2 className="hero-title">{t('home.heroTitle')}</h2>
        <p className="hero-subtitle">{t('home.heroSubtitle')}</p>
        <button type="button" className="hero-cta" onClick={() => onNavigate('shelters')}>
          {t('home.heroCta')}
        </button>
      </section>

      <section className="home-section">
        <h3>{t('home.situationTitle')}</h3>
        <p className="home-lead">{t('home.situationBody')}</p>
      </section>

      <section className="home-section">
        <h3>{t('home.featuresTitle')}</h3>
        <div className="feature-grid">
          {features.map((f) => (
            <button key={f.view} type="button" className="feature-card" onClick={() => onNavigate(f.view)}>
              <span className="feature-icon" aria-hidden="true">
                {f.icon}
              </span>
              <span className="feature-title">{f.title}</span>
              <span className="feature-desc">{f.desc}</span>
            </button>
          ))}
        </div>
      </section>
    </div>
  )
}
