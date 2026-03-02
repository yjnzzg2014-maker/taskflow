import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Task } from '@/types'
import { taskApi } from '@/api'

export const useTaskStore = defineStore('task', () => {
  const tasks = ref<Task[]>([])
  const currentTask = ref<Task | null>(null)
  const isLoading = ref(false)

  async function fetchTasks() {
    isLoading.value = true
    try {
      const response = await taskApi.getAll()
      tasks.value = response.data
    } finally {
      isLoading.value = false
    }
  }

  async function fetchTaskById(id: number) {
    isLoading.value = true
    try {
      const response = await taskApi.getById(id)
      currentTask.value = response.data
      return response.data
    } finally {
      isLoading.value = false
    }
  }

  async function createTask(data: Partial<Task>) {
    const response = await taskApi.create(data)
    tasks.value.unshift(response.data)
    return response.data
  }

  async function updateTask(id: number, data: Partial<Task>) {
    const response = await taskApi.update(id, data)
    const index = tasks.value.findIndex(t => t.id === id)
    if (index !== -1) {
      tasks.value[index] = response.data
    }
    return response.data
  }

  async function deleteTask(id: number) {
    await taskApi.delete(id)
    tasks.value = tasks.value.filter(t => t.id !== id)
  }

  async function searchTasks(keyword: string) {
    const response = await taskApi.search(keyword)
    tasks.value = response.data
    return response.data
  }

  return {
    tasks,
    currentTask,
    isLoading,
    fetchTasks,
    fetchTaskById,
    createTask,
    updateTask,
    deleteTask,
    searchTasks
  }
})
