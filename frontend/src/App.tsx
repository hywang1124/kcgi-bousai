import { useTranslation } from 'react-i18next'
import { LanguageSwitcher } from './components/LanguageSwitcher'
import { SheltersPage } from './pages/SheltersPage'
import './App.css'

function App() {
  const { t } = useTranslation()

  return (
    <div className="app">
      <header className="app-header">
        <div>
          <h1>{t('app.title')}</h1>
          <p className="subtitle">{t('app.subtitle')}</p>
        </div>
        <LanguageSwitcher />
      </header>
      <main>
        <SheltersPage />
      </main>
    </div>
  )
}

export default App
