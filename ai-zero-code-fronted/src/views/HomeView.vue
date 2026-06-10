<template>
  <div class="home-page">
    <!-- 网站标题 -->
    <div class="hero-section">
      <h1 class="hero-title">AI 应用生成平台</h1>
      <p class="hero-subtitle">一句话轻松创建网站应用</p>

      <!-- 提示词输入框 -->
      <div class="prompt-input-wrapper">
        <div class="input-container">
          <a-textarea
            v-model:value="prompt"
            placeholder="帮我创建个人博客网站"
            :rows="4"
            :maxLength="2000"
            class="prompt-input"
            @press-enter="handleCreateApp"
          />
          <a-button
            type="primary"
            shape="circle"
            :loading="creating"
            class="prompt-submit-btn"
            @click="handleCreateApp"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
            </template>
          </a-button>
        </div>
        <!-- 快捷提示词 -->
        <div class="quick-prompts">
          <a-button
            v-for="(item, index) in quickPrompts"
            :key="index"
            size="small"
            class="quick-prompt-btn"
            @click="selectPrompt(item)"
          >
            {{ item.label }}
          </a-button>
        </div>
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

// 快捷提示词
const quickPrompts = [
  {
    label: '📝 个人博客',
    value: '帮我创建一个现代化的个人博客网站，包含首页文章列表、文章详情页、关于我页面、联系表单等功能。要求采用简洁清新的设计风格，支持响应式布局，适配移动端和桌面端。配色以蓝白为主，字体清晰易读，整体风格专业但不失亲和力。',
  },
  {
    label: '🛒 电商展示',
    value: '帮我创建一个电商产品展示网站，包含商品分类导航、商品卡片列表、商品详情页、购物车功能、搜索过滤等。要求采用现代简约的设计风格，商品图片突出，价格信息清晰，支持筛选和排序功能。配色温暖舒适，用户体验流畅。',
  },
  {
    label: '📊 数据看板',
    value: '帮我创建一个数据分析仪表板网站，包含多个数据可视化图表（柱状图、折线图、饼图等）、关键指标卡片、数据表格、时间筛选器等。要求采用深色主题，图表色彩鲜明，数据展示清晰直观，支持实时数据更新和交互式筛选。',
  },
  {
    label: '🎨 作品集',
    value: '帮我创建一个创意作品集展示网站，包含项目网格展示、项目详情弹窗、分类过滤、平滑滚动动画、联系信息等。要求采用极简主义设计风格，大量留白，突出作品本身，支持图片懒加载和瀑布流布局，整体感觉高端大气。',
  },
]

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

function selectPrompt(item: { label: string; value: string }) {
  prompt.value = item.value
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
  width: 100%;
}

.hero-section {
  text-align: center;
  padding: 80px 24px 60px;
  background: linear-gradient(135deg, #e0f7fa 0%, #e8f5e9 50%, #e0f2f1 100%);
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #00b4d8, #0077b6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 40px;
}

.prompt-input-wrapper {
  max-width: 600px;
  margin: 0 auto;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
  justify-content: center;
}

.quick-prompt-btn {
  font-size: 13px;
  border-radius: 16px;
  padding: 4px 12px;
  height: auto;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
}

.quick-prompt-btn:hover {
  background: #fff;
  border-color: #00b4d8;
  color: #00b4d8;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 180, 216, 0.2);
}

.input-container {
  position: relative;
}

.prompt-input {
  border-radius: 12px;
  border: 2px solid #d9d9d9;
  transition: all 0.3s ease;
}

.prompt-input :deep(.ant-input) {
  border-radius: 12px;
  border: none;
  font-size: 15px;
  line-height: 1.6;
  padding-right: 55px !important;
}

.prompt-input :deep(.ant-input):focus {
  box-shadow: 0 0 0 2px rgba(0, 180, 216, 0.1);
}

.prompt-submit-btn {
  position: absolute;
  right: 10px;
  bottom: 10px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00b4d8, #0077b6);
  border: none;
  box-shadow: 0 2px 8px rgba(0, 180, 216, 0.3);
  transition: all 0.3s ease;
  z-index: 10;
}

.prompt-submit-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 180, 216, 0.4);
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
