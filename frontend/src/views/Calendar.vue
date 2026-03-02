<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useEventStore } from '@/stores/event'
import type { Event } from '@/types'

const eventStore = useEventStore()

const currentDate = ref(dayjs())
// const viewMode = ref<'month' | 'week' | 'day'>('month')
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentEvent = ref<Partial<Event>>({})

const form = ref<{
  title: string
  description: string
  startTime: string
  endTime: string
  location: string
  isAllDay: boolean
  repeatType: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CUSTOM'
  remindAt: string
}>({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  location: '',
  isAllDay: false,
  repeatType: 'NONE',
  remindAt: ''
})

const calendarDays = computed(() => {
  const startOfMonth = currentDate.value.startOf('month')
  const endOfMonth = currentDate.value.endOf('month')
  const startDay = startOfMonth.day()
  const daysInMonth = endOfMonth.date()

  const days = []

  // Previous month days
  for (let i = 0; i < startDay; i++) {
    days.push({
      date: startOfMonth.subtract(startDay - i, 'day'),
      isCurrentMonth: false,
      events: []
    })
  }

  // Current month days
  for (let i = 1; i <= daysInMonth; i++) {
    const date = currentDate.value.date(i)
    const dayEvents = eventStore.events.filter(e => {
      const eventDate = dayjs(e.startTime)
      return eventDate.isSame(date, 'day')
    })
    days.push({
      date,
      isCurrentMonth: true,
      events: dayEvents
    })
  }

  // Next month days to fill the grid
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    days.push({
      date: endOfMonth.add(i, 'day'),
      isCurrentMonth: false,
      events: []
    })
  }

  return days
})

const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

onMounted(async () => {
  await fetchEvents()
})

async function fetchEvents() {
  const start = currentDate.value.startOf('month').toISOString()
  const end = currentDate.value.endOf('month').toISOString()
  await eventStore.fetchCalendarEvents(start, end)
}

function prevMonth() {
  currentDate.value = currentDate.value.subtract(1, 'month')
  fetchEvents()
}

function nextMonth() {
  currentDate.value = currentDate.value.add(1, 'month')
  fetchEvents()
}

function goToday() {
  currentDate.value = dayjs()
  fetchEvents()
}

function openDialog(date?: dayjs.Dayjs) {
  isEdit.value = false
  currentEvent.value = {}
  form.value = {
    title: '',
    description: '',
    startTime: date ? date.format('YYYY-MM-DDTHH:mm') : dayjs().format('YYYY-MM-DDTHH:mm'),
    endTime: '',
    location: '',
    isAllDay: false,
    repeatType: 'NONE',
    remindAt: ''
  }
  dialogVisible.value = true
}

function openEditDialog(event: Event) {
  isEdit.value = true
  currentEvent.value = event
  form.value = {
    title: event.title,
    description: event.description || '',
    startTime: dayjs(event.startTime).format('YYYY-MM-DDTHH:mm'),
    endTime: event.endTime ? dayjs(event.endTime).format('YYYY-MM-DDTHH:mm') : '',
    location: event.location || '',
    isAllDay: event.isAllDay,
    repeatType: event.repeatType,
    remindAt: event.remindAt ? dayjs(event.remindAt).format('YYYY-MM-DDTHH:mm') : ''
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    const data = {
      title: form.value.title,
      description: form.value.description,
      startTime: dayjs(form.value.startTime).toISOString(),
      endTime: form.value.endTime ? dayjs(form.value.endTime).toISOString() : undefined,
      location: form.value.location,
      isAllDay: form.value.isAllDay,
      repeatType: form.value.repeatType,
      remindAt: form.value.remindAt ? dayjs(form.value.remindAt).toISOString() : undefined
    }

    if (isEdit.value && currentEvent.value.id) {
      await eventStore.updateEvent(currentEvent.value.id, data)
      ElMessage.success('Event updated successfully')
    } else {
      await eventStore.createEvent(data)
      ElMessage.success('Event created successfully')
    }
    dialogVisible.value = false
    await fetchEvents()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'Operation failed')
  }
}

async function handleDelete() {
  if (!currentEvent.value.id) return

  try {
    await eventStore.deleteEvent(currentEvent.value.id)
    ElMessage.success('Event deleted successfully')
    dialogVisible.value = false
    await fetchEvents()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || 'Delete failed')
  }
}

function formatEventTime(event: Event) {
  if (event.isAllDay) return 'All day'
  return dayjs(event.startTime).format('HH:mm')
}
</script>

<template>
  <div class="calendar-page">
    <div class="calendar-header">
      <h1 class="page-title">Calendar</h1>
      <div class="calendar-actions">
        <el-button-group>
          <el-button @click="prevMonth"><el-icon><ArrowLeft /></el-icon></el-button>
          <el-button @click="goToday">Today</el-button>
          <el-button @click="nextMonth"><el-icon><ArrowRight /></el-icon></el-button>
        </el-button-group>
        <span class="current-month">{{ currentDate.format('MMMM YYYY') }}</span>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          New Event
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="calendar-card">
      <div class="calendar-grid">
        <div v-for="day in weekDays" :key="day" class="calendar-weekday">{{ day }}</div>
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          class="calendar-day"
          :class="{
            'other-month': !day.isCurrentMonth,
            'today': day.date.isSame(dayjs(), 'day')
          }"
          @click="openDialog(day.date)"
        >
          <span class="day-number">{{ day.date.date() }}</span>
          <div class="day-events">
            <div
              v-for="event in day.events.slice(0, 3)"
              :key="event.id"
              class="event-item"
              @click.stop="openEditDialog(event)"
            >
              <span class="event-time">{{ formatEventTime(event) }}</span>
              <span class="event-title">{{ event.title }}</span>
            </div>
            <div v-if="day.events.length > 3" class="more-events">
              +{{ day.events.length - 3 }} more
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? 'Edit Event' : 'New Event'"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="Title" required>
          <el-input v-model="form.title" placeholder="Event title" />
        </el-form-item>

        <el-form-item label="Description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="All Day">
          <el-switch v-model="form.isAllDay" />
        </el-form-item>

        <el-form-item label="Start Time" required>
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="Select date and time"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="End Time">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="Select date and time"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="Location">
          <el-input v-model="form.location" placeholder="Event location" />
        </el-form-item>

        <el-form-item label="Repeat">
          <el-select v-model="form.repeatType" style="width: 100%">
            <el-option label="None" value="NONE" />
            <el-option label="Daily" value="DAILY" />
            <el-option label="Weekly" value="WEEKLY" />
            <el-option label="Monthly" value="MONTHLY" />
            <el-option label="Custom" value="CUSTOM" />
          </el-select>
        </el-form-item>

        <el-form-item label="Remind">
          <el-date-picker
            v-model="form.remindAt"
            type="datetime"
            placeholder="Remind time"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="isEdit" type="danger" @click="handleDelete">Delete</el-button>
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="handleSubmit">{{ isEdit ? 'Update' : 'Create' }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.calendar-page {
  max-width: 1400px;
  margin: 0 auto;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .page-title {
    margin: 0;
  }

  .calendar-actions {
    display: flex;
    align-items: center;
    gap: 16px;

    .current-month {
      font-size: 18px;
      font-weight: 600;
    }
  }
}

.calendar-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.calendar-weekday {
  padding: 12px;
  text-align: center;
  font-weight: 600;
  border-bottom: 1px solid var(--border-color);
  background: #f5f7fa;

  .dark & {
    background: #2a2a2a;
  }
}

.calendar-day {
  min-height: 120px;
  padding: 8px;
  border-right: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f5f7fa;

    .dark & {
      background: #2a2a2a;
    }
  }

  &:nth-child(7n) {
    border-right: none;
  }

  &.other-month {
    .day-number {
      color: #c0c4cc;
    }
  }

  &.today {
    .day-number {
      background: #409eff;
      color: #fff;
      border-radius: 50%;
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .day-number {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 4px;
  }

  .day-events {
    .event-item {
      font-size: 12px;
      padding: 2px 6px;
      margin-bottom: 2px;
      border-radius: 4px;
      background: #409eff;
      color: #fff;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      cursor: pointer;

      &:hover {
        opacity: 0.8;
      }

      .event-time {
        margin-right: 4px;
      }
    }

    .more-events {
      font-size: 11px;
      color: #909399;
      padding: 2px 6px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
