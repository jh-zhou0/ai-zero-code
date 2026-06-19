<template>
  <div class="chat-container">
    <!-- 顶部栏 -->
    <div class="chat-header">
      <div class="header-left">
        <a-button type="text" class="back-btn" @click="goBack">
          <arrow-left-outlined />
        </a-button>
        <span class="app-name">{{ appInfo?.appName || '应用加载中...' }}</span>
        <a-tag v-if="codeGenType" color="blue" class="gen-type-tag">{{ getCodeGenTypeLabel(codeGenType) }}</a-tag>
      </div>
      <div class="header-right">
        <a-button
          type="default"
          :loading="downloading"
          :disabled="!codeGenType || !isCodeGenerated || !canDownload"
          @click="handleDownloadCode"
        >
          <download-outlined />
          下载代码
        </a-button>
        <a-button
          type="primary"
          :loading="deploying"
          :disabled="!codeGenType"
          @click="handleDeploy"
        >
          <cloud-upload-outlined />
          部署
        </a-button>
        <a-button
          type="default"
          class="edit-btn"
          @click="goEdit"
        >
          <edit-outlined />
          编辑信息
        </a-button>
      </div>
    </div>

    <!-- 核心内容区域：左右布局 -->
    <div class="chat-body">
      <!-- 左侧：对话区域 -->
      <div class="chat-left">
        <div class="message-list" ref="messageListRef">
          <!-- 加载更多 -->
          <div v-if="historyHasMore" class="load-more-wrapper">
            <a-button
              type="dashed"
              :loading="historyLoading"
              @click="handleLoadMore"
            >
              加载更多消息
            </a-button>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="msg.key"
            class="message-item"
            :class="msg.role === 'user' ? 'message-user' : 'message-ai'"
          >
            <div class="message-avatar">
              <a-avatar
                v-if="msg.role === 'user'"
                :size="36"
                style="backgroundColor: #1890ff; flexShrink: 0;"
              >
                {{ userStore.currentUser?.userName?.charAt(0) || 'U' }}
              </a-avatar>
              <a-avatar
                v-else
                :size="36"
                style="backgroundColor: #52c41a; flexShrink: 0;"
              >
                AI
              </a-avatar>
            </div>
            <div class="message-content">
              <!-- AI 思考中：显示波纹动画，思考完成后直接在同一气泡输出内容 -->
              <div v-if="msg.role === 'ai' && !msg.content && msg.isCurrentSession && isLastMsg(index)" class="message-bubble thinking-dots">
                <span class="thinking-dot">.</span>
                <span class="thinking-dot">.</span>
                <span class="thinking-dot">.</span>
              </div>
              <div v-else class="message-bubble" :class="msg.role === 'ai' ? 'markdown-body' : ''" v-html="msg.role === 'ai' ? renderedMsg(msg.content) : msg.content"></div>
            </div>
          </div>
        </div>
        <div class="chat-input-area">
          <div class="input-container">
            <a-textarea
              v-model:value="userInput"
              placeholder="请描述你想生成的网站，越详细效果越好哦"
              :rows="2"
              :maxLength="2000"
              @press-enter="handleSend"
              class="chat-textarea"
              :disabled="aiThinking"
            />
            <a-button
              type="primary"
              shape="circle"
              :loading="aiThinking"
              :disabled="!userInput.trim()"
              @click="handleSend"
              class="chat-submit-btn"
            >
              <template #icon>
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="22" y1="2" x2="11" y2="13"></line>
                  <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                </svg>
              </template>
            </a-button>
          </div>
        </div>
      </div>

      <!-- 右侧：网页预览区域（始终显示） -->
      <div class="chat-right">
        <!-- 代码已生成完成 → 展示预览 -->
        <AppPreview
          v-if="shouldShowPreview && codeGenType"
          :deploy-key="deployKey"
          :app-id="appIdStr"
          :code-gen-type="codeGenType"
          empty-text="暂无预览"
        />
        <!-- 正在生成应用 → 展示加载动画 -->
        <div v-else-if="aiThinking" class="generating-wrapper">
          <div class="generating-animation">
            <div class="orbit-ring orbit-ring-1"></div>
            <div class="orbit-ring orbit-ring-2"></div>
            <div class="orbit-ring orbit-ring-3"></div>
            <div class="orbit-center">
              <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="16 3 21 3 21 8" />
                <line x1="4" y1="20" x2="21" y2="3" />
                <polyline points="21 16 21 21 16 21" />
                <line x1="15" y1="15" x2="21" y2="21" />
                <line x1="4" y1="4" x2="9" y2="9" />
              </svg>
            </div>
          </div>
          <div class="generating-text">AI 正在生成应用...</div>
          <div class="generating-steps">
            <div class="step-item" :class="{ active: true }">
              <div class="step-dot"></div>
              <span>理解你的需求</span>
            </div>
            <div class="step-item" :class="{ active: true }">
              <div class="step-dot"></div>
              <span>生成前端代码</span>
            </div>
            <div class="step-item">
              <div class="step-dot"></div>
              <span>构建应用页面</span>
            </div>
          </div>
        </div>
        <!-- 默认空状态 -->
        <AppPreview
          v-else
          :deploy-key="deployKey"
          :app-id="appIdStr"
          :code-gen-type="codeGenType"
          empty-text="暂无预览"
        />
      </div>
    </div>
  </div>

  <!-- 部署成功弹框 -->
  <a-modal
    v-model:visible="deployModalVisible"
    title="部署成功"
    :footer="null"
    :closable="true"
  >
    <div style="display: flex; align-items: center; gap: 8px; padding: 8px 0;">
      <a-input
        :value="deployUrl"
        readonly
        style="flex: 1;"
      />
      <a-button type="primary" @click="copyDeployUrl">
        <copy-outlined />
        复制链接
      </a-button>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, CloudUploadOutlined, DownloadOutlined, EditOutlined, CopyOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { getAppVoById, deployApp } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import AppPreview from '@/components/AppPreview.vue'
import { getApiBaseUrl } from '@/config/appConfig'
import myAxios from '@/request'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'
import MarkdownIt from 'markdown-it'
import { highlightCode } from '@/utils/codeHighlight'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/**
 * 从路由参数获取应用 ID。
 * 保持为字符串避免大整数精度丢失（后端返回的 ID 可能是 string 类型）。
 */
const appIdStr = computed(() => route.params.id as string)
const appInfo = ref<API.AppVO | null>(null)

// 聊天消息
const messages = ref<{ role: string; content: string; key: string; isCurrentSession: boolean }[]>([])
let msgKeyCounter = 0
const userInput = ref('')
const aiThinking = ref(false)
const messageListRef = ref<HTMLElement | null>(null)

// 对话历史管理
const historyLoading = ref(false)
const historyCursor = ref<string | undefined>(undefined)
const historyHasMore = ref(false)
const chatHistoryTotal = ref(0)

// 预览 - codeGenType 在应用创建时就有，不能用于判断代码是否已生成
const codeGenType = ref('')
const deployKey = ref('')
/** 标记 SSE 流式调用是否已完成，即代码是否真正生成完毕 */
const hasGeneratedCode = ref(false)
/** 标记 SSE 会话是否已结束（收到 done 或 error 后设为 true），避免重复处理 */
const sseFinished = ref(false)

// 部署
const deploying = ref(false)

// 下载代码
const downloading = ref(false)

// 部署成功弹框
const deployModalVisible = ref(false)
const deployUrl = ref('')

/**
 * 是否展示预览：
 * 1. 当前会话已生成代码（hasGeneratedCode）
 * 2. 或者已有 deployKey（之前已部署）
 * 3. 或者该应用有至少 2 条对话记录且已有 codeGenType
 */
const shouldShowPreview = computed(() => {
  if (!codeGenType.value) return false
  return hasGeneratedCode.value || !!deployKey.value || chatHistoryTotal.value >= 2
})

/**
 * 代码是否已生成（当前会话生成完成 或 历史已有生成记录 或 已部署）
 */
const isCodeGenerated = computed(() => {
  return hasGeneratedCode.value || !!deployKey.value || chatHistoryTotal.value >= 2
})

/**
 * 自己创建的应用：appInfo.userId === currentUser.id
 */
const isOwnApp = computed(() => {
  if (!appInfo.value?.userId || !userStore.currentUser?.id) return false
  return appInfo.value.userId === userStore.currentUser.id
})

/**
 * 是否有权限下载代码：应用创建者本人 或 管理员
 */
const canDownload = computed(() => {
  return isOwnApp.value || userStore.isAdmin
})

/**
 * Markdown 渲染器配置：
 * - 启用 HTML 标签
 * - 启用链接自动识别
 * - 启用 typographer 替代（智能引号等）
 * - 使用 highlight.js 进行代码高亮
 */
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: highlightCode,
})

/**
 * 判断是否为最新一条消息（用于思考中动画的显示判定）
 */
function isLastMsg(index: number) {
  return index === messages.value.length - 1
}

/**
 * 渲染消息内容：使用 markdown-it 将 Markdown 文本渲染为 HTML
 */
function renderedMsg(content: string) {
  if (!content) return ''
  return md.render(content)
}

/**
 * 获取应用详情
 */
async function loadAppInfo() {
  try {
    const res = await getAppVoById({ id: appIdStr.value as unknown as number })
    if (res.data?.code === 0 && res.data?.data) {
      appInfo.value = res.data.data
      if (appInfo.value.codeGenType) {
        codeGenType.value = appInfo.value.codeGenType
      }
      if (appInfo.value.deployKey) {
        deployKey.value = appInfo.value.deployKey
        // 已有部署记录，说明代码之前已生成完成，直接展示预览
        hasGeneratedCode.value = true
      }
    } else {
      message.error(res.data?.message || '获取应用信息失败')
    }
  } catch {
    message.error('获取应用信息失败')
  }
}

/**
 * 将 ChatHistory 记录转换为前端消息格式
 */
function convertHistoryToMessage(record: API.ChatHistory): {
  role: string
  content: string
  key: string
  isCurrentSession: boolean
} {
  return {
    role: record.messageType === 'user' ? 'user' : 'ai',
    content: record.message || '',
    key: `history-${record.id || msgKeyCounter++}`,
    isCurrentSession: false,
  }
}

/**
 * 加载对话历史（游标分页，每次 10 条）
 */
async function loadHistory(lastCreateTime?: string) {
  historyLoading.value = true
  try {
    const res = await listAppChatHistory({
      appId: appIdStr.value as unknown as number,
      pageSize: 10,
      lastCreateTime: lastCreateTime,
    })
    if (res.data?.code === 0 && res.data?.data) {
      const pageData = res.data.data
      const records = pageData.records || []
      chatHistoryTotal.value = Number(pageData.totalRow) || 0

      // 后端返回的记录是降序（最新在前），需要反转成升序（最旧在前/最新在底）展示
      const converted = records
        .filter((r) => r.messageType && r.message)
        .map(convertHistoryToMessage)
        .reverse()

      if (lastCreateTime) {
        // 加载更早的历史：追加到现有历史消息之前
        messages.value = [...converted, ...messages.value]
      } else {
        // 首次加载：直接设置为消息列表（已反转升序）
        messages.value = converted
      }

      // 更新游标：取当前页最旧的消息的 createTime（即 records 最后一个元素，因为后端返回降序）
      if (records.length > 0) {
        const oldestRecord = records[records.length - 1]
        historyCursor.value = oldestRecord?.createTime || undefined
      } else {
        historyCursor.value = undefined
      }

      // 是否还有更多
      const loadedCount = lastCreateTime
        ? messages.value.length
        : converted.length
      historyHasMore.value = loadedCount < chatHistoryTotal.value

      // 滚动到底部
      scrollToBottom()
    } else {
      message.error(res.data?.message || '获取对话历史失败')
    }
  } catch {
    message.error('获取对话历史失败')
  } finally {
    historyLoading.value = false
  }
}

/**
 * 加载更多历史消息
 */
function handleLoadMore() {
  if (historyLoading.value || !historyCursor.value) return
  loadHistory(historyCursor.value)
}

/**
 * 使用 EventSource 发送 SSE 请求
 */
function sendMessage(messageText: string) {
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: messageText,
    key: `session-${msgKeyCounter++}`,
    isCurrentSession: true,
  })
  userInput.value = ''
  aiThinking.value = true
  sseFinished.value = false

  // 创建一个 AI 消息占位
  messages.value.push({
    role: 'ai',
    content: '',
    key: `session-${msgKeyCounter++}`,
    isCurrentSession: true,
  })

  // 使用 EventSource 连接 SSE
  const baseUrl = getApiBaseUrl()
  const url = `${baseUrl}/app/chat/gen/code?appId=${appIdStr.value}&message=${encodeURIComponent(messageText)}`

  const eventSource = new EventSource(url, { withCredentials: true })

  // 处理普通消息事件 - 后端每个 chunk 包装为 { "d": "chunk内容" } 格式
  eventSource.onmessage = (event) => {
    if (sseFinished.value) return
    const data = event.data
    // 拼接内容
    const lastMsgIndex = messages.value.length - 1
    const lastMsg = messages.value[lastMsgIndex]
    if (lastMsg && lastMsg.role === 'ai') {
      try {
        const parsed = JSON.parse(data)
        // 后端包装格式：{ "d": "代码片段" }
        if (parsed.d) {
          lastMsg.content += parsed.d
        } else if (typeof parsed === 'string') {
          lastMsg.content += parsed
        }
      } catch {
        lastMsg.content += data
      }
    }
    scrollToBottom()
  }

  // 处理 done 事件 - 后端通过 .event("done") 发送
  eventSource.addEventListener('done', async () => {
    if (sseFinished.value) return
    sseFinished.value = true
    eventSource.close()
    // 刷新应用信息（获取 deployKey 等）
    await loadAppInfo()
    // 标记代码已生成完成，右侧显示预览
    hasGeneratedCode.value = true
    aiThinking.value = false
    message.success('网站生成完成！')
    scrollToBottom()
  })

  eventSource.onerror = async () => {
    if (sseFinished.value) return
    sseFinished.value = true
    eventSource.close()
    if (aiThinking.value) {
      // 由于 EventSource 在 done 后也会触发 onerror，此时 sseFinished 已为 true 不会重复进入
      // 如果内容为空，移除占位
      const lastMsgIndex = messages.value.length - 1
      const lastMsg = messages.value[lastMsgIndex]
      if (lastMsg && lastMsg.role === 'ai' && !lastMsg.content) {
        messages.value.pop()
      }
      aiThinking.value = false
    }
    scrollToBottom()
  }
}

/**
 * 发送消息
 */
function handleSend() {
  const text = userInput.value.trim()
  if (!text || aiThinking.value) return
  sendMessage(text)
}

/**
 * 下载应用代码
 */
async function handleDownloadCode() {
  if (!appIdStr.value) return
  downloading.value = true
  try {
    const response = await myAxios.get(`/app/download/${appIdStr.value}`, {
      responseType: 'blob',
    })

    // 从 Content-Disposition 响应头中提取文件名
    const contentDisposition = response.headers?.['content-disposition']
    let filename = `app_${appIdStr.value}.zip`
    if (contentDisposition) {
      // 优先匹配 RFC 5987 格式 (filename*=UTF-8''%E4%B8%AD%E6%96%87.zip)
      const rfc5987Match = contentDisposition.match(/filename\*=(?:UTF-8\'\')?(.+?)(?:;|$)/i)
      if (rfc5987Match) {
        filename = decodeURIComponent(rfc5987Match[1].trim())
      } else {
        // 回退匹配标准格式 (filename="xxx.zip")
        const standardMatch = contentDisposition.match(/filename="?([^";]+)"?/i)
        if (standardMatch) {
          filename = standardMatch[1].trim()
        }
      }
    }

    // 创建 Blob URL 并触发下载
    const blob = new Blob([response.data], { type: 'application/zip' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    message.success('代码下载成功')
  } catch {
    message.error('下载代码失败')
  } finally {
    downloading.value = false
  }
}

/**
 * 部署应用
 */
async function handleDeploy() {
  if (!appIdStr.value) return
  deploying.value = true
  try {
    const res = await deployApp({ appId: appIdStr.value as unknown as number })
    if (res.data?.code === 0 && res.data?.data) {
      deployUrl.value = res.data.data
      deployModalVisible.value = true
      // 刷新应用信息以获取 deployKey
      await loadAppInfo()
    } else {
      message.error(res.data?.message || '部署失败')
    }
  } catch {
    message.error('部署失败')
  } finally {
    deploying.value = false
  }
}

function copyDeployUrl() {
  navigator.clipboard.writeText(deployUrl.value)
  message.success('链接已复制')
}

function goBack() {
  router.push('/')
}

function goEdit() {
  router.push(`/app/edit/${appIdStr.value}`)
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

onMounted(async () => {
  await loadAppInfo()

  // 1. 加载对话历史（游标分页，首次加载最近 10 条）
  await loadHistory()

  // 2. 自动发送初始消息逻辑：
  //    - 是自己的 app
  //    - 并且没有对话历史（totalRow === 0）
  //    - 才有 initPrompt 才自动触发
  if (isOwnApp.value && chatHistoryTotal.value === 0 && appInfo.value?.initPrompt) {
    console.log('自动发送初始消息：', appInfo.value.initPrompt)
    sendMessage(appInfo.value.initPrompt)
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  font-size: 18px;
}

.app-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.header-right {
  display: flex;
  gap: 8px;
}

.edit-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ===== 左侧：对话区域 ===== */
.chat-left {
  flex: 0 0 40%;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #f0f0f0;
  min-width: 320px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
}

.load-more-wrapper {
  text-align: center;
  padding: 8px 0 16px;
}

.message-item {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.message-user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: calc(100% - 60px);
  min-width: 0;
}

.message-user .message-content {
  display: flex;
  justify-content: flex-end;
}

.message-bubble {
  max-width: 100%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  /* 需要 white-space 为 normal 以便 markdown-it 渲染的 HTML 正常换行 */
  white-space: normal;
}

.message-user .message-bubble {
  background: #1890ff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-ai .message-bubble {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-bottom-left-radius: 4px;
  color: #333;
}

.thinking-dots {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 4px;
  color: #999;
}

.thinking-dot {
  display: inline-block;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  color: #999;
  animation: thinking-bounce 1.4s ease-in-out infinite;
}

.thinking-dot:nth-child(1) {
  animation-delay: 0s;
}

.thinking-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes thinking-bounce {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: translateY(0);
  }
  30% {
    opacity: 1;
    transform: translateY(-6px);
  }
}

.chat-input-area {
  display: flex;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  gap: 10px;
  align-items: flex-end;
}

.input-container {
  position: relative;
  width: 100%;
}

.chat-textarea {
  border-radius: 12px;
  border: 2px solid #d9d9d9;
  transition: all 0.3s ease;
}

.chat-textarea:hover {
  border-color: #40a9ff;
}

.chat-textarea:focus-within {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.chat-textarea :deep(.ant-input) {
  border-radius: 12px;
  border: none;
  font-size: 14px;
  line-height: 1.6;
  padding-right: 50px !important;
  overflow-x: hidden;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.chat-submit-btn {
  position: absolute;
  right: 8px;
  bottom: 8px;
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

.chat-submit-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 180, 216, 0.4);
}

.chat-submit-btn:disabled {
  background: #d9d9d9;
  box-shadow: none;
}

/* ===== 右侧：网页预览区域 ===== */
.chat-right {
  flex: 0 0 60%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

/* 生成中动画区域 */
.generating-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0f7fa 0%, #e8f5e9 50%, #e0f2f1 100%);
}

.generating-animation {
  position: relative;
  width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
}

.orbit-ring {
  position: absolute;
  border-radius: 50%;
  border: 2.5px solid transparent;
  animation: orbit-spin 2.5s linear infinite;
}

.orbit-ring-1 {
  width: 160px;
  height: 160px;
  border-top-color: #4db6ac;
  border-right-color: #4dd0e1;
  animation-duration: 2.5s;
}

.orbit-ring-2 {
  width: 120px;
  height: 120px;
  border-bottom-color: #26a69a;
  border-left-color: #80cbc4;
  animation-duration: 3.2s;
  animation-direction: reverse;
}

.orbit-ring-3 {
  width: 80px;
  height: 80px;
  border-top-color: #80deea;
  border-bottom-color: #a5d6a7;
  animation-duration: 1.8s;
}

.orbit-center {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #b2dfdb, #c8e6c9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00796b;
  box-shadow: 0 4px 16px rgba(0, 150, 136, 0.3);
}

@keyframes orbit-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.generating-text {
  font-size: 18px;
  font-weight: 600;
  color: #00796b;
  margin-bottom: 24px;
  animation: pulse-text 2s ease-in-out infinite;
}

@keyframes pulse-text {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

.generating-steps {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #999;
  transition: color 0.5s ease;
}

.step-item.active {
  color: #00796b;
}

.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e0e0e0;
  transition: background 0.5s ease, box-shadow 0.5s ease;
}

.step-item.active .step-dot {
  background: #4db6ac;
  box-shadow: 0 0 8px rgba(77, 182, 172, 0.5);
}

@media (max-width: 768px) {
  .chat-left {
    max-width: none;
    min-width: 0;
  }

  .chat-body {
    flex-direction: column;
  }
}
</style>

<style>
/* ===== 全局 Markdown 渲染样式（非 scoped，因为 v-html 插入的内容不受 scoped 影响） ===== */
.markdown-body {
  font-size: 14px;
  line-height: 1.7;
  color: #24292e;
  word-wrap: break-word;
}

/* 标题 */
.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
  line-height: 1.3;
  color: #1a1a2e;
}
.markdown-body h1 { font-size: 20px; }
.markdown-body h2 { font-size: 18px; }
.markdown-body h3 { font-size: 16px; }
.markdown-body h4 { font-size: 15px; }

/* 段落 */
.markdown-body p {
  margin-top: 0;
  margin-bottom: 10px;
}

/* 列表 */
.markdown-body ul,
.markdown-body ol {
  padding-left: 22px;
  margin-top: 0;
  margin-bottom: 10px;
}
.markdown-body li {
  margin-bottom: 4px;
}

/* 引用 */
.markdown-body blockquote {
  padding: 6px 14px;
  margin: 0 0 10px 0;
  border-left: 4px solid #1890ff;
  background: #f6f8fa;
  color: #57606a;
}
.markdown-body blockquote p:last-child {
  margin-bottom: 0;
}

/* 行内代码 */
.markdown-body code {
  padding: 2px 6px;
  margin: 0;
  font-size: 13px;
  font-family: 'Menlo', 'Monaco', 'Consolas', 'Courier New', monospace;
  background: rgba(175, 184, 193, 0.2);
  border-radius: 4px;
  color: #cf222e;
}

/* 代码块 - highlight.js 渲染的 <pre><code> 容器 */
.markdown-body pre {
  margin: 10px 0;
  padding: 0;
  border-radius: 8px;
  overflow-x: auto;
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
  position: relative;
}
.markdown-body pre code {
  display: block;
  padding: 14px 16px;
  font-size: 13px;
  font-family: 'Menlo', 'Monaco', 'Consolas', 'Courier New', monospace;
  line-height: 1.5;
  color: #24292e;
  background: transparent;
  border-radius: 0;
  overflow-x: auto;
  white-space: pre;
  word-wrap: normal;
}

/* 链接 */
.markdown-body a {
  color: #0969da;
  text-decoration: none;
}
.markdown-body a:hover {
  text-decoration: underline;
}

/* 表格 */
.markdown-body table {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
  font-size: 13px;
}
.markdown-body table th,
.markdown-body table td {
  padding: 8px 12px;
  border: 1px solid #d0d7de;
  text-align: left;
}
.markdown-body table th {
  background: #f6f8fa;
  font-weight: 600;
}
.markdown-body table tr:nth-child(even) {
  background: #fafbfc;
}

/* 水平线 */
.markdown-body hr {
  height: 1px;
  margin: 16px 0;
  background: #d0d7de;
  border: none;
}

/* 图片 */
.markdown-body img {
  max-width: 100%;
  border-radius: 6px;
}

/* 加粗 */
.markdown-body strong {
  font-weight: 600;
  color: #1a1a2e;
}
</style>
