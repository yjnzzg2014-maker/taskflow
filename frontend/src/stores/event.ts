import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Event } from '@/types'
import { eventApi } from '@/api'

export const useEventStore = defineStore('event', () => {
  const events = ref<Event[]>([])
  const currentEvent = ref<Event | null>(null)
  const isLoading = ref(false)

  async function fetchEvents() {
    isLoading.value = true
    try {
      const response = await eventApi.getAll()
      events.value = response.data
    } finally {
      isLoading.value = false
    }
  }

  async function fetchCalendarEvents(start: string, end: string) {
    isLoading.value = true
    try {
      const response = await eventApi.getCalendar(start, end)
      events.value = response.data
      return response.data
    } finally {
      isLoading.value = false
    }
  }

  async function fetchEventById(id: number) {
    isLoading.value = true
    try {
      const response = await eventApi.getById(id)
      currentEvent.value = response.data
      return response.data
    } finally {
      isLoading.value = false
    }
  }

  async function createEvent(data: Partial<Event>) {
    const response = await eventApi.create(data)
    events.value.unshift(response.data)
    return response.data
  }

  async function updateEvent(id: number, data: Partial<Event>) {
    const response = await eventApi.update(id, data)
    const index = events.value.findIndex(e => e.id === id)
    if (index !== -1) {
      events.value[index] = response.data
    }
    return response.data
  }

  async function deleteEvent(id: number) {
    await eventApi.delete(id)
    events.value = events.value.filter(e => e.id !== id)
  }

  return {
    events,
    currentEvent,
    isLoading,
    fetchEvents,
    fetchCalendarEvents,
    fetchEventById,
    createEvent,
    updateEvent,
    deleteEvent
  }
})
