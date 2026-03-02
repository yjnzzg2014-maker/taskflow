export interface User {
  id: number
  username: string
  email: string
  role: 'ADMIN' | 'USER'
  avatar?: string
  createdAt: string
}

export interface AuthResponse {
  token: string
  refreshToken: string
  user: User
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface Event {
  id: number
  userId: number
  title: string
  description?: string
  startTime: string
  endTime?: string
  location?: string
  isAllDay: boolean
  repeatType: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'CUSTOM'
  repeatRule?: string
  remindAt?: string
  tags: Tag[]
  createdAt: string
  updatedAt: string
}

export interface Task {
  id: number
  userId: number
  title: string
  description?: string
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'
  dueDate?: string
  category?: Category
  subTasks: SubTask[]
  tags: Tag[]
  createdAt: string
  updatedAt: string
}

export interface SubTask {
  id: number
  taskId: number
  title: string
  isCompleted: boolean
  createdAt: string
}

export interface Category {
  id: number
  userId: number
  name: string
  createdAt: string
}

export interface Tag {
  id: number
  userId: number
  name: string
  color?: string
  createdAt: string
}

export interface DashboardStats {
  totalTasks: number
  completedTasks: number
  pendingTasks: number
  inProgressTasks: number
  overdueTasks: number
  totalEvents: number
  todayEvents: number
  todayTasks: number
  completionRate: number
}

export interface TrendData {
  date: string
  created: number
  completed: number
}
