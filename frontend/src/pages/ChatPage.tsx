import { useState } from 'react'
import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { postChat } from '../api/chat'
import type { ChatAnswer } from '../api/types'

/** 防災 AI 問答ページ。質問を送信し、回答と参照元を表示する。 */
export function ChatPage() {
  const { t, i18n } = useTranslation()
  const [question, setQuestion] = useState('')
  const [answer, setAnswer] = useState<ChatAnswer | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!question.trim()) return
    setLoading(true)
    setError(null)
    try {
      const res = await postChat(question, i18n.language)
      setAnswer(res)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : String(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section>
      <h2>{t('chat.heading')}</h2>
      <form onSubmit={handleSubmit} className="chat-form">
        <textarea
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          placeholder={t('chat.placeholder')}
          rows={3}
        />
        <button type="submit" disabled={loading || !question.trim()}>
          {loading ? t('chat.sending') : t('chat.send')}
        </button>
      </form>

      {error && (
        <p role="alert">
          {t('chat.error')}: {error}
        </p>
      )}

      {answer && (
        <div className="chat-answer">
          <p>{answer.answer}</p>
          {answer.sources.length > 0 && (
            <p className="sources">
              {t('chat.sources')}: {answer.sources.join(', ')}
            </p>
          )}
        </div>
      )}
    </section>
  )
}
