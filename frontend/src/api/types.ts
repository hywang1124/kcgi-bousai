/** バックエンドの ChatResponse DTO に対応する型 */
export interface ChatAnswer {
  answer: string
  lang: string
  sources: string[]
}
