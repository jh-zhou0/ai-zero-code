<template>
  <a-layout-header class="global-header">
    <div class="header-left">
      <img class="logo" src="@/assets/logo.svg" alt="logo" />
      <span class="site-title">Kayson</span>
    </div>
    <div class="header-center">
      <a-menu
        v-model:selectedKeys="currentRoute"
        mode="horizontal"
        :items="fullMenuItems"
        @click="handleMenuClick"
      />
    </div>
    <div class="header-right">
      <template v-if="userStore.isLoggedIn">
        <a-dropdown>
          <a class="user-dropdown-link" @click.prevent>
            {{ userStore.currentUser?.userName || userStore.currentUser?.userAccount }}
            <down-outlined />
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item key="logout" @click="handleLogout">
                <export-outlined />
                注销
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </template>
      <template v-else>
        <a-space>
          <router-link to="/user/login">
            <a-button type="primary" ghost>登录</a-button>
          </router-link>
          <router-link to="/user/register">
            <a-button>注册</a-button>
          </router-link>
        </a-space>
      </template>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { DownOutlined, ExportOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { checkAccess } from '@/access'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

interface MenuItem {
  key: string
  label: string
  path: string
}

/**
 * 从路由配置中提取可用于菜单的页面列表
 * 根据当前用户的权限动态过滤
 */
const menuItems = computed<MenuItem[]>(() => {
  return router
    .getRoutes()
    .filter((r) => r.meta?.title && r.name !== 'home') // 只显示有标题的页面，排除首页（已手动添加）
    .filter((r) => {
      // 模拟 RouteLocationNormalized 对象给 checkAccess
      const routeLike = { meta: r.meta, path: r.path } as any
      return checkAccess(routeLike, userStore)
    })
    .map((r) => ({
      key: r.name as string,
      label: (r.meta?.title as string) || r.name as string,
      path: r.path,
    }))
})

// 把首页放在最前面
const fullMenuItems = computed<MenuItem[]>(() => {
  return [
    { key: 'home', label: '首页', path: '/' },
    ...menuItems.value,
  ]
})

const currentRoute = computed<string[]>(() => {
  const item = fullMenuItems.value.find((item) => item.path === route.path)
  return item ? [item.key] : []
})

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  const item = fullMenuItems.value.find((item) => item.key === key)
  if (item) {
    router.push(item.path)
  }
}

async function handleLogout() {
  try {
    const success = await userStore.doLogout()
    if (success) {
      message.success('已注销')
      await router.push('/')
    } else {
      message.error('注销失败')
    }
  } catch {
    message.error('注销失败')
  }
}
</script>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.logo {
  height: 32px;
  width: 32px;
  margin-right: 12px;
}

.site-title {
  font-size: 18px;
  font-weight: 600;
  color: #000;
  white-space: nowrap;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  overflow: hidden;
}

.header-center .ant-menu {
  flex: 1;
  justify-content: center;
  border-bottom: none;
  background: transparent;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

@media (max-width: 768px) {
  .global-header {
    padding: 0 12px;
  }

  .site-title {
    display: none;
  }
}
</style>
