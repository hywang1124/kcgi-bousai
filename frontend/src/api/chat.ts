import type { ChatAnswer } from './types'

// 開発時は空（Vite プロキシで /api を後端へ）。本番(GitHub Pages)はビルド時に
// VITE_API_BASE_URL で後端の公開 URL を指定する。
const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

function parseSseDataFrame(frame: string): string | null {
  const dataLines = frame
    .replace(/\r\n/g, '\n')
    .split('\n')
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice('data:'.length))

  if (dataLines.length === 0) {
    return null
  }
  return dataLines.join('\n')
}

/**
 * 防災 AI に質問する（非ストリーミング）。
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

/**
 * 防災 AI に質問する（ストリーミング）。テキスト断片を受け取るたびに onDelta を呼ぶ。
 */
export async function streamChat(
  question: string,
  lang: string,
  onDelta: (delta: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  const res = await fetch(`${API_BASE}/api/v1/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question, lang }),
    signal,
  })
  if (!res.ok || !res.body) {
    throw new Error(`チャット要求に失敗しました (HTTP ${res.status})`)
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  for (;;) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })

    let sepIndex: number
    while ((sepIndex = buffer.indexOf('\n\n')) !== -1) {
      const frame = buffer.slice(0, sepIndex)
      buffer = buffer.slice(sepIndex + 2)
      const data = parseSseDataFrame(frame)
      if (data !== null) {
        onDelta(data)
      }
    }
  }
}
