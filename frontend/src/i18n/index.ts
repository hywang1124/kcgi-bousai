import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import ja from '../locales/ja.json'
import en from '../locales/en.json'
import zh from '../locales/zh.json'
import zhTW from '../locales/zh-TW.json'

export const supportedLanguages = ['ja', 'en', 'zh', 'zh-TW'] as const
export type SupportedLanguage = (typeof supportedLanguages)[number]

void i18n.use(initReactI18next).init({
  resources: {
    ja: { translation: ja },
    en: { translation: en },
    zh: { translation: zh },
    'zh-TW': { translation: zhTW },
  },
  lng: 'ja',
  fallbackLng: 'ja',
  interpolation: {
    escapeValue: false, // React は既に XSS 対策を行う
  },
})

export default i18n
