import axios from 'axios'
import type { AuthResponse, LoginRequest, RegisterRequest, User, Event, Task, SubTask, Category, Tag } from '@/types'

const baseURL = import.meta.env.VITE_API_URL || '/api'

const api = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const isLoginRequest = error.config?.url?.includes('/auth/login')
      if (!isLoginRequest) {
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// Auth API
export const authApi = {
  login: (data: LoginRequest) => api.post<AuthResponse>('/auth/login', data),
  register: (data: RegisterRequest) => api.post<AuthResponse>('/auth/register', data),
  refreshToken: (refreshToken: string) => api.post<AuthResponse>('/auth/refresh', refreshToken),
  getCurrentUser: () => api.get<User>('/auth/me')
}

// Events API
export const eventApi = {
  getAll: () => api.get<Event[]>('/events'),
  getById: (id: number) => api.get<Event>(`/events/${id}`),
  getCalendar: (start: string, end: string) => api.get<Event[]>('/events/calendar', { params: { start, end } }),
  create: (data: Partial<Event>) => api.post<Event>('/events', data),
  update: (id: number, data: Partial<Event>) => api.put<Event>(`/events/${id}`, data),
  delete: (id: number) => api.delete(`/events/${id}`)
}

// Tasks API
export const taskApi = {
  getAll: () => api.get<Task[]>('/tasks'),
  getById: (id: number) => api.get<Task>(`/tasks/${id}`),
  getByStatus: (status: string) => api.get<Task[]>(`/tasks/status/${status}`),
  search: (keyword: string) => api.get<Task[]>('/tasks/search', { params: { keyword } }),
  create: (data: Partial<Task>) => api.post<Task>('/tasks', data),
  update: (id: number, data: Partial<Task>) => api.put<Task>(`/tasks/${id}`, data),
  delete: (id: number) => api.delete(`/tasks/${id}`),
  addSubTask: (taskId: number, title: string) => api.post<SubTask>(`/tasks/${taskId}/subtasks`, { title }),
  updateSubTask: (taskId: number, subTaskId: number, data: Partial<SubTask>) => api.put<SubTask>(`/tasks/${taskId}/subtasks/${subTaskId}`, data),
  deleteSubTask: (taskId: number, subTaskId: number) => api.delete(`/tasks/${taskId}/subtasks/${subTaskId}`)
}

// Categories API
export const categoryApi = {
  getAll: () => api.get<Category[]>('/categories'),
  getById: (id: number) => api.get<Category>(`/categories/${id}`),
  create: (data: Partial<Category>) => api.post<Category>('/categories', data),
  update: (id: number, data: Partial<Category>) => api.put<Category>(`/categories/${id}`, data),
  delete: (id: number) => api.delete(`/categories/${id}`)
}

// Tags API
export const tagApi = {
  getAll: () => api.get<Tag[]>('/tags'),
  getById: (id: number) => api.get<Tag>(`/tags/${id}`),
  create: (data: Partial<Tag>) => api.post<Tag>('/tags', data),
  update: (id: number, data: Partial<Tag>) => api.put<Tag>(`/tags/${id}`, data),
  delete: (id: number) => api.delete(`/tags/${id}`)
}

// Stats API
export const statsApi = {
  getDashboard: () => api.get('/stats/dashboard')
}

// Pomodoro API
export const pomodoroApi = {
  createRecord: (data: { taskId?: number; mode: string; duration: number; notes?: string }) =>
    api.post('/pomodoros/records', data),
  getRecords: () => api.get('/pomodoros/records'),
  getStats: () => api.get('/pomodoros/stats')
}

export default api
