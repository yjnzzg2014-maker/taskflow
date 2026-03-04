<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import dayjs from 'dayjs'

const { t } = useI18n()
const message = useMessage()
const authStore = useAuthStore()
const loading = ref(false)

const form = ref({
  email: '',
  avatar: ''
})

const avatarUrl = computed(() => {
  return form.value.avatar || authStore.user?.avatar
})

const formattedDate = computed(() => {
  if (authStore.user?.createdAt) {
    return dayjs(authStore.user.createdAt).format('YYYY-MM-DD')
  }
  return ''
})

onMounted(() => {
  if (authStore.user) {
    form.value.email = authStore.user.email || ''
    form.value.avatar = authStore.user.avatar || ''
  }
})

async function handleSubmit() {
  loading.value = true
  try {
    // TODO: Call API to update user profile
    // await authApi.updateProfile(form.value)
    message.success(t('common.success'))
  } catch (error) {
    message.error(t('common.failed'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="profile-page">
    <h1 class="page-title">{{ t('profile.title') }}</h1>

    <n-grid :cols="24" :x-gap="20" class="profile-grid">
      <n-grid-item :span="8" class="profile-left">
        <n-card hoverable>
          <div class="profile-avatar">
            <n-avatar v-if="avatarUrl" :size="120" round :src="avatarUrl" />
            <n-avatar v-else :size="120" round>
              {{ authStore.user?.username?.[0]?.toUpperCase() }}
            </n-avatar>
            <h3>{{ authStore.user?.username }}</h3>
            <n-tag type="primary">{{ authStore.user?.role }}</n-tag>
          </div>
          <n-divider />
          <div class="profile-info">
            <div class="info-item">
              <span class="label">{{ t('profile.email') }}</span>
              <span class="value">{{ authStore.user?.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ t('profile.memberSince') }}</span>
              <span class="value">{{ formattedDate }}</span>
            </div>
          </div>
        </n-card>
      </n-grid-item>

      <n-grid-item :span="16" class="profile-right">
        <n-card hoverable>
          <template #header>
            <span>{{ t('profile.editProfile') }}</span>
          </template>

          <n-form :model="form" label-placement="top">
            <n-form-item :label="t('profile.username')" path="username">
              <n-input :value="authStore.user?.username" disabled />
            </n-form-item>

            <n-form-item :label="t('profile.email')" path="email">
              <n-input v-model:value="form.email" />
            </n-form-item>

            <n-form-item :label="t('profile.avatarUrl')" path="avatar">
              <n-input v-model:value="form.avatar" placeholder="https://example.com/avatar.jpg" />
            </n-form-item>

            <n-form-item>
              <n-button type="primary" :loading="loading" @click="handleSubmit">
                {{ t('common.save') }}
              </n-button>
            </n-form-item>
          </n-form>
        </n-card>

        <n-card hoverable class="preference-card">
          <template #header>
            <span>{{ t('profile.preferences') }}</span>
          </template>

          <div class="preference-item">
            <div class="preference-label">
              <span>{{ t('profile.darkMode') }}</span>
            </div>
            <n-switch
              :value="authStore.isDark"
              @update:value="authStore.setDarkMode"
            />
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<style scoped lang="scss">
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
}

.profile-grid {
  :deep(.n-grid-item__content) {
    display: flex;
    flex-direction: column;
  }
}

.profile-left {
  :deep(.n-card) {
    height: 100%;
  }
}

.profile-avatar {
  text-align: center;
  padding: 20px 0;

  :deep(.n-avatar) {
    margin-bottom: 12px;
  }

  h3 {
    margin: 8px 0;
    font-size: 20px;
  }
}

.profile-info {
  .info-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 12px 0;
    border-bottom: 1px solid var(--n-border-color);

    &:last-child {
      border-bottom: none;
    }

    .label {
      font-size: 12px;
      color: var(--n-text-color-3);
    }

    .value {
      font-weight: 500;
      word-break: break-all;
    }
  }
}

.profile-right {
  :deep(.n-card) {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.preference-card {
  .preference-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;

    .preference-label {
      font-weight: 500;
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .profile-page {
    .page-title {
      font-size: 20px;
      text-align: center;
      margin-bottom: 20px;
    }

    .profile-grid {
      display: flex !important;
      flex-direction: column;
      gap: 16px !important;

      :deep(.n-grid-item) {
        grid-column: span 1 !important;
      }
    }

    .profile-avatar {
      padding: 16px 0;

      :deep(.n-avatar) {
        width: 80px !important;
        height: 80px !important;
      }

      h3 {
        font-size: 18px;
        margin: 8px 0;
      }
    }

    .profile-info {
      .info-item {
        flex-direction: row;
        justify-content: space-between;
        align-items: center;

        .label {
          font-size: 13px;
        }

        .value {
          font-size: 14px;
        }
      }
    }

    .preference-item {
      padding: 12px 0;
    }
  }
}
</style>
