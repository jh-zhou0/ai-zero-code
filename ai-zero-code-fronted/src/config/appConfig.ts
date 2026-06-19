/**
 * 应用配置常量
 * 统一管理环境变量和配置项
 */

import { CodeGenTypeEnum } from '@/constants/codeGenType.ts'

/**
 * 获取部署基础URL（已部署应用的访问地址）
 */
export const getDeployBaseUrl = (): string => {
  return import.meta.env.VITE_APP_DEPLOY_BASE_URL || 'http://localhost:8080'
}

/**
 * 获取预览基础URL（未部署应用的静态资源地址）
 */
export const getApiBaseUrl = (): string => {
  return import.meta.env.VITE_APP_API_BASE_URL || 'http://localhost:8123/api'
}

/**
 * 构建部署应用URL
 * @param deployKey 部署密钥
 * @returns 完整的部署应用URL
 */
export const buildDeployUrl = (deployKey: string): string => {
  if (!deployKey) return ''
  const baseUrl = getDeployBaseUrl()
  // 确保baseUrl不以/结尾，deployKey以/结尾
  return `${baseUrl.replace(/\/$/, '')}/${deployKey}/`
}

/**
 * 构建预览应用URL
 * @param codeGenType 代码生成类型
 * @param appId 应用ID
 * @returns 完整的预览应用URL
 */
export const buildPreviewUrl = (codeGenType: string, appId: string | number): string => {
  if (!codeGenType || !appId) return ''
  const baseUrl = getApiBaseUrl()
  const previewUrl = `${baseUrl.replace(/\/$/, '')}/static/${codeGenType}_${appId}/`
  if (codeGenType === CodeGenTypeEnum.VUE_PROJECT.value) {
    return `${baseUrl}/dist/index.html`
  }
  return previewUrl
}
