const STORAGE_KEY = 'bousai_token'

/** JWT をローカルストレージで永続化する簡易ストア。 */
export function getToken(): string | null {
  return localStorage.getItem(STORAGE_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(STORAGE_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(STORAGE_KEY)
}
