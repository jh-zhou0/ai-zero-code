<template>
  <div class="app-manage-container">
    <a-card title="应用管理">
      <!-- 搜索表单 -->
      <a-form :model="searchParams" layout="inline" style="margin-bottom: 16px">
        <a-form-item label="应用名称">
          <a-input
            v-model:value="searchParams.appName"
            placeholder="请输入应用名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="创建用户ID">
          <a-input-number
            v-model:value="searchParams.userId"
            placeholder="用户ID"
            allow-clear
            style="width: 120px"
          />
        </a-form-item>
        <a-form-item label="生成类型">
          <a-select
            v-model:value="searchParams.codeGenType"
            placeholder="生成类型"
            allow-clear
            style="width: 160px"
            :options="codeGenTypeOptions"
          />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number
            v-model:value="searchParams.priority"
            placeholder="优先级"
            allow-clear
            style="width: 100px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="doSearch">搜索</a-button>
            <a-button @click="doReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 应用表格 -->
      <a-table
        :columns="columns"
        :data-source="appList"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        row-key="id"
        scroll="{ x: 1400 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'cover'">
            <a-avatar
              :src="record.cover || undefined"
              shape="square"
              :size="48"
            >
              <template v-if="!record.cover">
                <img src="@/assets/logo.svg" style="opacity: 0.3; width: 24px; height: 24px;" />
              </template>
            </a-avatar>
          </template>
          <template v-if="column.key === 'priority'">
            <a-tag :color="record.priority && record.priority >= 99 ? 'gold' : 'default'">
              {{ record.priority ?? 0 }}
            </a-tag>
          </template>
          <template v-if="column.key === 'codeGenType'">
            <a-tag v-if="record.codeGenType" color="blue">{{ getCodeGenTypeLabel(record.codeGenType) }}</a-tag>
            <span v-else class="no-data">-</span>
          </template>
          <template v-if="column.key === 'deployKey'">
            <a-tag v-if="record.deployKey" color="green">已部署</a-tag>
            <a-tag v-else color="default">未部署</a-tag>
          </template>
          <template v-if="column.key === 'userName'">
            {{ record.user?.userName || record.user?.userAccount || '-' }}
          </template>
          <template v-if="column.key === 'createTime'">
            {{ record.createTime?.slice(0, 16) || '-' }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="handleViewChat(record)">查看会话</a-button>
              <a-button type="link" @click="handlePreview(record)">预览</a-button>
              <a-button type="link" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" @click="handleFeature(record)">精选</a-button>
              <a-button type="link" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 编辑应用弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      title="编辑应用"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        ref="modalFormRef"
        :model="modalForm"
        :rules="modalRules"
        layout="vertical"
      >
        <a-form-item label="应用名称" name="appName">
          <a-input v-model:value="modalForm.appName" placeholder="请输入应用名称" />
        </a-form-item>
        <a-form-item label="应用封面" name="cover">
          <a-input v-model:value="modalForm.cover" placeholder="请输入封面 URL" />
        </a-form-item>
        <a-form-item label="优先级" name="priority">
          <a-input-number
            v-model:value="modalForm.priority"
            placeholder="优先级"
            :min="0"
            :max="999"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  listAppVoByPageByAdmin,
  deleteAppByAdmin,
  updateAppByAdmin,
} from '@/api/appController'
import { CodeGenTypeEnum, getCodeGenTypeLabel } from '@/constants/codeGenType'
import { buildDeployUrl, buildPreviewUrl } from '@/config/appConfig'
import type { TablePaginationConfig, SelectProps } from 'ant-design-vue'

/** 生成类型下拉选项，value 是枚举值，label 是中文描述 */
const codeGenTypeOptions: SelectProps['options'] = Object.values(CodeGenTypeEnum).map((item) => ({
  value: item.value,
  label: item.label,
}))

const loading = ref(false)
const appList = ref<API.AppVO[]>([])

const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  appName: undefined,
  userId: undefined,
  codeGenType: undefined,
  priority: undefined,
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
  { title: '封面', key: 'cover', width: 70 },
  { title: '应用名称', dataIndex: 'appName', key: 'appName', width: 160, ellipsis: true },
  { title: '初始提示词', dataIndex: 'initPrompt', key: 'initPrompt', width: 200, ellipsis: true },
  { title: '生成类型', key: 'codeGenType', width: 100 },
  { title: '部署状态', key: 'deployKey', width: 90 },
  { title: '优先级', key: 'priority', width: 80 },
  { title: '创建用户', key: 'userName', width: 120 },
  { title: '创建时间', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 320, fixed: 'right' },
]

// ====== 弹窗相关 ======
const modalVisible = ref(false)
const modalLoading = ref(false)
const editingRecord = ref<API.AppVO | null>(null)
const modalFormRef = ref()

const modalForm = reactive<{
  appName?: string
  cover?: string
  priority?: number
}>({
  appName: undefined,
  cover: undefined,
  priority: undefined,
})

const modalRules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
}

function handleEdit(record: API.AppVO) {
  editingRecord.value = record
  modalForm.appName = record.appName
  modalForm.cover = record.cover
  modalForm.priority = record.priority
  modalVisible.value = true
}

function handleFeature(record: API.AppVO) {
  Modal.confirm({
    title: '确认精选',
    content: `确定要将应用"${record.appName || '未命名'}"设为精选吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await updateAppByAdmin({
          id: record.id,
          appName: record.appName,
          cover: record.cover,
          priority: 99,
        })
        if (res.data?.code === 0) {
          message.success('设置精选成功')
          await loadAppList()
        } else {
          message.error(res.data?.message || '操作失败')
        }
      } catch {
        message.error('操作失败')
      }
    },
  })
}

async function handleModalOk() {
  try {
    await modalFormRef.value?.validate()
  } catch {
    return
  }

  modalLoading.value = true
  try {
    const res = await updateAppByAdmin({
      id: editingRecord.value?.id,
      appName: modalForm.appName,
      cover: modalForm.cover || undefined,
      priority: modalForm.priority,
    })
    if (res.data?.code === 0) {
      message.success('修改成功')
      modalVisible.value = false
      await loadAppList()
    } else {
      message.error(res.data?.message || '修改失败')
    }
  } catch {
    message.error('修改失败')
  } finally {
    modalLoading.value = false
  }
}

function handleModalCancel() {
  modalVisible.value = false
}

// ====== 数据加载 ======

async function loadAppList() {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'descend',
      ...searchParams,
    })
    if (res.data?.code === 0 && res.data?.data) {
      appList.value = res.data.data.records || []
      pagination.total = res.data.data.totalRow || 0
    } else {
      message.error(res.data?.message || '获取应用列表失败')
    }
  } catch {
    message.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

function doSearch() {
  pagination.current = 1
  loadAppList()
}

function doReset() {
  searchParams.appName = undefined
  searchParams.userId = undefined
  searchParams.codeGenType = undefined
  searchParams.priority = undefined
  pagination.current = 1
  loadAppList()
}

function handleTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadAppList()
}

function handleDelete(record: API.AppVO) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用"${record.appName || '未命名'}"吗？此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteAppByAdmin({ id: record.id })
        if (res.data?.code === 0) {
          message.success('删除成功')
          await loadAppList()
        } else {
          message.error(res.data?.message || '删除失败')
        }
      } catch {
        message.error('删除失败')
      }
    },
  })
}

/**
 * 查看会话：在新窗口打开应用的聊天页面
 */
function handleViewChat(record: API.AppVO) {
  if (!record.id) return
  const url = `${window.location.origin}/app/chat/${record.id}`
  window.open(url, '_blank')
}

/**
 * 预览应用：在新窗口打开预览页面
 */
function handlePreview(record: API.AppVO) {
  let url = ''
  // 优先使用已部署的地址
  if (record.deployKey) {
    url = buildDeployUrl(record.deployKey)
  } else if (record.codeGenType && record.id) {
    // 已生成但未部署，使用静态资源地址
    url = buildPreviewUrl(record.codeGenType, record.id)
  }

  if (url) {
    window.open(url, '_blank')
  } else {
    message.warning('该应用尚未生成代码，无法预览')
  }
}

onMounted(() => {
  loadAppList()
})
</script>

<style scoped>
.app-manage-container {
  padding: 0;
}

.no-data {
  color: #999;
}
</style>
