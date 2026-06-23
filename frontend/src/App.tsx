import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { LanguageSwitcher } from './components/LanguageSwitcher'
import { HomePage } from './pages/HomePage'
import { KnowledgePage } from './pages/KnowledgePage'
import { SheltersMapPage } from './pages/SheltersMapPage'
import { ChatPage } from './pages/ChatPage'
import { AdminPage } from './pages/AdminPage'
import './App.css'

export type View = 'home' | 'knowledge' | 'shelters' | 'chat' | 'admin'

const NAV_ITEMS: { view: View; key: string }[] = [
  { view: 'home', key: 'nav.home' },
  { view: 'knowledge', key: 'nav.knowledge' },
  { view: 'shelters', key: 'nav.shelters' },
  { view: 'chat', key: 'nav.chat' },
  { view: 'admin', key: 'nav.admin' },
]

function App() {
  const { t } = useTranslation()
  const [view, setView] = useState<View>('home')

  return (
    <div className="app">
      <header className="app-header">
        <button type="button" className="brand" onClick={() => setView('home')}>
          <span className="brand-mark" aria-hidden="true">🛟</span>
          <span>
            <span className="brand-title">{t('app.title')}</span>
            <span className="brand-subtitle">{t('app.subtitle')}</span>
          </span>
        </button>
        <LanguageSwitcher />
      </header>

      <nav className="app-nav">
        {NAV_ITEMS.map((item) => (
          <button
            key={item.view}
            type="button"
            className={view === item.view ? 'active' : ''}
            aria-current={view === item.view ? 'page' : undefined}
            onClick={() => setView(item.view)}
          >
            {t(item.key)}
          </button>
        ))}
      </nav>

      <main>
        {view === 'home' && <HomePage onNavigate={setView} />}
        {view === 'knowledge' && <KnowledgePage />}
        {view === 'shelters' && <SheltersMapPage />}
        {view === 'chat' && <ChatPage />}
        {view === 'admin' && <AdminPage />}
      </main>
    </div>
  )
}

export default App
