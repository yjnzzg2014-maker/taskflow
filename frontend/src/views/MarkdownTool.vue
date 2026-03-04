<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import MarkdownViewer from '@/components/MarkdownViewer.vue'
import DataExport from '@/components/DataExport.vue'
import TagsManager from '@/components/TagsManager.vue'
import { DocumentText, Calendar, Pricetags } from '@vicons/ionicons5'

const { t } = useI18n()
const dataExportRef = ref()

const tools = [
  {
    id: 'markdown',
    name: 'markdown.title',
    icon: DocumentText,
    description: 'markdown.desc'
  },
  {
    id: 'export',
    name: 'export.title',
    icon: Calendar,
    description: 'export.description'
  },
  {
    id: 'tags',
    name: 'tags.categories',
    icon: Pricetags,
    description: 'tags.categories'
  }
]

const currentTool = ref<string | null>(null)

function selectTool(toolId: string) {
  currentTool.value = toolId
  if (toolId === 'export') {
    dataExportRef.value?.openExportModal()
    currentTool.value = 'markdown'
  }
}
</script>

<template>
  <div class="tools-page">
    <h1 class="page-title">{{ t('nav.tools') }}</h1>

    <div class="tools-grid">
      <n-card
        v-for="tool in tools"
        :key="tool.id"
        hoverable
        class="tool-card"
        :class="{ active: currentTool === tool.id }"
        @click="selectTool(tool.id)"
      >
        <div class="tool-icon">
          <n-icon :component="tool.icon" :size="32" />
        </div>
        <div class="tool-info">
          <h3>{{ t(tool.name) }}</h3>
          <p>{{ t(tool.description) }}</p>
        </div>
      </n-card>
    </div>

    <div v-if="currentTool === 'markdown'" class="tool-content">
      <h2>{{ t('markdown.title') }}</h2>
      <MarkdownViewer :title="t('markdown.preview')" />
    </div>

    <div v-else-if="currentTool === 'tags'" class="tool-content">
      <h2>{{ t('tags.categories') }}</h2>
      <TagsManager />
    </div>

    <DataExport ref="dataExportRef" />
  </div>
</template>

<style scoped lang="scss">
.tools-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  margin-bottom: 20px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.tool-card {
  cursor: pointer;
  transition: all 0.2s;

  &.active {
    border-color: #18a058;
  }

  .tool-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60px;
    height: 60px;
    border-radius: 12px;
    background: rgba(24, 160, 88, 0.1);
    color: #18a058;
    margin-bottom: 12px;
  }

  .tool-info {
    h3 {
      margin: 0 0 4px;
      font-size: 16px;
    }

    p {
      margin: 0;
      font-size: 12px;
      color: var(--n-text-color-3);
    }
  }
}

.tool-content {
  h2 {
    margin-bottom: 16px;
  }
}

// 移动端适配
@media (max-width: 768px) {
  .tools-grid {
    grid-template-columns: 1fr;
  }
}
</style>
