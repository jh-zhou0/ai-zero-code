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
        :items="menuItems"
        @click="handleMenuClick"
      />
    </div>
    <div class="header-right">
      <a-button type="primary" ghost>登录</a-button>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()

interface MenuItem {
  key: string
  label: string
  path: string
}

// 菜单配置，可通过此数组扩展菜单项
const menuItems: MenuItem[] = [
  { key: 'home', label: '首页', path: '/' },
  { key: 'about', label: '关于', path: '/about' },
]

const currentRoute = computed<string[]>(() => {
  const item = menuItems.find((item) => item.path === route.path)
  return item ? [item.key] : []
})

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  const item = menuItems.find((item) => item.key === key)
  if (item) {
    router.push(item.path)
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
