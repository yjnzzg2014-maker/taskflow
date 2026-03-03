<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { Calendar } from '@vicons/ionicons5'

console.log('=== Login.vue mounted ===')
console.log('localStorage token:', localStorage.getItem('token'))

onMounted(() => {
  console.log('Login onMounted')
})

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const message = useMessage()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: {
    required: true,
    message: t('auth.username'),
    trigger: 'blur'
  },
  password: {
    required: true,
    message: t('auth.password'),
    trigger: 'blur'
  }
}

async function handleLogin(event: Event) {
  console.log('=== handleLogin called ===')
  event.preventDefault()
  event.stopPropagation()

  if (!form.username || !form.password) {
    message.error('请输入用户名和密码')
    return
  }

  try {
    loading.value = true
    await authStore.login(form)
    message.success(t('auth.loginSuccess'))
    router.push('/')
  } catch (error: any) {
    console.error('Login error:', error)
    if (error?.response) {
      message.error(error.response?.data?.message || t('auth.loginFailed'))
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <n-card class="login-box">
      <div class="login-header">
        <n-icon :size="48" color="#18a058">
          <Calendar />
        </n-icon>
        <h1>TaskFlow</h1>
        <p>{{ t('common.language') }}</p>
      </div>

      <n-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <n-form-item path="username" :label="t('auth.username')">
          <n-input
            v-model:value="form.username"
            :placeholder="t('auth.username')"
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
            @keyup.enter="handleLogin"
          />
        </n-form-item>

        <n-form-item>
          <button
            type="button"
            class="login-btn-native"
            :disabled="loading"
            @click="(e) => handleLogin(e)"
          >
            {{ loading ? '登录中...' : t('auth.login') }}
          </button>
        </n-form-item>
      </n-form>

      <div class="login-footer">
        <span>{{ t('auth.noAccount') }}</span>
        <router-link to="/register">{{ t('auth.register') }}</router-link>
      </div>
    </n-card>
  </div>
</template>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
}

.login-header {
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

.login-btn {
  width: 100%;
}

.login-btn-native {
  width: 100%;
  padding: 12px;
  background: #18a058;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.login-btn-native:hover {
  background: #159954;
}

.login-btn-native:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.login-footer {
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
