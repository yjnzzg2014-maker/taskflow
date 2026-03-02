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
  <el-dropdown trigger="click" @command="changeLanguage">
    <span class="language-switcher">
      <el-icon><Language /></el-icon>
      <span class="lang-label">{{ languages.find(l => l.value === locale)?.label }}</span>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="lang in languages"
          :key="lang.value"
          :command="lang.value"
          :class="{ active: locale === lang.value }"
        >
          {{ lang.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped lang="scss">
.language-switcher {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;

  &:hover {
    background: rgba(0, 0, 0, 0.05);

    .dark & {
      background: rgba(255, 255, 255, 0.1);
    }
  }

  .lang-label {
    font-size: 14px;
  }
}

:deep(.el-dropdown-menu__item.active) {
  color: var(--primary-color);
  font-weight: 600;
}
</style>
