<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

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
  // Implement profile update logic
  ElMessage.success('Profile updated successfully')
}
</script>

<template>
  <div class="profile-page">
    <h1 class="page-title">Profile</h1>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="profile-avatar">
            <el-avatar :size="120">
              {{ authStore.user?.username?.[0]?.toUpperCase() }}
            </el-avatar>
            <h3>{{ authStore.user?.username }}</h3>
            <el-tag>{{ authStore.user?.role }}</el-tag>
          </div>
          <el-divider />
          <div class="profile-info">
            <div class="info-item">
              <span class="label">Email:</span>
              <span class="value">{{ authStore.user?.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">Member since:</span>
              <span class="value">{{ authStore.user?.createdAt }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span>Edit Profile</span>
          </template>

          <el-form :model="form" label-width="100px">
            <el-form-item label="Username">
              <el-input :value="authStore.user?.username" disabled />
            </el-form-item>

            <el-form-item label="Email">
              <el-input v-model="form.email" />
            </el-form-item>

            <el-form-item label="Avatar URL">
              <el-input v-model="form.avatar" placeholder="https://..." />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleSubmit">
                Save Changes
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" style="margin-top: 20px">
          <template #header>
            <span>Preferences</span>
          </template>

          <div class="preference-item">
            <span>Dark Mode</span>
            <el-switch
              :model-value="authStore.isDark"
              @change="authStore.toggleDarkMode"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
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
    border-bottom: 1px solid var(--border-color);

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: #909399;
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
