import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginRequest, RegisterRequest } from '@/types'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const isLoading = ref(false)

  const isAuthenticated = computed(() => !!token.value)
  const isDark = ref(localStorage.getItem('theme') === 'dark')

  // Apply dark mode on initial load
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  }

  async function login(credentials: LoginRequest) {
    isLoading.value = true
    try {
      const response = await authApi.login(credentials)
      token.value = response.data.token
      refreshToken.value = response.data.refreshToken
      user.value = response.data.user
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('refreshToken', response.data.refreshToken)
      return response.data
    } finally {
      isLoading.value = false
    }
  }

  async function register(data: RegisterRequest) {
    isLoading.value = true
    try {
      const response = await authApi.register(data)
      token.value = response.data.token
      refreshToken.value = response.data.refreshToken
      user.value = response.data.user
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('refreshToken', response.data.refreshToken)
      return response.data
    } finally {
      isLoading.value = false
    }
  }

  async function fetchCurrentUser() {
    if (!token.value) return
    try {
      const response = await authApi.getCurrentUser()
      user.value = response.data
    } catch (error) {
      logout()
    }
  }

  function logout() {
    token.value = null
    refreshToken.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  function toggleDarkMode() {
    isDark.value = !isDark.value
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  function setDarkMode(value: boolean) {
    isDark.value = value
    localStorage.setItem('theme', value ? 'dark' : 'light')
    document.documentElement.classList.toggle('dark', value)
  }

  return {
    user,
    token,
    refreshToken,
    isLoading,
    isAuthenticated,
    isDark,
    login,
    register,
    fetchCurrentUser,
    logout,
    toggleDarkMode,
    setDarkMode
  }
})
