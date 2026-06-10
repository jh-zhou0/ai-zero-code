<template>
  <div class="chat-container">
    <!-- 顶部栏 -->
    <div class="chat-header">
      <div class="header-left">
        <a-button type="text" class="back-btn" @click="goBack">
          <arrow-left-outlined />
        </a-button>
        <span class="app-name">{{ appInfo?.appName || '应用加载中...' }}</span>
      </div>
      <div class="header-right">
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
          <div
            v-for="(msg, index) in messages"
            :key="index"
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
              <div class="message-bubble" v-html="renderedMsg(msg.content)"></div>
            </div>
          </div>
          <div v-if="aiThinking" class="message-item message-ai">
            <div class="message-avatar">
              <a-avatar :size="36" style="backgroundColor: #52c41a;">AI</a-avatar>
            </div>
            <div class="message-content">
              <div class="message-bubble thinking-dots">
                <a-spin size="small" />
                <span>AI 正在生成代码...</span>
              </div>
            </div>
          </div>
        </div>
        <div class="chat-input-area">
          <a-textarea
            v-model:value="userInput"
            placeholder="输入消息..."
            :rows="2"
            :maxLength="2000"
            @press-enter="handleSend"
            class="chat-textarea"
            :disabled="aiThinking"
          />
          <a-button
            type="primary"
            :loading="aiThinking"
            :disabled="!userInput.trim()"
            @click="handleSend"
            class="send-btn"
          >
            发送
          </a-button>
        </div>
      </div>

      <!-- 右侧：网页预览区域（始终显示） -->
      <div class="chat-right">
        <!-- 代码已生成完成 → 展示预览 -->
        <AppPreview
          v-if="hasGeneratedCode && codeGenType"
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
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, CloudUploadOutlined, EditOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { getAppVoById, deployApp } from '@/api/appController'
import AppPreview from '@/components/AppPreview.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/**
 * 从路由参数获取应用 ID。
 * 保持为字符串避免大整数精度丢失（后端返回的 ID 可能是 string 类型）。
 */
const appIdStr = computed(() => route.params.id as string)
const appInfo = ref<API.AppVO | null>(null)

// 聊天
const messages = ref<{ role: string; content: string }[]>([])
const userInput = ref('')
const aiThinking = ref(false)
const messageListRef = ref<HTMLElement | null>(null)
// 预览 - codeGenType 在应用创建时就有，不能用于判断代码是否已生成
const codeGenType = ref('')
const hasInitPrompt = ref(false)
const deployKey = ref('')
/** 标记 SSE 流式调用是否已完成，即代码是否真正生成完毕 */
const hasGeneratedCode = ref(false)

// 部署
const deploying = ref(false)

/**
 * 渲染消息内容，将换行转为 <br>
 */
function renderedMsg(content: string) {
  return content.replace(/\n/g, '<br/>')
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
 * 使用 EventSource 发送 SSE 请求
 */
function sendMessage(messageText: string) {
  // 添加用户消息
  messages.value.push({ role: 'user', content: messageText })
  userInput.value = ''
  aiThinking.value = true

  // 创建一个 AI 消息占位
  messages.value.push({ role: 'ai', content: '' })

  // 使用 EventSource 连接 SSE
  const baseUrl = 'http://localhost:8123/api'
  const url = `${baseUrl}/app/chat/gen/code?appId=${appIdStr.value}&message=${encodeURIComponent(messageText)}`

  const eventSource = new EventSource(url, { withCredentials: true })

  // 处理普通消息事件 - 后端每个 chunk 包装为 { "d": "chunk内容" } 格式
  eventSource.onmessage = (event) => {
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
    eventSource.close()
    if (aiThinking.value) {
      // 刷新应用信息
      await loadAppInfo()
      hasGeneratedCode.value = true
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
 * 部署应用
 */
async function handleDeploy() {
  if (!appIdStr.value) return
  deploying.value = true
  try {
    const res = await deployApp({ appId: appIdStr.value as unknown as number })
    if (res.data?.code === 0 && res.data?.data) {
      message.success(`部署成功！访问地址：${res.data.data}`)
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
  // 区分两种进入方式：
  // 1. 从首页"开始生成"进入：URL 带 ?new=true → 自动调用 AI 生成
  // 2. 从"查看对话"进入：不带 ?new=true → 仅展示已有内容，不触发 AI 调用
  if (route.query.new === 'true' && appInfo.value?.initPrompt) {
    hasInitPrompt.value = true
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
  padding: 12px 24px;
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
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #f0f0f0;
  min-width: 360px;
  max-width: 600px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fafafa;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-bubble {
  max-width: 480px;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
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
  gap: 8px;
  color: #999;
}

.chat-input-area {
  display: flex;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  gap: 12px;
  align-items: flex-end;
}

.chat-textarea {
  flex: 1;
  border-radius: 8px;
  resize: none;
}

.send-btn {
  height: 44px;
  border-radius: 8px;
  flex-shrink: 0;
}

/* ===== 右侧：网页预览区域 ===== */
.chat-right {
  flex: 1;
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
