<script setup lang="ts">
import { ref, computed } from 'vue'
import { marked } from 'marked'
import { NButton, NIcon, NModal } from 'naive-ui'
import { FileTrayOutline, PrintOutline, CloseOutline } from '@vicons/ionicons5'

const props = defineProps<{
  modelValue?: string
  title?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// Configure marked
marked.setOptions({
  breaks: true,
  gfm: true
})

const content = ref(props.modelValue || '')
const showModal = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

const renderedHtml = computed(() => {
  if (!content.value) return ''
  return marked.parse(content.value) as string
})

function openFile() {
  fileInputRef.value?.click()
}

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    content.value = e.target?.result as string
    emit('update:modelValue', content.value)
  }
  reader.readAsText(file, 'UTF-8')

  // Reset input so same file can be selected again
  target.value = ''
}

function handlePrint() {
  const printContent = document.getElementById('markdown-print-area')
  if (!printContent) return

  const printWindow = window.open('', '_blank')
  if (!printWindow) return

  printWindow.document.write(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>${props.title || 'Markdown Preview'}</title>
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: "PingFang SC", "Microsoft YaHei", sans-serif; padding: 20px; }
        .markdown-body { font-size: 15px; line-height: 1.8; }
        .markdown-body h1 { font-size: 24px; text-align: center; margin-bottom: 30px; padding-bottom: 15px; border-bottom: 2px solid #333; }
        .markdown-body h2 { font-size: 18px; margin-top: 30px; margin-bottom: 15px; padding-bottom: 8px; border-bottom: 1px solid #ddd; }
        .markdown-body h3 { font-size: 16px; margin-top: 20px; margin-bottom: 10px; }
        .markdown-body p { text-indent: 2em; margin-bottom: 12px; }
        .markdown-body table { width: 100%; border-collapse: collapse; margin: 15px 0; }
        .markdown-body th, .markdown-body td { border: 1px solid #333; padding: 8px 12px; text-align: left; font-size: 14px; }
        .markdown-body th { background: #f5f5f5; }
        .markdown-body pre { background: #f5f5f5; padding: 15px; border-radius: 4px; overflow-x: auto; font-size: 13px; }
        .markdown-body code { background: #f5f5f5; padding: 2px 6px; border-radius: 3px; font-family: "Consolas", monospace; font-size: 13px; }
        .markdown-body ul, .markdown-body ol { margin: 10px 0 10px 20px; }
        .markdown-body li { margin: 5px 0; }
        .markdown-body blockquote { border-left: 4px solid #ddd; padding-left: 15px; margin: 15px 0; color: #666; }
        .markdown-body hr { border: none; border-top: 1px solid #ddd; margin: 20px 0; }
        @media print {
          body { background: white; padding: 0; }
          img { max-width: 100% !important; page-break-inside: avoid; }
          h1, h2, h3, h4, h5, h6 { page-break-after: avoid; }
          pre, code { white-space: pre-wrap; word-wrap: break-word; }
        }
      </style>
    </head>
    <body>
      <div class="markdown-body">${renderedHtml.value}</div>
      <script>window.onload = function() { window.print(); window.close(); }<\/script>
    </body>
    </html>
  `)
  printWindow.document.close()
}

function clearContent() {
  content.value = ''
  emit('update:modelValue', '')
}

function openPreview() {
  showModal.value = true
}

function closePreview() {
  showModal.value = false
}
</script>

<template>
  <div class="markdown-viewer">
    <!-- Toolbar -->
    <div class="viewer-toolbar">
      <NButton quaternary @click="openFile">
        <template #icon>
          <NIcon><FileTrayOutline /></NIcon>
        </template>
        选择MD文件
      </NButton>
      <NButton quaternary :disabled="!content" @click="openPreview">
        预览
      </NButton>
      <NButton quaternary :disabled="!content" @click="handlePrint">
        <template #icon>
          <NIcon><PrintOutline /></NIcon>
        </template>
        打印
      </NButton>
      <NButton quaternary :disabled="!content" @click="clearContent">
        <template #icon>
          <NIcon><CloseOutline /></NIcon>
        </template>
        清空
      </NButton>
    </div>

    <!-- Hidden file input -->
    <input
      ref="fileInputRef"
      type="file"
      accept=".md,.markdown"
      style="display: none"
      @change="handleFileChange"
    />

    <!-- Content display (when content is provided via prop) -->
    <div v-if="content" class="viewer-content">
      <div class="markdown-body" v-html="renderedHtml"></div>
    </div>

    <!-- Empty state -->
    <div v-else class="viewer-empty">
      <p>请选择要预览的Markdown文件</p>
      <p style="margin-top: 10px; font-size: 13px; color: #999;">
        点击上方"选择MD文件"按钮
      </p>
    </div>

    <!-- Preview Modal -->
    <NModal
      v-model:show="showModal"
      preset="card"
      :title="title || 'Markdown 预览'"
      style="width: 900px; max-width: 95vw;"
    >
      <div id="markdown-print-area" class="markdown-body" v-html="renderedHtml"></div>
      <template #footer>
        <div class="modal-footer">
          <NButton @click="closePreview">关闭</NButton>
          <NButton type="primary" @click="handlePrint">
            <template #icon>
              <NIcon><PrintOutline /></NIcon>
            </template>
            打印
          </NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>

<style scoped lang="scss">
.markdown-viewer {
  width: 100%;
}

.viewer-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px;
  background: var(--n-color);
  border-radius: 8px;
  border: 1px solid var(--n-border-color);
}

.viewer-content {
  background: var(--n-color);
  border-radius: 8px;
  padding: 20px;
  border: 1px solid var(--n-border-color);
  min-height: 200px;
}

.viewer-empty {
  text-align: center;
  padding: 50px;
  color: var(--n-text-color-3);
  background: var(--n-color);
  border-radius: 8px;
  border: 1px solid var(--n-border-color);
}

.markdown-body {
  font-size: 15px;
  line-height: 1.8;

  :deep(h1) {
    font-size: 24px;
    text-align: center;
    margin-bottom: 30px;
    padding-bottom: 15px;
    border-bottom: 2px solid #333;
  }

  :deep(h2) {
    font-size: 18px;
    margin-top: 30px;
    margin-bottom: 15px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ddd;
  }

  :deep(h3) {
    font-size: 16px;
    margin-top: 20px;
    margin-bottom: 10px;
  }

  :deep(p) {
    text-indent: 2em;
    margin-bottom: 12px;
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 15px 0;
  }

  :deep(th),
  :deep(td) {
    border: 1px solid #333;
    padding: 8px 12px;
    text-align: left;
    font-size: 14px;
  }

  :deep(th) {
    background: #f5f5f5;
  }

  :deep(pre) {
    background: #f5f5f5;
    padding: 15px;
    border-radius: 4px;
    overflow-x: auto;
    font-size: 13px;
  }

  :deep(code) {
    background: #f5f5f5;
    padding: 2px 6px;
    border-radius: 3px;
    font-family: "Consolas", "Monaco", monospace;
    font-size: 13px;
  }

  :deep(ul),
  :deep(ol) {
    margin: 10px 0 10px 20px;
  }

  :deep(li) {
    margin: 5px 0;
  }

  :deep(blockquote) {
    border-left: 4px solid #ddd;
    padding-left: 15px;
    margin: 15px 0;
    color: #666;
  }

  :deep(hr) {
    border: none;
    border-top: 1px solid #ddd;
    margin: 20px 0;
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
  }
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
