<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { NMessageProvider, NDialogProvider, NNotificationProvider, NConfigProvider, darkTheme } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import NotificationService from '@/components/NotificationService.vue'

const authStore = useAuthStore()

const theme = computed(() => authStore.isDark ? darkTheme : null)

onMounted(() => {
  // Apply initial dark mode
  if (authStore.isDark) {
    document.documentElement.classList.add('dark')
  }
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
