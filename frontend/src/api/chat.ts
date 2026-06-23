import type { ChatAnswer } from './types'

// 開発時は空（Vite プロキシで /api を後端へ）。本番(GitHub Pages)はビルド時に
// VITE_API_BASE_URL で後端の公開 URL を指定する。
const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

/**
 * 防災 AI に質問する。
 */
export async function postChat(question: string, lang: string): Promise<ChatAnswer> {
  const res = await fetch(`${API_BASE}/api/v1/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question, lang }),
  })
  if (!res.ok) {
    throw new Error(`チャット要求に失敗しました (HTTP ${res.status})`)
  }
  return (await res.json()) as ChatAnswer
}
