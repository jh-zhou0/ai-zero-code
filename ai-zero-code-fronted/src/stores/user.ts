import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUser, login, logout, register } from '@/api/userController'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<API.LoginUserVO | null>(null)

  /**
   * 是否已登录
   */
  const isLoggedIn = computed(() => !!currentUser.value?.id)

  /**
   * 是否为管理员
   */
  const isAdmin = computed(() => currentUser.value?.userRole === 'admin')

  /**
   * 获取当前登录用户信息（从后端获取）
   */
  async function fetchCurrentUser() {
    try {
      const res = await getLoginUser()
      if (res.data?.code === 0 && res.data?.data) {
        currentUser.value = res.data.data
      } else {
        currentUser.value = null
      }
    } catch {
      currentUser.value = null
    }
    return currentUser.value
  }

  /**
   * 用户注册
   */
  async function doRegister(params: API.UserRegisterRequest) {
    const res = await register(params)
    if (res.data?.code === 0) {
      return res.data?.data
    }
    throw new Error(res.data?.message || '注册失败')
  }

  /**
   * 用户登录
   */
  async function doLogin(params: API.UserLoginRequest) {
    const res = await login(params)
    if (res.data?.code === 0 && res.data?.data) {
      currentUser.value = res.data.data
      return res.data.data
    }
    throw new Error(res.data?.message || '登录失败')
  }

  /**
   * 用户注销
   */
  async function doLogout() {
    const res = await logout()
    if (res.data?.code === 0) {
      currentUser.value = null
      return true
    }
    return false
  }

  return {
    currentUser,
    isLoggedIn,
    isAdmin,
    fetchCurrentUser,
    doRegister,
    doLogin,
    doLogout,
  }
})
