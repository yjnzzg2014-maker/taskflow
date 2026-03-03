<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { useEventStore } from '@/stores/event'
import { useTaskStore } from '@/stores/task'
import { Download } from '@vicons/ionicons5'

const { t } = useI18n()
const message = useMessage()
const eventStore = useEventStore()
const taskStore = useTaskStore()

const showExportModal = ref(false)
const exporting = ref(false)

function openExportModal() {
  showExportModal.value = true
}

function closeModal() {
  showExportModal.value = false
}

function generateICalEvents() {
  const events = eventStore.events
  let ical = 'BEGIN:VCALENDAR\r\n'
  ical += 'VERSION:2.0\r\n'
  ical += 'PRODID:-//TaskFlow//EN\r\n'

  for (const event of events) {
    ical += 'BEGIN:VEVENT\r\n'
    ical += `UID:${event.id}@taskflow\r\n`
    ical += `SUMMARY:${event.title}\r\n`
    if (event.description) {
      ical += `DESCRIPTION:${event.description}\r\n`
    }
    if (event.location) {
      ical += `LOCATION:${event.location}\r\n`
    }
    if (event.startTime) {
      const start = new Date(event.startTime).toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '')
      ical += `DTSTART:${start}\r\n`
    }
    if (event.endTime) {
      const end = new Date(event.endTime).toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '')
      ical += `DTEND:${end}\r\n`
    }
    if (event.isAllDay) {
      ical += 'RRULE:FREQ=DAILY\r\n'
    }
    ical += 'END:VEVENT\r\n'
  }

  ical += 'END:VCALENDAR'
  return ical
}

function generateICalTasks() {
  const tasks = taskStore.tasks
  let ical = 'BEGIN:VCALENDAR\r\n'
  ical += 'VERSION:2.0\r\n'
  ical += 'PRODID:-//TaskFlow//EN\r\n'

  for (const task of tasks) {
    ical += 'BEGIN:VEVENT\r\n'
    ical += `UID:task-${task.id}@taskflow\r\n`
    ical += `SUMMARY:${task.title}\r\n`
    if (task.description) {
      ical += `DESCRIPTION:${task.description}\r\n`
    }
    if (task.dueDate) {
      const due = new Date(task.dueDate).toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '')
      ical += `DTSTART;VALUE=DATE:${due.slice(0, 8)}\r\n`
      ical += `DTEND;VALUE=DATE:${due.slice(0, 8)}\r\n`
    }
    ical += `STATUS:${task.status === 'COMPLETED' ? 'COMPLETED' : 'NEEDS-ACTION'}\r\n`
    ical += 'END:VEVENT\r\n'
  }

  ical += 'END:VCALENDAR'
  return ical
}

function downloadFile(content: string, filename: string, mimeType: string) {
  const blob = new Blob([content], { type: mimeType })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

async function exportCalendar() {
  exporting.value = true
  try {
    const ical = generateICalEvents()
    downloadFile(ical, 'taskflow-events.ics', 'text/calendar')
    message.success(t('export.success'))
    closeModal()
  } catch (e) {
    message.error(t('export.failed'))
  } finally {
    exporting.value = false
  }
}

async function exportTasks() {
  exporting.value = true
  try {
    const ical = generateICalTasks()
    downloadFile(ical, 'taskflow-tasks.ics', 'text/calendar')
    message.success(t('export.success'))
    closeModal()
  } catch (e) {
    message.error(t('export.failed'))
  } finally {
    exporting.value = false
  }
}

async function exportAll() {
  exporting.value = true
  try {
    const eventsIcal = generateICalEvents()
    const tasksIcal = generateICalTasks()
    const combined = 'BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//TaskFlow//EN\r\n' +
      eventsIcal.replace('BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//TaskFlow//EN\r\n', '') +
      tasksIcal.replace('BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//TaskFlow//EN\r\n', '')
    downloadFile(combined, 'taskflow-all.ics', 'text/calendar')
    message.success(t('export.success'))
    closeModal()
  } catch (e) {
    message.error(t('export.failed'))
  } finally {
    exporting.value = false
  }
}

defineExpose({ openExportModal })
</script>

<template>
  <n-modal v-model:show="showExportModal" :title="t('export.title')" preset="card" style="width: 400px">
    <div class="export-content">
      <p class="export-desc">{{ t('export.description') }}</p>

      <n-space vertical>
        <n-button block @click="exportCalendar">
          <template #icon>
            <n-icon :component="Download" />
          </template>
          {{ t('export.calendar') }}
        </n-button>

        <n-button block @click="exportTasks">
          <template #icon>
            <n-icon :component="Download" />
          </template>
          {{ t('export.tasks') }}
        </n-button>

        <n-button block type="primary" @click="exportAll">
          <template #icon>
            <n-icon :component="Download" />
          </template>
          {{ t('export.all') }}
        </n-button>
      </n-space>
    </div>
  </n-modal>
</template>

<style scoped lang="scss">
.export-content {
  .export-desc {
    margin-bottom: 16px;
    color: var(--n-text-color-3);
    font-size: 13px;
  }
}
</style>
