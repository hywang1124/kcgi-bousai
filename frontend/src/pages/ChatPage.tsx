import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { streamChat } from '../api/chat'

interface ChatMessage {
  role: 'user' | 'assistant'
  text: string
  sources?: string[]
  error?: boolean
}

/** 防災 AI 問答ページ。Claude 風のチャット UI で、回答はストリーミングで逐次表示する。 */
export function ChatPage() {
  const { t, i18n } = useTranslation()
  const [question, setQuestion] = useState('')
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [streaming, setStreaming] = useState(false)
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' })
  }, [messages])

  async function submitQuestion() {
    const text = question.trim()
    if (!text || streaming) return

    setQuestion('')
    setMessages((prev) => [...prev, { role: 'user', text }, { role: 'assistant', text: '' }])
    setStreaming(true)

    try {
      await streamChat(text, i18n.language, (delta) => {
        setMessages((prev) => {
          const next = [...prev]
          const last = next[next.length - 1]
          next[next.length - 1] = { ...last, text: last.text + delta }
          return next
        })
      })
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err)
      setMessages((prev) => {
        const next = [...prev]
        next[next.length - 1] = { role: 'assistant', text: `${t('chat.error')}: ${message}`, error: true }
        return next
      })
    } finally {
      setStreaming(false)
    }
  }

  return (
    <section className="chat-page">
      <h2>{t('chat.heading')}</h2>

      <div className="chat-transcript" ref={scrollRef}>
        {messages.length === 0 && <p className="chat-empty">{t('chat.placeholder')}</p>}
        {messages.map((m, i) => (
          <div key={i} className={`chat-bubble chat-bubble--${m.role}${m.error ? ' chat-bubble--error' : ''}`}>
            <div className="chat-bubble__text">
              {m.text || (m.role === 'assistant' && streaming && i === messages.length - 1 ? t('chat.thinking') : '')}
              {m.role === 'assistant' && streaming && i === messages.length - 1 && m.text && (
                <span className="chat-cursor" aria-hidden="true" />
              )}
            </div>
            {m.sources && m.sources.length > 0 && (
              <p className="chat-bubble__sources">
                {t('chat.sources')}: {m.sources.join(', ')}
              </p>
            )}
          </div>
        ))}
      </div>

      <form
        onSubmit={(e: FormEvent) => {
          e.preventDefault()
          void submitQuestion()
        }}
        className="chat-form"
      >
        <textarea
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault()
              void submitQuestion()
            }
          }}
          placeholder={t('chat.placeholder')}
          rows={2}
          disabled={streaming}
        />
        <button type="submit" disabled={streaming || !question.trim()}>
          {streaming ? t('chat.sending') : t('chat.send')}
        </button>
      </form>
    </section>
  )
}
