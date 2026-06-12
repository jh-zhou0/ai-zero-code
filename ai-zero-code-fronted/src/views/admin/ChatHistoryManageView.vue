<template>
  <div class="chat-history-manage-container">
    <a-card title="对话管理">
      <!-- 搜索表单 -->
      <a-form :model="searchParams" layout="inline" style="margin-bottom: 16px">
        <a-form-item label="应用ID">
          <a-input-number
            v-model:value="searchParams.appId"
            placeholder="应用ID"
            allow-clear
            style="width: 120px"
          />
        </a-form-item>
        <a-form-item label="用户ID">
          <a-input-number
            v-model:value="searchParams.userId"
            placeholder="用户ID"
            allow-clear
            style="width: 120px"
          />
        </a-form-item>
        <a-form-item label="消息类型">
          <a-select
            v-model:value="searchParams.messageType"
            placeholder="消息类型"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="user">用户</a-select-option>
            <a-select-option value="ai">AI</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="doSearch">搜索</a-button>
            <a-button @click="doReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 对话历史表格 -->
      <a-table
        :columns="columns"
        :data-source="chatHistoryList"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        row-key="id"
        scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'messageType'">
            <a-tag v-if="record.messageType === 'user'" color="blue">用户</a-tag>
            <a-tag v-else-if="record.messageType === 'ai'" color="green">AI</a-tag>
            <span v-else class="no-data">{{ record.messageType }}</span>
          </template>
          <template v-if="column.key === 'message'">
            <a-tooltip placement="topLeft" :title="record.message" :overlay-style="{ maxWidth: '400px' }">
              <span class="message-cell">{{ record.message?.slice(0, 80) || '-' }}{{ record.message?.length > 80 ? '...' : '' }}</span>
            </a-tooltip>
          </template>
          <template v-if="column.key === 'createTime'">
            {{ record.createTime?.slice(0, 16) || '-' }}
          </template>
          <template v-if="column.key === 'editTime'">
            {{ record.editTime?.slice(0, 16) || '-' }}
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'
import type { TablePaginationConfig } from 'ant-design-vue'

const loading = ref(false)
const chatHistoryList = ref<API.ChatHistory[]>([])

const searchParams = reactive<API.ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  appId: undefined,
  userId: undefined,
  messageType: undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '消息内容', key: 'message', width: 300, ellipsis: true },
  { title: '消息类型', key: 'messageType', width: 90 },
  { title: '应用ID', dataIndex: 'appId', key: 'appId', width: 100 },
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: '创建时间', key: 'createTime', width: 160 },
  { title: '编辑时间', key: 'editTime', width: 160 },
]

// ====== 数据加载 ======

async function loadChatHistoryList() {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'descend',
      ...searchParams,
    })
    if (res.data?.code === 0 && res.data?.data) {
      chatHistoryList.value = res.data.data.records || []
      pagination.total = res.data.data.totalRow || 0
    } else {
      message.error(res.data?.message || '获取对话历史列表失败')
    }
  } catch {
    message.error('获取对话历史列表失败')
  } finally {
    loading.value = false
  }
}

function doSearch() {
  pagination.current = 1
  loadChatHistoryList()
}

function doReset() {
  searchParams.appId = undefined
  searchParams.userId = undefined
  searchParams.messageType = undefined
  pagination.current = 1
  loadChatHistoryList()
}

function handleTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadChatHistoryList()
}

onMounted(() => {
  loadChatHistoryList()
})
</script>

<style scoped>
.chat-history-manage-container {
  padding: 0;
}

.no-data {
  color: #999;
}

.message-cell {
  display: inline-block;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
