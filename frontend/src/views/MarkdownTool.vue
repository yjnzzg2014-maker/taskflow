<script setup lang="ts">
import MarkdownViewer from '@/components/MarkdownViewer.vue'
</script>

<template>
  <div class="markdown-tool-page">
    <h1 class="page-title">常用工具 - Markdown 预览</h1>
    <MarkdownViewer title="Markdown 预览打印" />
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
