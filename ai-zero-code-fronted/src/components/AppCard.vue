<template>
  <a-card
    hoverable
    class="app-card"
    @click="handleCardClick"
  >
    <template #cover>
      <div class="app-card-cover">
        <img
          v-if="app.cover"
          :src="app.cover"
          alt="cover"
          class="app-cover-img"
        />
        <img
          v-else
          src="@/assets/logo.svg"
          alt="default"
          class="app-cover-img default-cover"
        />
        <!-- 悬浮遮罩层 -->
        <div v-if="$slots.overlay" class="cover-overlay">
          <slot name="overlay"></slot>
        </div>
      </div>
    </template>
    <div class="card-body">
      <div class="card-left">
        <a-avatar
          :size="48"
          :src="app.user?.userAvatar"
          style="background-color: #1890ff;"
        >
          {{ (app.user?.userName || app.user?.userAccount || 'U').charAt(0) }}
        </a-avatar>
      </div>
      <div class="card-right">
        <div class="card-title">{{ app.appName || '未命名应用' }}</div>
        <div class="card-creator">
          <span class="creator-name">{{ app.user?.userName || app.user?.userAccount || '未知用户' }}</span>
          <div class="deploy-tag">
            <a-tag v-if="app.deployKey" color="green" size="small">已部署</a-tag>
            <a-tag v-else color="default" size="small">未部署</a-tag>
          </div>
        </div>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
const props = defineProps<{
  /** 应用数据 */
  app: API.AppVO
}>()

const emit = defineEmits<{
  /** 卡片点击事件 */
  click: [app: API.AppVO]
}>()

function handleCardClick() {
  emit('click', props.app)
}
</script>

<style scoped>
.app-card {
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.app-card-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0f7fa, #e8f5e9);
}

.app-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  width: 64px;
  height: 64px;
  opacity: 0.3;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0);
  transition: background 0.3s ease;
}

.app-card-cover:hover .cover-overlay {
  background: rgba(0, 0, 0, 0.45);
}

.card-body {
  display: flex;
  gap: 12px;
  padding: 4px 0;
}

.card-left {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
}

.card-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-creator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
  color: #666;
}

.creator-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.deploy-tag {
  flex-shrink: 0;
}
</style>
