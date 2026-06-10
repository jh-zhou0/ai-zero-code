<template>
  <div class="app-preview">
    <div class="preview-header">
      <div class="preview-header-left">
        <eye-outlined />
        预览效果
      </div>
      <a-button
        v-if="deployKey"
        type="link"
        size="small"
        class="deploy-link-btn"
        @click="handleViewDeploy"
      >
        <link-outlined />
        查看部署
      </a-button>
    </div>
    <div class="preview-iframe-wrapper">
      <iframe
        v-if="previewUrl"
        :src="previewUrl"
        class="preview-iframe"
        frameborder="0"
        sandbox="allow-scripts allow-same-origin"
      />
      <div v-else class="preview-empty">
        <a-empty :description="emptyText" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { EyeOutlined, LinkOutlined } from '@ant-design/icons-vue'
import { buildDeployUrl, buildPreviewUrl } from '@/config/appConfig'

const props = withDefaults(defineProps<{
  /** 部署 key：已部署的应用使用此 key */
  deployKey?: string
  /** 应用 ID：用于构造未部署应用的预览地址（需要 codeGenType 配合） */
  appId?: string | number
  /** 代码生成类型：如 html、multi_file，用于构造未部署应用的预览地址 */
  codeGenType?: string
  emptyText?: string
}>(), {
  deployKey: '',
  appId: '',
  codeGenType: '',
  emptyText: '暂无预览',
})

/**
 * 计算预览地址：
 * 1. 有 deployKey → 已部署，直接使用
 * 2. 没有 deployKey 但有 appId + codeGenType → 已生成未部署，构造 {codeGenType}_{appId}
 * 3. 都没有 → 显示空状态
 */
const previewUrl = computed(() => {
  if (props.deployKey) {
    return buildDeployUrl(props.deployKey)
  }
  if (props.appId && props.codeGenType) {
    return buildPreviewUrl(props.codeGenType, props.appId)
  }
  return ''
})

function handleViewDeploy() {
  if (props.deployKey) {
    const url = buildDeployUrl(props.deployKey)
    window.open(url, '_blank')
  }
}
</script>

<style scoped>
.app-preview {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.preview-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.deploy-link-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.preview-iframe-wrapper {
  flex: 1;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
}

.preview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>
