<script setup lang="ts">
import { ref, computed, h, onMounted, onUnmounted } from 'vue'
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
const isMobile = ref(false)
const showMobileMenu = ref(false)

const menuItems = computed(() => [
  { label: t('nav.dashboard'), key: '/', icon: () => h(GridOutline) },
  { label: t('nav.calendar'), key: '/calendar', icon: () => h(CalendarOutline) },
  { label: t('nav.tasks'), key: '/tasks', icon: () => h(ListOutline) },
  { label: t('nav.profile'), key: '/profile', icon: () => h(PersonOutline) },
  { label: t('nav.tools'), key: '/tools', icon: () => h(ConstructOutline) }
])

const activeMenu = computed(() => route.path)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

function checkMobile() {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    isCollapse.value = true
  }
}

function navigateTo(path: string) {
  router.push(path)
  showMobileMenu.value = false
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<template>
  <div class="app-container">
    <!-- 桌面端侧边栏 -->
    <n-layout v-if="!isMobile" has-sider class="desktop-layout">
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

    <!-- 移动端布局 -->
    <div v-else class="mobile-layout">
      <n-layout-header bordered class="mobile-header">
        <div class="mobile-header-left">
          <span class="mobile-title">TaskFlow</span>
        </div>
        <div class="mobile-header-right">
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
        </div>
      </n-layout-header>

      <n-layout-content class="mobile-main">
        <router-view />
      </n-layout-content>

      <!-- 移动端底部导航 -->
      <div class="mobile-nav">
        <div
          v-for="item in menuItems"
          :key="item.key"
          class="mobile-nav-item"
          :class="{ active: activeMenu === item.key }"
          @click="navigateTo(item.key as string)"
        >
          <n-icon :size="24" :component="item.icon" />
          <span class="mobile-nav-label">{{ item.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.app-container {
  height: 100vh;
  overflow: hidden;
}

.desktop-layout {
  height: 100%;
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

// 移动端样式
.mobile-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.mobile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 12px;
  height: 50px;
  flex-shrink: 0;
}

.mobile-header-left {
  .mobile-title {
    font-size: 16px;
    font-weight: 600;
    color: #18a058;
  }
}

.mobile-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mobile-main {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.mobile-nav {
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 60px;
  border-top: 1px solid var(--n-border-color);
  background: var(--n-color);
  flex-shrink: 0;
}

.mobile-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--n-text-color-3);
  transition: color 0.2s;

  &.active {
    color: #18a058;
  }

  .mobile-nav-label {
    font-size: 10px;
  }
}

// 响应式调整
@media (max-width: 768px) {
  .app-main {
    padding: 12px;
  }
}
</style>
