<template>
  <div class="register-page">
    <div class="register-container">
      <a-card title="用户注册" class="register-card">
        <a-form
          :model="formState"
          :rules="rules"
          ref="formRef"
          @finish="handleRegister"
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

          <a-form-item label="确认密码" name="checkPassword">
            <a-input-password
              v-model:value="formState.checkPassword"
              placeholder="请再次输入密码"
              allow-clear
              size="large"
            />
          </a-form-item>

          <a-form-item>
            <a-button type="primary" html-type="submit" :loading="loading" block size="large">
              注册
            </a-button>
          </a-form-item>

          <a-form-item>
            <a-space>
              <span>已有账号？</span>
              <router-link to="/user/login">立即登录</router-link>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import type { Rule } from 'ant-design-vue/es/form'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const validatePassword = async (_rule: Rule, value: string) => {
  if (value === '') {
    return Promise.reject('请确认密码')
  }
  if (value !== formState.userPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const rules: Record<string, Rule[]> = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号长度不能少于 4 位', trigger: 'blur' },
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
  ],
  checkPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' },
  ],
}

async function handleRegister() {
  loading.value = true
  try {
    await userStore.doRegister(formState)
    message.success('注册成功')
    await router.push('/user/login')
  } catch (e: any) {
    message.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: calc(100vh - 64px);
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e0f7fa 0%, #e8f5e9 50%, #f1f8e9 100%);
}

.register-container {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 24px;
}

.register-card {
  width: 420px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 150, 136, 0.12);
}

.register-card :deep(.ant-card-head) {
  text-align: center;
  font-size: 20px;
  border-bottom: 1px solid #e8f5e9;
}

.register-card :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.register-card :deep(.ant-input),
.register-card :deep(.ant-input-password) {
  border-radius: 6px;
}

.register-card :deep(.ant-btn) {
  border-radius: 6px;
  height: 44px;
}
</style>

