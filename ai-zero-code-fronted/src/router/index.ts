import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useUserStore } from '@/stores/user'
import { checkAccess, AccessEnum } from '@/access'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { access: AccessEnum.PUBLIC },
    },
    {
      path: '/user/login',
      name: 'userLogin',
      component: () => import('@/views/user/UserLoginView.vue'),
      meta: { access: AccessEnum.PUBLIC },
    },
    {
      path: '/user/register',
      name: 'userRegister',
      component: () => import('@/views/user/UserRegisterView.vue'),
      meta: { access: AccessEnum.PUBLIC },
    },
    {
      path: '/admin/userManage',
      name: 'userManage',
      component: () => import('@/views/admin/UserManageView.vue'),
      meta: { access: AccessEnum.ADMIN, title: '用户管理' },
    },
    {
      path: '/admin/appManage',
      name: 'appManage',
      component: () => import('@/views/admin/AppManageView.vue'),
      meta: { access: AccessEnum.ADMIN, title: '应用管理' },
    },
    {
      path: '/app/chat/:id',
      name: 'appChat',
      component: () => import('@/views/app/AppChatView.vue'),
      meta: { access: AccessEnum.USER },
    },
    {
      path: '/app/manage',
      name: 'myAppManage',
      component: () => import('@/views/app/MyAppManageView.vue'),
      meta: { access: AccessEnum.USER, title: '我的应用' },
    },
    {
      path: '/app/edit/:id',
      name: 'appEdit',
      component: () => import('@/views/app/AppEditView.vue'),
      meta: { access: AccessEnum.USER },
    },
  ],
})

/**
 * 全局路由守卫：权限检查
 * 每次切换路由前，检查当前用户是否有权访问目标页面
 */
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  // 如果用户信息尚未加载，则尝试获取
  if (!userStore.isLoggedIn && to.meta?.access !== AccessEnum.PUBLIC) {
    await userStore.fetchCurrentUser()
  }

  // 检查权限
  const hasAccess = checkAccess(to, userStore)
  if (!hasAccess) {
    // 未登录 -> 跳转登录页
    if (!userStore.isLoggedIn) {
      next({ name: 'userLogin' })
    } else {
      // 已登录但权限不足 -> 跳转首页
      next({ name: 'home' })
    }
    return
  }

  next()
})

export default router
