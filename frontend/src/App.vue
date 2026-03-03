<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { NMessageProvider, NDialogProvider, NNotificationProvider, NConfigProvider, darkTheme, lightTheme } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import NotificationService from '@/components/NotificationService.vue'

const authStore = useAuthStore()

const theme = ref(lightTheme)

// Initialize theme
function initTheme() {
  if (authStore.isDark) {
    theme.value = darkTheme
    document.documentElement.classList.add('dark')
  } else {
    theme.value = lightTheme
    document.documentElement.classList.remove('dark')
  }
}

// Watch for dark mode changes
watch(
  () => authStore.isDark,
  (newValue) => {
    if (newValue) {
      theme.value = darkTheme
      document.documentElement.classList.add('dark')
    } else {
      theme.value = lightTheme
      document.documentElement.classList.remove('dark')
    }
  }
)

onMounted(() => {
  initTheme()
})
</script>

<template>
  <NConfigProvider :theme="theme">
    <NMessageProvider>
      <NDialogProvider>
        <NNotificationProvider>
          <NotificationService />
          <RouterView />
        </NNotificationProvider>
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>

<style>
/* Global dark mode styles */
html.dark {
  color-scheme: dark;
}

html.dark body {
  background-color: #18181c;
  color: #fff;
}
</style>
