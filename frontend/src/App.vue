<script setup lang="ts">
import { RouterView } from 'vue-router'
import { NMessageProvider, NDialogProvider, NNotificationProvider } from 'naive-ui'
</script>

<template>
  <NMessageProvider>
    <NDialogProvider>
      <NNotificationProvider>
        <RouterView />
      </NNotificationProvider>
    </NDialogProvider>
  </NMessageProvider>
</template>

<style scoped>
</style>
