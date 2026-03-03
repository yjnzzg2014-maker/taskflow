<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import MarkdownViewer from '@/components/MarkdownViewer.vue'

const { t } = useI18n()
</script>

<template>
  <div class="markdown-tool-page">
    <h1 class="page-title">{{ t('nav.tools') }} - {{ t('markdown.title') }}</h1>
    <MarkdownViewer :title="t('markdown.preview')" />
  </div>
</template>

<style scoped lang="scss">
.markdown-tool-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  margin-bottom: 20px;
}
</style>
