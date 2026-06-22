import type { ChatAnswer } from './types'

/**
 * 防災 AI に質問する。
 * 開発時は Vite プロキシ経由で Spring Boot (/api/v1/chat) に到達する。
 */
export async function postChat(question: string, lang: string): Promise<ChatAnswer> {
  const res = await fetch('/api/v1/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question, lang }),
  })
  if (!res.ok) {
    throw new Error(`チャット要求に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as ChatAnswer
}
