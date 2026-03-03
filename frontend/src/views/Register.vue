<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { Calendar } from '@vicons/ionicons5'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const message = useMessage()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = () => {
  if (form.confirmPassword !== form.password) {
    return new Error(t('auth.passwordMismatch'))
  }
}

const rules = {
  username: {
    required: true,
    message: t('auth.username'),
    trigger: 'blur'
  },
  email: [
    {
      required: true,
      message: t('auth.email'),
      trigger: 'blur'
    },
    {
      type: 'email' as const,
      message: t('auth.emailValid'),
      trigger: 'blur'
    }
  ],
  password: {
    required: true,
    message: t('auth.password'),
    trigger: 'blur'
  },
  confirmPassword: {
    required: true,
    message: t('auth.confirmPassword'),
    trigger: 'blur',
    validator: validateConfirmPassword
  }
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
    loading.value = true
    await authStore.register({
      username: form.username,
      email: form.email,
      password: form.password
    })
    message.success(t('auth.registerSuccess'))
    router.push('/')
  } catch (error: any) {
    if (error?.response) {
      message.error(error.response?.data?.message || t('auth.registerFailed'))
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <n-card class="register-box">
      <div class="register-header">
        <n-icon :size="48" color="#18a058">
          <Calendar />
        </n-icon>
        <h1>TaskFlow</h1>
        <p>{{ t('auth.register') }}</p>
      </div>

      <n-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <n-form-item path="username" :label="t('auth.username')">
          <n-input
            v-model:value="form.username"
            :placeholder="t('auth.username')"
            size="large"
          />
        </n-form-item>

        <n-form-item path="email" :label="t('auth.email')">
          <n-input
            v-model:value="form.email"
            :placeholder="t('auth.email')"
            size="large"
          />
        </n-form-item>

        <n-form-item path="password" :label="t('auth.password')">
          <n-input
            v-model:value="form.password"
            type="password"
            :placeholder="t('auth.password')"
            size="large"
            show-password-on="click"
          />
        </n-form-item>

        <n-form-item path="confirmPassword" :label="t('auth.confirmPassword')">
          <n-input
            v-model:value="form.confirmPassword"
            type="password"
            :placeholder="t('auth.confirmPassword')"
            size="large"
            show-password-on="click"
            @keyup.enter="handleRegister"
          />
        </n-form-item>

        <n-form-item>
          <n-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-btn"
            block
            @click="handleRegister"
          >
            {{ t('auth.register') }}
          </n-button>
        </n-form-item>
      </n-form>

      <div class="register-footer">
        <span>{{ t('auth.hasAccount') }}</span>
        <router-link to="/login">{{ t('auth.login') }}</router-link>
      </div>
    </n-card>
  </div>
</template>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 400px;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    font-size: 28px;
    font-weight: 600;
    margin: 16px 0 8px;
    color: var(--n-text-color);
  }

  p {
    color: var(--n-text-color-3);
    font-size: 14px;
  }
}

.register-btn {
  width: 100%;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  color: var(--n-text-color-3);

  a {
    color: #18a058;
    margin-left: 4px;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
