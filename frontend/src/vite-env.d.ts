/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 本番(GitHub Pages)で後端 API のベース URL。開発時は未設定（Vite プロキシ）。 */
  readonly VITE_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
