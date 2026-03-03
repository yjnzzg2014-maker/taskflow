<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import PomodoroTimer from '@/components/PomodoroTimer.vue'
import { GridOutline, CalendarOutline, ListOutline, PersonOutline, Moon, Sunny, ConstructOutline } from '@vicons/ionicons5'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isCollapse = ref(false)

const menuItems = computed(() => [
  { label: t('nav.dashboard'), key: '/', icon: () => h(GridOutline) },
  { label: t('nav.calendar'), key: '/calendar', icon: () => h(CalendarOutline) },
  { label: t('nav.tasks'), key: '/tasks', icon: () => h(ListOutline) },
  { label: t('nav.profile'), key: '/profile', icon: () => h(PersonOutline) },
  { label: '常用工具', key: '/tools', icon: () => h(ConstructOutline) }
])

const activeMenu = computed(() => route.path)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <n-layout has-sider class="app-container">
    <n-layout-sider
      bordered
      :width="isCollapse ? 64 : 200"
      :collapsed-width="64"
      :collapsed="isCollapse"
      show-trigger
      collapse-mode="width"
      :native-scrollbar="false"
      class="app-aside"
      @collapse="isCollapse = true"
      @expand="isCollapse = false"
    >
      <div class="logo">
        <n-icon :size="24" color="#18a058">
          <CalendarOutline />
        </n-icon>
        <span v-if="!isCollapse" class="logo-text">TaskFlow</span>
      </div>
      <n-menu
        :collapsed="isCollapse"
        :collapsed-width="64"
        :value="activeMenu"
        :options="menuItems"
        @update:value="(key: string) => router.push(key)"
      />
    </n-layout-sider>

    <n-layout>
      <n-layout-header bordered class="app-header">
        <div class="header-left">
          <span></span>
        </div>
        <div class="header-right">
          <PomodoroTimer />
          <LanguageSwitcher />
          <n-switch
            :value="authStore.isDark"
            @update:value="authStore.setDarkMode"
          >
            <template #checked-icon>
              <n-icon :component="Moon" />
            </template>
            <template #unchecked-icon>
              <n-icon :component="Sunny" />
            </template>
          </n-switch>
          <n-dropdown :options="[{ label: t('auth.logout'), key: 'logout' }]" @select="handleLogout">
            <n-button quaternary>
              <n-avatar :size="24" round>{{ authStore.user?.username?.[0]?.toUpperCase() }}</n-avatar>
              <span class="username">{{ authStore.user?.username }}</span>
            </n-button>
          </n-dropdown>
        </div>
      </n-layout-header>

      <n-layout-content class="app-main">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<style scoped lang="scss">
.app-container {
  height: 100vh;
}

.app-aside {
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border-bottom: 1px solid var(--n-border-color);
    color: #18a058;

    .logo-text {
      font-size: 18px;
      font-weight: 600;
      white-space: nowrap;
    }
  }
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  margin-left: 8px;
  font-size: 14px;
}

.app-main {
  padding: 20px;
  background: var(--n-color);
}
</style>
