<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()

const languages = [
  { label: '中文', value: 'zh' },
  { label: 'English', value: 'en' }
]

function changeLanguage(lang: string) {
  locale.value = lang
  localStorage.setItem('locale', lang)
}
</script>

<template>
  <n-dropdown :options="languages" @select="changeLanguage">
    <n-button quaternary>
      <span>{{ languages.find(l => l.value === locale)?.label }}</span>
    </n-button>
  </n-dropdown>
</template>

<style scoped lang="scss">
</style>
