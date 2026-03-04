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

type ViewMode = 'day' | 'week' | 'month'
const viewMode = ref<ViewMode>('month')
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

// 周视图数据
const weekDaysData = computed(() => {
  const startOfWeek = currentDate.value.startOf('week')
  const days = []
  for (let i = 0; i < 7; i++) {
    const date = startOfWeek.add(i, 'day')
    const dayEvents = eventStore.events.filter(e => {
      const eventDate = dayjs.utc(e.startTime).local()
      return eventDate.isSame(date, 'day')
    })
    days.push({
      date,
      isToday: date.isSame(dayjs(), 'day'),
      events: dayEvents
    })
  }
  return days
})

// 日视图数据（24小时）
const dayHours = computed(() => {
  const hours = []
  for (let i = 0; i < 24; i++) {
    hours.push(i)
  }
  return hours
})

// 判断是否是睡眠时间 (23:00 - 08:00)
function isSleepTime(hour: number) {
  return hour >= 23 || hour < 8
}

const dayEvents = computed(() => {
  return eventStore.events.filter(e => {
    const eventDate = dayjs.utc(e.startTime).local()
    return eventDate.isSame(currentDate.value, 'day')
  }).sort((a, b) => {
    return dayjs(a.startTime).valueOf() - dayjs(b.startTime).valueOf()
  })
})

function getEventsForHour(hour: number) {
  return dayEvents.value.filter(event => {
    const eventHour = dayjs.utc(event.startTime).local().hour()
    return eventHour === hour
  })
}

function getEventsForWeekDayHour(date: dayjs.Dayjs, hour: number) {
  return eventStore.events.filter(event => {
    const eventDate = dayjs.utc(event.startTime).local()
    return eventDate.isSame(date, 'day') && eventDate.hour() === hour
  })
}

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

function prevWeek() {
  currentDate.value = currentDate.value.subtract(1, 'week')
  fetchEvents()
}

function nextWeek() {
  currentDate.value = currentDate.value.add(1, 'week')
  fetchEvents()
}

function prevDay() {
  currentDate.value = currentDate.value.subtract(1, 'day')
  fetchEvents()
}

function nextDay() {
  currentDate.value = currentDate.value.add(1, 'day')
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
          <n-button @click="viewMode === 'month' ? prevMonth() : viewMode === 'week' ? prevWeek() : prevDay()">
            <template #icon>
              <n-icon :component="ArrowBack" />
            </template>
          </n-button>
          <n-button @click="goToday">{{ t('calendar.today') }}</n-button>
          <n-button @click="viewMode === 'month' ? nextMonth() : viewMode === 'week' ? nextWeek() : nextDay()">
            <template #icon>
              <n-icon :component="ArrowForward" />
            </template>
          </n-button>
        </n-button-group>
        <span class="current-month">{{ viewMode === 'day' ? currentDate.format('MMMM D, YYYY') : viewMode === 'week' ? currentDate.format('MMMM YYYY') : currentDate.format('MMMM YYYY') }}</span>
        <n-radio-group v-model:value="viewMode" name="viewMode" class="view-toggle">
          <n-radio-button value="day">{{ t('calendar.views.day') }}</n-radio-button>
          <n-radio-button value="week">{{ t('calendar.views.week') }}</n-radio-button>
          <n-radio-button value="month">{{ t('calendar.views.month') }}</n-radio-button>
        </n-radio-group>
        <n-button type="primary" @click="openDialog()">
          <template #icon>
            <n-icon :component="Add" />
          </template>
          {{ t('calendar.newEvent') }}
        </n-button>
      </div>
    </div>

    <!-- 月视图 -->
    <n-card v-if="viewMode === 'month'" :bordered="false" class="calendar-card">
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

    <!-- 周视图 -->
    <n-card v-else-if="viewMode === 'week'" :bordered="false" class="calendar-card week-view">
      <div class="week-header">
        <div v-for="day in weekDaysData" :key="day.date.toString()" class="week-day-header" :class="{ 'is-today': day.isToday }">
          <span class="week-day-name">{{ weekDays[day.date.day()] }}</span>
          <span class="week-day-number">{{ day.date.date() }}</span>
        </div>
      </div>
      <div class="week-grid">
        <div v-for="hour in dayHours" :key="hour" class="week-hour-row">
          <div class="hour-label">{{ hour.toString().padStart(2, '0') }}:00</div>
          <div v-for="day in weekDaysData" :key="day.date.toString()" class="week-hour-cell" @click="openDialog(day.date.hour(hour))">
            <div
              v-for="event in getEventsForWeekDayHour(day.date, hour)"
              :key="event.id"
              class="week-event-item"
              @click.stop="openEditDialog(event)"
            >
              <span class="event-time">{{ formatEventTime(event) }}</span>
              <span class="event-title">{{ event.title }}</span>
            </div>
          </div>
        </div>
      </div>
    </n-card>

    <!-- 日视图 -->
    <n-card v-else :bordered="false" class="calendar-card day-view">
      <div class="day-header">
        <div class="day-info">
          <span class="day-weekday">{{ weekDays[currentDate.day()] }}</span>
          <span class="day-number-large">{{ currentDate.date() }}</span>
          <span class="day-month-year">{{ currentDate.format('MMMM YYYY') }}</span>
        </div>
      </div>
      <div class="day-grid">
        <div v-for="hour in dayHours" :key="hour" class="day-hour-row" :class="{ 'sleep-time': isSleepTime(hour) }" @click="openDialog(currentDate.hour(hour))">
          <div class="hour-label">{{ hour.toString().padStart(2, '0') }}:00</div>
          <div class="day-hour-content">
            <div
              v-for="event in getEventsForHour(hour)"
              :key="event.id"
              class="day-event-item"
              @click.stop="openEditDialog(event)"
            >
              <span class="event-time">{{ formatEventTime(event) }}</span>
              <span class="event-title">{{ event.title }}</span>
              <span v-if="event.location" class="event-location">{{ event.location }}</span>
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
      min-width: 180px;
      text-align: center;
    }

    .view-toggle {
      :deep(.n-radio-button) {
        padding: 0 12px;
      }
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

// 周视图
.week-view {
  .week-header {
    display: grid;
    grid-template-columns: 60px repeat(7, 1fr);
    border-bottom: 1px solid var(--n-border-color);
  }

  .week-day-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 12px 8px;
    border-right: 1px solid var(--n-border-color);

    &.is-today {
      background: rgba(24, 160, 88, 0.1);

      .week-day-number {
        background: #18a058;
        color: #fff;
        border-radius: 50%;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .week-day-name {
      font-size: 12px;
      color: var(--n-text-color-3);
    }

    .week-day-number {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .week-grid {
    max-height: 600px;
    overflow-y: auto;
  }

  .week-hour-row {
    display: grid;
    grid-template-columns: 60px repeat(7, 1fr);
    min-height: 48px;
    border-bottom: 1px solid var(--n-border-color);

    .hour-label {
      padding: 4px 8px;
      font-size: 12px;
      color: var(--n-text-color-3);
      border-right: 1px solid var(--n-border-color);
    }

    .week-hour-cell {
      padding: 2px;
      border-right: 1px solid var(--n-border-color);
      cursor: pointer;
      min-height: 48px;

      &:hover {
        background: var(--n-color-hover);
      }

      &:last-child {
        border-right: none;
      }
    }
  }

  .week-event-item {
    font-size: 11px;
    padding: 2px 4px;
    margin-bottom: 2px;
    border-radius: 3px;
    background: #18a058;
    color: #fff;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    cursor: pointer;

    .event-time {
      margin-right: 4px;
    }
  }
}

// 日视图
.day-view {
  .day-header {
    text-align: center;
    padding: 16px;
    border-bottom: 1px solid var(--n-border-color);

    .day-info {
      display: flex;
      flex-direction: column;
      align-items: center;

      .day-weekday {
        font-size: 14px;
        color: var(--n-text-color-3);
        text-transform: uppercase;
      }

      .day-number-large {
        font-size: 48px;
        font-weight: 700;
        line-height: 1;
        margin: 8px 0;
      }

      .day-month-year {
        font-size: 16px;
        color: var(--n-text-color-2);
      }
    }
  }

  .day-grid {
    max-height: 700px;
    overflow-y: auto;
  }

  .day-hour-row {
    display: grid;
    grid-template-columns: 70px 1fr;
    min-height: 60px;
    border-bottom: 1px solid var(--n-border-color);
    cursor: pointer;

    &.sleep-time {
      background: rgba(128, 128, 128, 0.1);

      .hour-label {
        color: rgba(128, 128, 128, 0.6);
      }
    }

    &:hover {
      background: var(--n-color-hover);
    }

    .hour-label {
      padding: 8px;
      font-size: 13px;
      color: var(--n-text-color-3);
      border-right: 1px solid var(--n-border-color);
      text-align: right;
    }

    .day-hour-content {
      padding: 4px 8px;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
  }

  .day-event-item {
    padding: 8px 12px;
    border-radius: 6px;
    background: #18a058;
    color: #fff;
    cursor: pointer;

    &:hover {
      opacity: 0.9;
    }

    .event-time {
      font-size: 12px;
      opacity: 0.9;
      margin-right: 8px;
    }

    .event-title {
      font-size: 14px;
      font-weight: 500;
    }

    .event-location {
      display: block;
      font-size: 12px;
      opacity: 0.8;
      margin-top: 4px;
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .calendar-page {
    .calendar-header {
      flex-wrap: wrap;
      gap: 12px;

      .page-title {
        font-size: 18px;
        width: 100%;
      }

      .calendar-actions {
        width: 100%;
        justify-content: space-between;
        flex-wrap: wrap;

        .current-month {
          font-size: 14px;
          min-width: auto;
          order: -1;
          width: 100%;
          margin-bottom: 8px;
        }

        .view-toggle {
          :deep(.n-radio-button) {
            padding: 0 8px;
            font-size: 12px;
          }
        }
      }
    }

    .calendar-grid {
      grid-template-columns: repeat(7, 1fr);
    }

    .calendar-weekday {
      padding: 8px 2px;
      font-size: 11px;
    }

    .calendar-day {
      min-height: 80px;
      padding: 4px;

      .day-number {
        font-size: 12px;
      }

      .day-events {
        .event-item {
          font-size: 10px;
          padding: 1px 4px;

          .event-time {
            display: none;
          }
        }

        .more-events {
          font-size: 9px;
        }
      }
    }

    .week-view {
      .week-header {
        grid-template-columns: 40px repeat(7, 1fr);
      }

      .week-day-header {
        padding: 8px 2px;

        .week-day-name {
          font-size: 10px;
        }

        .week-day-number {
          font-size: 14px;
        }

        &.is-today .week-day-number {
          width: 24px;
          height: 24px;
        }
      }

      .week-hour-row {
        grid-template-columns: 40px repeat(7, 1fr);
        min-height: 36px;

        .hour-label {
          font-size: 10px;
          padding: 2px;
        }

        .week-hour-cell {
          min-height: 36px;
        }
      }
    }

    .day-view {
      .day-header {
        padding: 12px;

        .day-info {
          .day-weekday {
            font-size: 12px;
          }

          .day-number-large {
            font-size: 36px;
          }

          .day-month-year {
            font-size: 14px;
          }
        }
      }

      .day-grid {
        max-height: calc(100vh - 300px);
      }

      .day-hour-row {
        grid-template-columns: 50px 1fr;
        min-height: 50px;

        .hour-label {
          font-size: 11px;
          padding: 4px;
        }
      }

      .day-event-item {
        padding: 6px 8px;

        .event-title {
          font-size: 12px;
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .calendar-page {
    .calendar-day {
      min-height: 60px;

      .day-events {
        display: none;
      }
    }
  }
}
</style>
