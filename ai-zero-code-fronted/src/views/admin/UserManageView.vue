<template>
  <div class="user-manage-container">
    <a-card title="用户管理">
      <!-- 搜索表单 -->
      <a-form :model="searchParams" layout="inline" style="margin-bottom: 16px">
        <a-form-item label="账号">
          <a-input
            v-model:value="searchParams.userAccount"
            placeholder="请输入账号"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input
            v-model:value="searchParams.userName"
            placeholder="请输入用户名"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="角色">
          <a-select
            v-model:value="searchParams.userRole"
            placeholder="请选择角色"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="ban">封禁</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="doSearch">搜索</a-button>
            <a-button @click="doReset">重置</a-button>
            <a-button type="primary" @click="handleAdd">新增用户</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 用户表格 -->
      <a-table
        :columns="columns"
        :data-source="userList"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userAvatar'">
            <a-avatar :src="record.userAvatar" />
          </template>
          <template v-if="column.key === 'userRole'">
            <a-tag :color="record.userRole === 'admin' ? 'red' : record.userRole === 'ban' ? 'gray' : 'blue'">
              {{ record.userRole === 'admin' ? '管理员' : record.userRole === 'ban' ? '封禁' : '普通用户' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'createTime'">
            {{ record.createTime }}
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="handleDelete(record)">
              删除
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑用户弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEditMode ? '编辑用户' : '新增用户'"
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
        <a-form-item label="账号" name="userAccount" v-if="!isEditMode">
          <a-input v-model:value="modalForm.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item label="用户名" name="userName">
          <a-input v-model:value="modalForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="头像地址" name="userAvatar">
          <a-input v-model:value="modalForm.userAvatar" placeholder="请输入头像URL" />
        </a-form-item>
        <a-form-item label="简介" name="userProfile">
          <a-textarea v-model:value="modalForm.userProfile" placeholder="请输入简介" :rows="3" />
        </a-form-item>
        <a-form-item label="角色" name="userRole">
          <a-select v-model:value="modalForm.userRole" placeholder="请选择角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="ban">封禁</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { listUserVoByPage, deleteUsingPost, add, update } from '@/api/userController'
import type { TablePaginationConfig } from 'ant-design-vue'

const loading = ref(false)
const userList = ref<API.UserVO[]>([])

const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  userAccount: undefined,
  userName: undefined,
  userRole: undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 180 },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', width: 150 },
  { title: '用户名', dataIndex: 'userName', key: 'userName', width: 150 },
  { title: '头像', key: 'userAvatar', width: 80 },
  { title: '简介', dataIndex: 'userProfile', key: 'userProfile', ellipsis: true },
  { title: '角色', key: 'userRole', width: 100 },
  { title: '创建时间', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' },
]

// ====== 弹窗相关 ======
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEditMode = ref(false)
const editingId = ref<number | undefined>(undefined)
const modalFormRef = ref()

const modalForm = reactive<{
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
}>({
  userAccount: undefined,
  userName: undefined,
  userAvatar: undefined,
  userProfile: undefined,
  userRole: 'user',
})

const modalRules = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号长度不能少于4位', trigger: 'blur' },
  ],
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
}

/**
 * 打开新增用户弹窗
 */
function handleAdd() {
  isEditMode.value = false
  editingId.value = undefined
  modalForm.userAccount = undefined
  modalForm.userName = undefined
  modalForm.userAvatar = undefined
  modalForm.userProfile = undefined
  modalForm.userRole = 'user'
  modalVisible.value = true
}

/**
 * 打开编辑用户弹窗
 */
function handleEdit(record: API.UserVO) {
  isEditMode.value = true
  editingId.value = record.id
  modalForm.userAccount = undefined
  modalForm.userName = record.userName
  modalForm.userAvatar = record.userAvatar
  modalForm.userProfile = record.userProfile
  modalForm.userRole = record.userRole
  modalVisible.value = true
}

/**
 * 弹窗确认提交
 */
async function handleModalOk() {
  try {
    await modalFormRef.value?.validate()
  } catch {
    return
  }

  modalLoading.value = true
  try {
    if (isEditMode.value) {
      const res = await update({
        id: editingId.value,
        userName: modalForm.userName,
        userAvatar: modalForm.userAvatar,
        userProfile: modalForm.userProfile,
        userRole: modalForm.userRole,
      } as API.UserUpdateRequest)
      if (res.data?.code === 0) {
        message.success('修改成功')
        modalVisible.value = false
        await loadUserList()
      } else {
        message.error(res.data?.message || '修改失败')
      }
    } else {
      const res = await add({
        userAccount: modalForm.userAccount,
        userName: modalForm.userName,
        userAvatar: modalForm.userAvatar,
        userProfile: modalForm.userProfile,
        userRole: modalForm.userRole,
      } as API.UserAddRequest)
      if (res.data?.code === 0) {
        message.success('新增成功')
        modalVisible.value = false
        await loadUserList()
      } else {
        message.error(res.data?.message || '新增失败')
      }
    }
  } catch {
    message.error(isEditMode.value ? '修改失败' : '新增失败')
  } finally {
    modalLoading.value = false
  }
}

/**
 * 弹窗取消
 */
function handleModalCancel() {
  modalVisible.value = false
}

// ====== 原有逻辑 ======

/**
 * 加载用户列表
 */
async function loadUserList() {
  loading.value = true
  try {
    const res = await listUserVoByPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'descend',
      ...searchParams,
    })
    if (res.data?.code === 0 && res.data?.data) {
      userList.value = res.data.data.records || []
      pagination.total = Number(res.data.data.totalRow) || 0
    } else {
      message.error(res.data?.message || '获取用户列表失败')
    }
  } catch {
    message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
function doSearch() {
  pagination.current = 1
  loadUserList()
}

/**
 * 重置搜索条件
 */
function doReset() {
  searchParams.userAccount = undefined
  searchParams.userName = undefined
  searchParams.userRole = undefined
  pagination.current = 1
  loadUserList()
}

/**
 * 表格分页变化
 */
function handleTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadUserList()
}

/**
 * 删除用户
 */
async function handleDelete(record: API.UserVO) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户"${record.userName || record.userAccount}"吗？此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteUsingPost({ id: record.id })
        if (res.data?.code === 0) {
          message.success('删除成功')
          await loadUserList()
        } else {
          message.error(res.data?.message || '删除失败')
        }
      } catch {
        message.error('删除失败')
      }
    },
  })
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-manage-container {
  padding: 0;
}
</style>
