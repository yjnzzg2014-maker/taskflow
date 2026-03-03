<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { useEventStore } from '@/stores/event'
import type { Event } from '@/types'
import { ArrowBack, ArrowForward, Add } from '@vicons/ionicons5'

dayjs.extend(utc)

const { t } = useI18n()
const message = useMessage()
const eventStore = useEventStore()

const currentDate = ref(dayjs())
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentEvent = ref<Partial<Event>>({})

const form = ref<{
  title: string
  description: string
  startTime: number | null
  endTime: number | null
  location: string
  isAllDay: boolean
  repeatType: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CUSTOM'
  remindAt: number | null
}>({
  title: '',
  description: '',
  startTime: null,
  endTime: null,
  location: '',
  isAllDay: false,
  repeatType: 'NONE',
  remindAt: null
})

const calendarDays = computed(() => {
  const startOfMonth = currentDate.value.startOf('month')
  const endOfMonth = currentDate.value.endOf('month')
  const startDay = startOfMonth.day()
  const daysInMonth = endOfMonth.date()

  const days = []

  for (let i = 0; i < startDay; i++) {
    days.push({
      date: startOfMonth.subtract(startDay - i, 'day'),
      isCurrentMonth: false,
      events: []
    })
  }

  for (let i = 1; i <= daysInMonth; i++) {
    const date = currentDate.value.date(i)
    const dayEvents = eventStore.events.filter(e => {
      const eventDate = dayjs.utc(e.startTime).local()
      return eventDate.isSame(date, 'day')
    })
    days.push({
      date,
      isCurrentMonth: true,
      events: dayEvents
    })
  }

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

const weekDays = computed(() => [
  t('calendar.weekdays.sun'),
  t('calendar.weekdays.mon'),
  t('calendar.weekdays.tue'),
  t('calendar.weekdays.wed'),
  t('calendar.weekdays.thu'),
  t('calendar.weekdays.fri'),
  t('calendar.weekdays.sat')
])

const repeatOptions = computed(() => [
  { label: t('calendar.repeatTypes.none'), value: 'NONE' },
  { label: t('calendar.repeatTypes.daily'), value: 'DAILY' },
  { label: t('calendar.repeatTypes.weekly'), value: 'WEEKLY' },
  { label: t('calendar.repeatTypes.monthly'), value: 'MONTHLY' },
  { label: t('calendar.repeatTypes.custom'), value: 'CUSTOM' }
])

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
  const defaultDate = date || dayjs()
  form.value = {
    title: '',
    description: '',
    startTime: defaultDate.valueOf(),
    endTime: null,
    location: '',
    isAllDay: false,
    repeatType: 'NONE',
    remindAt: null
  }
  dialogVisible.value = true
}

function openEditDialog(event: Event) {
  isEdit.value = true
  currentEvent.value = event
  form.value = {
    title: event.title,
    description: event.description || '',
    startTime: event.startTime ? dayjs(event.startTime).valueOf() : null,
    endTime: event.endTime ? dayjs(event.endTime).valueOf() : null,
    location: event.location || '',
    isAllDay: event.isAllDay,
    repeatType: event.repeatType,
    remindAt: event.remindAt ? dayjs(event.remindAt).valueOf() : null
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (!form.value.startTime) {
      message.error(t('calendar.startTime') + ' is required')
      return
    }

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
      message.success(t('common.success'))
    } else {
      await eventStore.createEvent(data)
      message.success(t('common.success'))
    }
    dialogVisible.value = false
    await fetchEvents()
  } catch (error: any) {
    message.error(error.response?.data?.message || t('common.failed'))
  }
}

async function handleDelete() {
  if (!currentEvent.value.id) return

  try {
    await eventStore.deleteEvent(currentEvent.value.id)
    message.success(t('common.success'))
    dialogVisible.value = false
    await fetchEvents()
  } catch (error: any) {
    message.error(error.response?.data?.message || t('common.failed'))
  }
}

function formatEventTime(event: Event) {
  if (event.isAllDay) return t('calendar.allDay')
  return dayjs.utc(event.startTime).local().format('HH:mm')
}
</script>

<template>
  <div class="calendar-page">
    <div class="calendar-header">
      <h1 class="page-title">{{ t('calendar.title') }}</h1>
      <div class="calendar-actions">
        <n-button-group>
          <n-button @click="prevMonth">
            <template #icon>
              <n-icon :component="ArrowBack" />
            </template>
          </n-button>
          <n-button @click="goToday">{{ t('calendar.today') }}</n-button>
          <n-button @click="nextMonth">
            <template #icon>
              <n-icon :component="ArrowForward" />
            </template>
          </n-button>
        </n-button-group>
        <span class="current-month">{{ currentDate.format('MMMM YYYY') }}</span>
        <n-button type="primary" @click="openDialog()">
          <template #icon>
            <n-icon :component="Add" />
          </template>
          {{ t('calendar.newEvent') }}
        </n-button>
      </div>
    </div>

    <n-card :bordered="false" class="calendar-card">
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
              +{{ day.events.length - 3 }}
            </div>
          </div>
        </div>
      </div>
    </n-card>

    <n-modal
      v-model:show="dialogVisible"
      :title="isEdit ? t('calendar.editEvent') : t('calendar.newEvent')"
      preset="card"
      style="width: 500px"
    >
      <n-form :model="form" label-placement="left" label-width="100px">
        <n-form-item :label="t('calendar.eventTitle')" required>
          <n-input v-model:value="form.title" :placeholder="t('calendar.eventTitle')" />
        </n-form-item>

        <n-form-item :label="t('calendar.description')">
          <n-input v-model:value="form.description" type="textarea" :rows="3" />
        </n-form-item>

        <n-form-item :label="t('calendar.allDay')">
          <n-switch v-model:value="form.isAllDay" />
        </n-form-item>

        <n-form-item :label="t('calendar.startTime')" required>
          <n-date-picker
            v-model:value="form.startTime"
            type="datetime"
            :placeholder="t('calendar.startTime')"
            style="width: 100%"
          />
        </n-form-item>

        <n-form-item :label="t('calendar.endTime')">
          <n-date-picker
            v-model:value="form.endTime"
            type="datetime"
            :placeholder="t('calendar.endTime')"
            style="width: 100%"
          />
        </n-form-item>

        <n-form-item :label="t('calendar.location')">
          <n-input v-model:value="form.location" :placeholder="t('calendar.location')" />
        </n-form-item>

        <n-form-item :label="t('calendar.repeat')">
          <n-select v-model:value="form.repeatType" :options="repeatOptions" />
        </n-form-item>

        <n-form-item :label="t('calendar.remind')">
          <n-date-picker
            v-model:value="form.remindAt"
            type="datetime"
            :placeholder="t('calendar.remind')"
            style="width: 100%"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <div class="dialog-footer">
          <n-button v-if="isEdit" type="error" @click="handleDelete">{{ t('common.delete') }}</n-button>
          <n-button @click="dialogVisible = false">{{ t('common.cancel') }}</n-button>
          <n-button type="primary" @click="handleSubmit">{{ isEdit ? t('common.edit') : t('common.create') }}</n-button>
        </div>
      </template>
    </n-modal>
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

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.calendar-weekday {
  padding: 12px;
  text-align: center;
  font-weight: 600;
  border-bottom: 1px solid var(--n-border-color);
  background: var(--n-color-hover);
}

.calendar-day {
  min-height: 120px;
  padding: 8px;
  border-right: 1px solid var(--n-border-color);
  border-bottom: 1px solid var(--n-border-color);
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: var(--n-color-hover);
  }

  &:nth-child(7n) {
    border-right: none;
  }

  &.other-month {
    .day-number {
      color: var(--n-text-color-3);
    }
  }

  &.today {
    .day-number {
      background: #18a058;
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
      background: #18a058;
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
      color: var(--n-text-color-3);
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
