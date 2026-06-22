/** バックエンドの ChatResponse DTO に対応する型 */
export interface ChatAnswer {
  answer: string
  lang: string
  sources: string[]
}

/** バックエンドの ShelterResponse DTO に対応する型 */
export interface Shelter {
  id: number
  nameJa: string
  nameEn: string | null
  nameZh: string | null
  address: string | null
  lat: number
  lng: number
  capacity: number | null
  facilities: string[]
}
