import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { LanguageSwitcher } from './components/LanguageSwitcher'
import { SheltersPage } from './pages/SheltersPage'
import { ChatPage } from './pages/ChatPage'
import './App.css'

type View = 'shelters' | 'chat'

function App() {
  const { t } = useTranslation()
  const [view, setView] = useState<View>('shelters')

  return (
    <div className="app">
      <header className="app-header">
        <div>
          <h1>{t('app.title')}</h1>
          <p className="subtitle">{t('app.subtitle')}</p>
        </div>
        <LanguageSwitcher />
      </header>

      <nav className="app-nav">
        <button
          type="button"
          onClick={() => setView('shelters')}
          disabled={view === 'shelters'}
        >
          {t('nav.shelters')}
        </button>
        <button
          type="button"
          onClick={() => setView('chat')}
          disabled={view === 'chat'}
        >
          {t('nav.chat')}
        </button>
      </nav>

      <main>{view === 'shelters' ? <SheltersPage /> : <ChatPage />}</main>
    </div>
  )
}

export default App
