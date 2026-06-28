import { useTranslation } from 'react-i18next'

const KANTEI_BOUSAI_IMAGE_BASE = 'https://www.kantei.go.jp'

const DISASTERS: { key: string; icon: string; image: string }[] = [
  { key: 'preparedness', icon: '🎒', image: `${KANTEI_BOUSAI_IMAGE_BASE}/jp/n5-common/img/bousai_kv_sonae.png` },
  { key: 'evacuation', icon: '🚶', image: `${KANTEI_BOUSAI_IMAGE_BASE}/jp/n5-common/img/bousai_kv_hinan.png` },
  { key: 'earthquake', icon: '🏚️', image: `${KANTEI_BOUSAI_IMAGE_BASE}/jp/n5-common/img/bousai_kv_jishin.png` },
  { key: 'tsunami', icon: '🌊', image: `${KANTEI_BOUSAI_IMAGE_BASE}/jp/n5-common/img/bousai_kv_tsunami.png` },
  { key: 'heavyRain', icon: '☔', image: `${KANTEI_BOUSAI_IMAGE_BASE}/jp/n5-common/img/bousai_kv_taifu.png` },
  { key: 'landslide', icon: '⛰️', image: `${KANTEI_BOUSAI_IMAGE_BASE}/jp/n5-common/img/bousai_kv_dosha.png` },
  { key: 'volcano', icon: '🌋', image: `${KANTEI_BOUSAI_IMAGE_BASE}/jp/n5-common/img/bousai_kv_kazan.png` },
  { key: 'multilingual', icon: '🌐', image: `${KANTEI_BOUSAI_IMAGE_BASE}/jp/n5-common/img/bousai_kv_keihou.png` },
]

/** 防災知識ページ。日本で起こりやすい災害の紹介と備え方。 */
export function KnowledgePage() {
  const { t } = useTranslation()

  return (
    <section>
      <h2>{t('knowledge.heading')}</h2>
      <p className="home-lead">{t('knowledge.lead')}</p>

      <div className="knowledge-list">
        {DISASTERS.map(({ key, icon, image }) => {
          const tips = t(`knowledge.${key}.tips`, { returnObjects: true }) as unknown as string[]
          const actions = t(`knowledge.${key}.actions`, { returnObjects: true }) as unknown as string[]
          return (
            <article key={key} className="knowledge-card">
              <img
                className="knowledge-card-image"
                src={image}
                alt=""
                aria-hidden="true"
                loading="lazy"
              />
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
              <p className="knowledge-tips-title">{t('knowledge.actionsTitle')}</p>
              <ul className="knowledge-tips knowledge-actions">
                {actions.map((action, i) => (
                  <li key={i}>{action}</li>
                ))}
              </ul>
            </article>
          )
        })}
      </div>
    </section>
  )
}
