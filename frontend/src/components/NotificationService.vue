<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useEventStore } from '@/stores/event'
import { useTaskStore } from '@/stores/task'
import dayjs from 'dayjs'

const eventStore = useEventStore()
const taskStore = useTaskStore()

const notificationPermission = ref(Notification.permission)
let checkInterval: ReturnType<typeof setInterval> | null = null

async function requestPermission() {
  if (Notification.permission === 'granted') {
    notificationPermission.value = 'granted'
    return
  }

  if (Notification.permission !== 'denied') {
    const permission = await Notification.requestPermission()
    notificationPermission.value = permission
  }
}

function sendNotification(title: string, body: string, icon?: string) {
  if (Notification.permission === 'granted') {
    new Notification(title, {
      body,
      icon: icon || '/favicon.ico',
      tag: 'taskflow-notification'
    })
  }
}

function checkReminders() {
  const now = dayjs()

  // Check events
  eventStore.events.forEach(event => {
    if (event.remindAt) {
      const remindTime = dayjs(event.remindAt)
      const diff = remindTime.diff(now, 'minute')
      if (diff >= 0 && diff <= 1) {
        sendNotification(
          '日程提醒',
          event.title,
        )
        // Mark as reminded (in real app, would update backend)
      }
    }
  })

  // Check tasks
  taskStore.tasks.forEach(task => {
    if (task.dueDate && task.status !== 'COMPLETED') {
      const dueTime = dayjs(task.dueDate)
      const diff = dueTime.diff(now, 'hour')
      // Notify if due within 1 hour
      if (diff >= 0 && diff <= 1) {
        sendNotification(
          '任务到期提醒',
          task.title,
        )
      }
    }
  })
}

function startNotificationService() {
  // Request permission on mount
  requestPermission()

  // Check every minute
  checkInterval = setInterval(checkReminders, 60000)

  // Initial check
  checkReminders()
}

function stopNotificationService() {
  if (checkInterval) {
    clearInterval(checkInterval)
    checkInterval = null
  }
}

onMounted(() => {
  startNotificationService()
})

onUnmounted(() => {
  stopNotificationService()
})

defineExpose({
  requestPermission,
  permission: notificationPermission
})
</script>

<template>
  <div class="notification-service">
    <!-- Notification permission is handled automatically -->
  </div>
</template>

<style scoped>
.notification-service {
  display: none;
}
</style>
