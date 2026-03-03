<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'

const i18n = useI18n()

const languages = [
  { label: '中文', key: 'zh' },
  { label: 'English', key: 'en' }
]

const currentLabel = computed(() => {
  return languages.find(l => l.key === i18n.locale.value)?.label || '中文'
})

function changeLanguage(key: string) {
  i18n.locale.value = key
  localStorage.setItem('locale', key)
}
</script>

<template>
  <n-dropdown :options="languages" @select="changeLanguage">
    <n-button quaternary>
      <span>{{ currentLabel }}</span>
    </n-button>
  </n-dropdown>
</template>

<style scoped lang="scss">
</style>
