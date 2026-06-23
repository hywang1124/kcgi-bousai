import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { LanguageSwitcher } from './components/LanguageSwitcher'
import { SheltersMapPage } from './pages/SheltersMapPage'
import { ChatPage } from './pages/ChatPage'
import { AdminPage } from './pages/AdminPage'
import './App.css'

type View = 'shelters' | 'chat' | 'admin'

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
        <button
          type="button"
          onClick={() => setView('admin')}
          disabled={view === 'admin'}
        >
          {t('nav.admin')}
        </button>
      </nav>

      <main>
        {view === 'shelters' && <SheltersMapPage />}
        {view === 'chat' && <ChatPage />}
        {view === 'admin' && <AdminPage />}
      </main>
    </div>
  )
}

export default App
