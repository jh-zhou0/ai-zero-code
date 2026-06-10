/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 部署域名（已部署应用的访问地址） */
  readonly VITE_APP_DEPLOY_BASE_URL: string
  /** 应用生成预览域名（未部署应用的静态资源地址） */
  readonly VITE_APP_PREVIEW_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
