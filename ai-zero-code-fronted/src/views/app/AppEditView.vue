<template>
  <div class="edit-page">
    <a-card :title="`编辑应用${appIdStr ? ` #${appIdStr}` : ''}`" class="edit-card">
      <a-spin :spinning="loading">
        <a-form
          ref="formRef"
          :model="formState"
          :rules="rules"
          layout="vertical"
          style="max-width: 600px"
        >
          <a-form-item label="应用名称" name="appName">
            <a-input
              v-model:value="formState.appName"
              placeholder="请输入应用名称"
              allow-clear
              size="large"
            />
          </a-form-item>

          <!-- 管理员可见：应用封面 -->
          <a-form-item label="应用封面" name="cover" v-if="userStore.isAdmin">
            <a-input
              v-model:value="formState.cover"
              placeholder="请输入封面 URL"
              allow-clear
              size="large"
            />
          </a-form-item>

          <!-- 管理员可见：优先级 -->
          <a-form-item label="优先级" name="priority" v-if="userStore.isAdmin">
            <a-input-number
              v-model:value="formState.priority"
              placeholder="优先级"
              :min="0"
              :max="999"
              style="width: 100%"
              size="large"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" :loading="submitting" @click="handleSubmit" size="large">
                保存修改
              </a-button>
              <a-button @click="goBack" size="large">取消</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import {
  getAppVoById,
  updateApp,
  getAppVoByIdByAdmin,
  updateAppByAdmin,
} from '@/api/appController'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const appIdStr = computed(() => route.params.id as string)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()

const formState = ref<{
  appName?: string
  cover?: string
  priority?: number
}>({
  appName: undefined,
  cover: undefined,
  priority: undefined,
})

const rules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
  ],
}

async function loadAppInfo() {
  loading.value = true
  try {
    if (userStore.isAdmin) {
      const res = await getAppVoByIdByAdmin({ id: appIdStr.value as unknown as number })
      if (res.data?.code === 0 && res.data?.data) {
        fillForm(res.data.data)
      } else {
        message.error(res.data?.message || '获取应用信息失败')
      }
    } else {
      const res = await getAppVoById({ id: appIdStr.value as unknown as number })
      if (res.data?.code === 0 && res.data?.data) {
        fillForm(res.data.data)
      } else {
        message.error(res.data?.message || '获取应用信息失败')
      }
    }
  } catch {
    message.error('获取应用信息失败')
  } finally {
    loading.value = false
  }
}

function fillForm(data: API.AppVO) {
  formState.value = {
    appName: data.appName,
    cover: data.cover,
    priority: data.priority,
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (userStore.isAdmin) {
      const res = await updateAppByAdmin({
        id: appIdStr.value as unknown as number,
        appName: formState.value.appName,
        cover: formState.value.cover || undefined,
        priority: formState.value.priority,
      })
      if (res.data?.code === 0) {
        message.success('保存成功')
      } else {
        message.error(res.data?.message || '保存失败')
        return
      }
    } else {
      const res = await updateApp({
        id: appIdStr.value as unknown as number,
        appName: formState.value.appName,
      })
      if (res.data?.code === 0) {
        message.success('保存成功')
      } else {
        message.error(res.data?.message || '保存失败')
        return
      }
    }
    await router.back()
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadAppInfo()
})
</script>

<style scoped>
.edit-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 0;
}

.edit-card {
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 150, 136, 0.1);
}

.edit-card :deep(.ant-card-head) {
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid #e8f5e9;
}
</style>
