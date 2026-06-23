/** 災害種別 */
export type HazardType = 'FLOOD' | 'LANDSLIDE' | 'EARTHQUAKE' | 'TSUNAMI' | 'OTHER'

/** 危険度 */
export type SeverityLevel = 'LOW' | 'MEDIUM' | 'HIGH'

/** 危険区域（/api/v1/hazard-zones） */
export interface HazardZone {
  id: number
  type: HazardType
  severity: SeverityLevel
  nameJa: string
  nameEn: string | null
  nameZh: string | null
  description: string | null
  /** GeoJSON ジオメトリ文字列（JSON.parse して描画） */
  geojson: string
}

/** ユーザ役割 */
export type Role = 'ADMIN' | 'EDITOR' | 'USER'

/** 管理対象ユーザ（/api/v1/admin/users） */
export interface UserAccount {
  id: number
  username: string
  role: Role
  enabled: boolean
}

/** ログイン成功時のレスポンス */
export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
}

/** 認証中の管理者情報（/api/v1/admin/me） */
export interface AdminMe {
  username: string
  roles: string[]
}

/** バックエンドの ChatResponse DTO に対応する型 */
export interface ChatAnswer {
  answer: string
  lang: string
  sources: string[]
}
