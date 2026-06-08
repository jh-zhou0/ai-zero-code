<template>
  <div class="login-page">
    <div class="login-container">
      <a-card title="用户登录" class="login-card">
        <a-form
          :model="formState"
          :rules="rules"
          ref="formRef"
          @finish="handleLogin"
          layout="vertical"
        >
          <a-form-item label="账号" name="userAccount">
            <a-input
              v-model:value="formState.userAccount"
              placeholder="请输入账号"
              allow-clear
              size="large"
            />
          </a-form-item>

          <a-form-item label="密码" name="userPassword">
            <a-input-password
              v-model:value="formState.userPassword"
              placeholder="请输入密码"
              allow-clear
              size="large"
            />
          </a-form-item>

          <a-form-item>
            <a-button type="primary" html-type="submit" :loading="loading" block size="large">
              登录
            </a-button>
          </a-form-item>

          <a-form-item>
            <a-space>
              <span>没有账号？</span>
              <router-link to="/user/register">立即注册</router-link>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import type { Rule } from 'ant-design-vue/es/form'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const rules: Record<string, Rule[]> = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号长度不能少于 4 位', trigger: 'blur' },
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
  ],
}

async function handleLogin() {
  loading.value = true
  try {
    await userStore.doLogin(formState)
    message.success('登录成功')
    // 登录成功后重定向到之前的页面或首页
    const redirect = (route.query.redirect as string) || '/'
    await router.push(redirect)
  } catch (e: unknown) {
    // 使用类型守卫安全地判断 e 是否为 Error 对象
    const errorMessage = e instanceof Error ? e.message : '登录失败'
    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 64px);
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e0f7fa 0%, #e8f5e9 50%, #f1f8e9 100%);
}

.login-container {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 24px;
}

.login-card {
  width: 420px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 150, 136, 0.12);
}

.login-card :deep(.ant-card-head) {
  text-align: center;
  font-size: 20px;
  border-bottom: 1px solid #e8f5e9;
}

.login-card :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.login-card :deep(.ant-input),
.login-card :deep(.ant-input-password) {
  border-radius: 6px;
}

.login-card :deep(.ant-btn) {
  border-radius: 6px;
  height: 44px;
}
</style>
