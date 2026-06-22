import { useTranslation } from 'react-i18next'
import { supportedLanguages } from '../i18n'

/** 言語切替ボタン群（ja / en / zh）。 */
export function LanguageSwitcher() {
  const { i18n, t } = useTranslation()

  return (
    <div className="language-switcher">
      <span>{t('language.label')}:</span>
      {supportedLanguages.map((lng) => (
        <button
          key={lng}
          type="button"
          onClick={() => void i18n.changeLanguage(lng)}
          disabled={i18n.language === lng}
        >
          {t(`language.${lng}`)}
        </button>
      ))}
    </div>
  )
}
