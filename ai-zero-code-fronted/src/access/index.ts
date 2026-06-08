import type { RouteLocationNormalized } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 权限级别枚举
 */
export enum AccessEnum {
  /** 公开，无需登录 */
  PUBLIC = 'public',
  /** 登录用户可访问 */
  USER = 'user',
  /** 管理员可访问 */
  ADMIN = 'admin',
}

/**
 * 检查当前用户是否有权限访问指定路由
 * @param route 目标路由
 * @param userStore 用户状态
 * @returns 是否有权限
 */
export function checkAccess(
  route: RouteLocationNormalized,
  userStore: ReturnType<typeof useUserStore>,
): boolean {
  const requiredAccess = route.meta?.access as string | undefined

  // 如果路由未设置访问权限，默认为公开
  if (!requiredAccess || requiredAccess === AccessEnum.PUBLIC) {
    return true
  }

  // 需要登录但未登录
  if (!userStore.isLoggedIn) {
    return false
  }

  // 需要管理员权限
  if (requiredAccess === AccessEnum.ADMIN) {
    return userStore.isAdmin
  }

  // 需要登录用户（普通用户级别），已登录即可
  if (requiredAccess === AccessEnum.USER) {
    return true
  }

  return false
}
