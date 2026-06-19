/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 后端 API 基础地址 */
  readonly VITE_APP_API_BASE_URL: string
  /** 部署域名（已部署应用的访问地址） */
  readonly VITE_APP_DEPLOY_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
