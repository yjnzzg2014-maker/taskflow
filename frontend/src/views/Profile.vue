<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const message = useMessage()
const authStore = useAuthStore()
const loading = ref(false)

const form = ref({
  email: '',
  avatar: ''
})

onMounted(() => {
  if (authStore.user) {
    form.value.email = authStore.user.email
    form.value.avatar = authStore.user.avatar || ''
  }
})

async function handleSubmit() {
  message.success(t('common.success'))
}
</script>

<template>
  <div class="profile-page">
    <h1 class="page-title">{{ t('profile.title') }}</h1>

    <n-grid :cols="24" :x-gap="20">
      <n-grid-item :span="8">
        <n-card hoverable>
          <div class="profile-avatar">
            <n-avatar :size="120" round>
              {{ authStore.user?.username?.[0]?.toUpperCase() }}
            </n-avatar>
            <h3>{{ authStore.user?.username }}</h3>
            <n-tag>{{ authStore.user?.role }}</n-tag>
          </div>
          <n-divider />
          <div class="profile-info">
            <div class="info-item">
              <span class="label">{{ t('profile.email') }}:</span>
              <span class="value">{{ authStore.user?.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ t('profile.memberSince') }}:</span>
              <span class="value">{{ authStore.user?.createdAt }}</span>
            </div>
          </div>
        </n-card>
      </n-grid-item>

      <n-grid-item :span="16">
        <n-card hoverable>
          <template #header>
            <span>{{ t('profile.editProfile') }}</span>
          </template>

          <n-form :model="form" label-placement="left" label-width="100px">
            <n-form-item :label="t('profile.username')">
              <n-input :value="authStore.user?.username" disabled />
            </n-form-item>

            <n-form-item :label="t('profile.email')">
              <n-input v-model:value="form.email" />
            </n-form-item>

            <n-form-item :label="t('profile.avatarUrl')">
              <n-input v-model:value="form.avatar" placeholder="https://..." />
            </n-form-item>

            <n-form-item>
              <n-button type="primary" :loading="loading" @click="handleSubmit">
                {{ t('common.save') }}
              </n-button>
            </n-form-item>
          </n-form>
        </n-card>

        <n-card hoverable style="margin-top: 20px">
          <template #header>
            <span>{{ t('profile.preferences') }}</span>
          </template>

          <div class="preference-item">
            <span>{{ t('profile.darkMode') }}</span>
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

.profile-avatar {
  text-align: center;
  padding: 20px 0;

  h3 {
    margin: 16px 0 8px;
    font-size: 20px;
  }
}

.profile-info {
  .info-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid var(--n-border-color);

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: var(--n-text-color-3);
    }

    .value {
      font-weight: 500;
    }
  }
}

.preference-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}
</style>
