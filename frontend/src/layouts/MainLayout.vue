<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import PomodoroTimer from '@/components/PomodoroTimer.vue'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isCollapse = ref(false)

const menuItems = computed(() => [
  { path: '/', title: t('nav.dashboard'), icon: 'DataBoard' },
  { path: '/calendar', title: t('nav.calendar'), icon: 'Calendar' },
  { path: '/tasks', title: t('nav.tasks'), icon: 'List' },
  { path: '/profile', title: t('nav.profile'), icon: 'User' }
])

const activeMenu = computed(() => route.path)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="app-container">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="app-aside">
      <div class="logo">
        <el-icon :size="24"><Calendar /></el-icon>
        <span v-show="!isCollapse" class="logo-text">TaskFlow</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="app-menu"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <PomodoroTimer />
          <LanguageSwitcher />
          <el-switch
            v-model="authStore.isDark"
            :active-action-icon="'Moon'"
            :inactive-action-icon="'Sunny'"
            @change="(val: string | number | boolean) => authStore.setDarkMode(val as boolean)"
          />
          <el-dropdown @command="handleLogout">
            <span class="user-info">
              <el-avatar :size="32">{{ authStore.user?.username?.[0]?.toUpperCase() }}</el-avatar>
              <span class="username">{{ authStore.user?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">{{ t('auth.logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped lang="scss">
.app-container {
  height: 100vh;
}

.app-aside {
  background-color: #fff;
  border-right: 1px solid var(--border-color);
  transition: width 0.3s;
  overflow: hidden;

  .dark & {
    background-color: #1a1a1a;
  }
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid var(--border-color);
  color: var(--primary-color);

  .logo-text {
    font-size: 18px;
    font-weight: 600;
    white-space: nowrap;
  }
}

.app-menu {
  border-right: none;
}

.app-header {
  background-color: #fff;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;

  .dark & {
    background-color: #1a1a1a;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  .username {
    font-size: 14px;
  }
}

.app-main {
  background-color: var(--bg-color);
  padding: 20px;
  overflow-y: auto;
}
</style>
