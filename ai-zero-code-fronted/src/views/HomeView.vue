<template>
  <div class="home-page">
    <!-- 网站标题 -->
    <div class="hero-section">
      <h1 class="hero-title">AI Zero Code</h1>
      <p class="hero-subtitle">通过对话描述你的想法，AI 自动生成网站应用</p>

      <!-- 提示词输入框 -->
      <div class="prompt-input-wrapper">
        <a-input
          v-model:value="prompt"
          placeholder="描述你想要的应用，例如：帮我生成一个待办事项管理网站"
          size="large"
          class="prompt-input"
          @press-enter="handleCreateApp"
        />
        <a-button
          type="primary"
          size="large"
          :loading="creating"
          class="prompt-submit-btn"
          @click="handleCreateApp"
        >
          开始生成
        </a-button>
      </div>
    </div>

    <!-- 内容区：我的应用 + 精选应用 -->
    <div class="content-section">
      <!-- 我的应用 -->
      <template v-if="userStore.isLoggedIn">
        <div class="section-header">
          <h2 class="section-title">我的应用</h2>
          <a-input-search
            v-model:value="mySearchName"
            placeholder="搜索我的应用名称"
            allow-clear
            style="width: 240px"
            @search="handleMySearch"
          />
        </div>
        <a-row :gutter="[16, 16]" v-if="myApps.length > 0">
          <a-col
            v-for="app in myApps"
            :key="app.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <AppCard :app="app" @click="goToChat">
              <template #overlay>
                <a-button
                  type="primary"
                  shape="round"
                  size="small"
                  class="overlay-btn"
                  @click.stop="goToChat(app)"
                >
                  查看对话
                </a-button>
              </template>
            </AppCard>
          </a-col>
        </a-row>
        <a-empty v-else-if="!myLoading" description="暂无应用，快去创建一个吧" />
        <div class="pagination-wrapper" v-if="myTotal > myPageSize">
          <a-pagination
            v-model:current="myPageNum"
            :page-size="myPageSize"
            :total="myTotal"
            @change="loadMyApps"
            size="small"
          />
        </div>
      </template>

      <!-- 分割线 -->
      <a-divider v-if="userStore.isLoggedIn && goodApps.length > 0" />

      <!-- 精选应用 -->
      <div class="section-header">
        <h2 class="section-title">精选应用</h2>
        <a-input-search
          v-model:value="goodSearchName"
          placeholder="搜索精选应用名称"
          allow-clear
          style="width: 240px"
          @search="handleGoodSearch"
        />
      </div>
      <a-row :gutter="[16, 16]" v-if="goodApps.length > 0">
        <a-col
          v-for="app in goodApps"
          :key="app.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <AppCard :app="app" @click="goToChat">
            <template #overlay>
              <a-button
                type="primary"
                shape="round"
                size="small"
                class="overlay-btn"
                @click.stop="handlePreview(app)"
                :disabled="!app.deployKey"
              >
                预览
              </a-button>
            </template>
          </AppCard>
        </a-col>
      </a-row>
      <a-empty v-else-if="!goodLoading" description="暂无精选应用" />
      <div class="pagination-wrapper" v-if="goodTotal > goodPageSize">
        <a-pagination
          v-model:current="goodPageNum"
          :page-size="goodPageSize"
          :total="goodTotal"
          @change="loadGoodApps"
          size="small"
        />
      </div>
    </div>

    <!-- 预览弹窗 -->
    <a-modal
      v-model:open="previewVisible"
      :title="previewApp?.appName || '应用预览'"
      :footer="null"
      width="80%"
      :style="{ top: '20px' }"
      destroy-on-close
    >
      <div style="height: 70vh;">
        <AppPreview
          :deploy-key="previewApp?.deployKey"
          :app-id="previewApp?.id"
          :code-gen-type="previewApp?.codeGenType"
          empty-text="该应用暂无可预览内容"
        />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import AppPreview from '@/components/AppPreview.vue'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const userStore = useUserStore()

// 创建应用
const prompt = ref('')
const creating = ref(false)

async function handleCreateApp() {
  const trimmedPrompt = prompt.value.trim()
  if (!trimmedPrompt) {
    message.warning('请输入应用描述')
    return
  }
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }
    creating.value = true
    try {
      const res = await addApp({ initPrompt: trimmedPrompt })
      if (res.data?.code === 0 && res.data?.data) {
        const appId = res.data.data
        message.success('应用创建成功')
        // 带 ?new=true 参数，标识这是新建应用，需自动触发 AI 生成
        await router.push(`/app/chat/${appId}?new=true`)
      } else {
        message.error(res.data?.message || '创建失败')
      }
  } catch {
    message.error('创建失败')
  } finally {
    creating.value = false
  }
}

// 我的应用列表
const myApps = ref<API.AppVO[]>([])
const myLoading = ref(false)
const myPageNum = ref(1)
const myPageSize = ref(8)
const myTotal = ref(0)
const mySearchName = ref('')

async function loadMyApps() {
  myLoading.value = true
  try {
    const res = await listMyAppVoByPage({
      pageNum: myPageNum.value,
      pageSize: myPageSize.value,
      appName: mySearchName.value || undefined,
    })
    if (res.data?.code === 0 && res.data?.data) {
      myApps.value = res.data.data.records || []
      myTotal.value = res.data.data.totalRow || 0
    }
  } catch {
    message.error('获取我的应用列表失败')
  } finally {
    myLoading.value = false
  }
}

function handleMySearch() {
  myPageNum.value = 1
  loadMyApps()
}

// 精选应用列表
const goodApps = ref<API.AppVO[]>([])
const goodLoading = ref(false)
const goodPageNum = ref(1)
const goodPageSize = ref(8)
const goodTotal = ref(0)
const goodSearchName = ref('')

async function loadGoodApps() {
  goodLoading.value = true
  try {
    const res = await listGoodAppVoByPage({
      pageNum: goodPageNum.value,
      pageSize: goodPageSize.value,
      appName: goodSearchName.value || undefined,
    })
    if (res.data?.code === 0 && res.data?.data) {
      goodApps.value = res.data.data.records || []
      goodTotal.value = res.data.data.totalRow || 0
    }
  } catch {
    message.error('获取精选应用列表失败')
  } finally {
    goodLoading.value = false
  }
}

function handleGoodSearch() {
  goodPageNum.value = 1
  loadGoodApps()
}

function goToChat(app: API.AppVO) {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }
  router.push(`/app/chat/${app.id}`)
}

// 预览弹窗
const previewVisible = ref(false)
const previewApp = ref<API.AppVO | null>(null)

function handlePreview(app: API.AppVO) {
  previewApp.value = app
  previewVisible.value = true
}
onMounted(() => {
  if (userStore.isLoggedIn) {
    loadMyApps()
  }
  loadGoodApps()
})
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-section {
  text-align: center;
  padding: 48px 24px 32px;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #00b4d8, #0077b6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 16px;
  color: #666;
  margin-bottom: 32px;
}

.prompt-input-wrapper {
  display: flex;
  max-width: 700px;
  margin: 0 auto;
  gap: 12px;
}

.prompt-input {
  flex: 1;
  border-radius: 8px;
}

.prompt-input :deep(.ant-input) {
  border-radius: 8px;
}

.prompt-submit-btn {
  border-radius: 8px;
  height: 44px;
  padding: 0 32px;
  font-size: 16px;
  white-space: nowrap;
}

.content-section {
  padding: 0 24px 48px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.overlay-btn {
  opacity: 0;
  transform: translateY(8px);
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.app-card-cover:hover .overlay-btn {
  opacity: 1;
  transform: translateY(0);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 28px;
  }

  .prompt-input-wrapper {
    flex-direction: column;
  }

  .content-section {
    padding: 0 12px 32px;
  }
}
</style>
