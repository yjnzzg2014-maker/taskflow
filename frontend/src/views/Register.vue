<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value !== form.password) {
    callback(new Error(t('auth.passwordMismatch')))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: t('auth.username'), trigger: 'blur' },
    { min: 3, max: 50, message: t('auth.usernameLength'), trigger: 'blur' }
  ],
  email: [
    { required: true, message: t('auth.email'), trigger: 'blur' },
    { type: 'email', message: t('auth.emailValid'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('auth.password'), trigger: 'blur' },
    { min: 6, max: 100, message: t('auth.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('auth.confirmPassword'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.register({
          username: form.username,
          email: form.email,
          password: form.password
        })
        ElMessage.success(t('auth.registerSuccess'))
        router.push('/')
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || t('auth.registerFailed'))
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <el-icon :size="48" color="#409eff"><Calendar /></el-icon>
        <h1>TaskFlow</h1>
        <p>{{ t('auth.register') }}</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            :placeholder="t('auth.username')"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            :placeholder="t('auth.email')"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="t('auth.password')"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            :placeholder="t('auth.confirmPassword')"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            {{ t('auth.register') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>{{ t('auth.hasAccount') }}</span>
        <router-link to="/login">{{ t('auth.login') }}</router-link>
      </div>
    </div>
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
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

  .dark & {
    background: #1a1a1a;
  }
}

.register-header {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    font-size: 28px;
    font-weight: 600;
    margin: 16px 0 8px;
    color: #303133;

    .dark & {
      color: #e5e5e5;
    }
  }

  p {
    color: #909399;
    font-size: 14px;
  }
}

.register-form {
  .register-btn {
    width: 100%;
  }
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  color: #909399;

  a {
    color: #409eff;
    margin-left: 4px;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
