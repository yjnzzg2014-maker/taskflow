<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useTaskStore } from '@/stores/task'
import type { Task } from '@/types'

const { t } = useI18n()
const taskStore = useTaskStore()

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentTask = ref<Partial<Task>>({})
const filterStatus = ref<string>('')
const searchKeyword = ref('')

const form = ref<{
  title: string
  description: string
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'
  dueDate: string
  categoryId: number | null
}>({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'PENDING',
  dueDate: '',
  categoryId: null
})

const priorityOptions = computed(() => [
  { label: t('task.priorities.low'), value: 'LOW', color: '#909399' },
  { label: t('task.priorities.medium'), value: 'MEDIUM', color: '#409eff' },
  { label: t('task.priorities.high'), value: 'HIGH', color: '#e6a23c' },
  { label: t('task.priorities.urgent'), value: 'URGENT', color: '#f56c6c' }
])

const statusOptions = computed(() => [
  { label: t('task.statuses.pending'), value: 'PENDING' },
  { label: t('task.statuses.inProgress'), value: 'IN_PROGRESS' },
  { label: t('task.statuses.completed'), value: 'COMPLETED' }
])

const filteredTasks = computed(() => {
  let tasks = taskStore.tasks

  if (filterStatus.value) {
    tasks = tasks.filter(t => t.status === filterStatus.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    tasks = tasks.filter(t =>
      t.title.toLowerCase().includes(keyword) ||
      t.description?.toLowerCase().includes(keyword)
    )
  }

  return tasks
})

onMounted(async () => {
  await taskStore.fetchTasks()
})

function openDialog(task?: Task) {
  if (task) {
    isEdit.value = true
    currentTask.value = task
    form.value = {
      title: task.title,
      description: task.description || '',
      priority: task.priority,
      status: task.status,
      dueDate: task.dueDate || '',
      categoryId: task.category?.id || null
    }
  } else {
    isEdit.value = false
    currentTask.value = {}
    form.value = {
      title: '',
      description: '',
      priority: 'MEDIUM',
      status: 'PENDING',
      dueDate: '',
      categoryId: null
    }
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    const data = {
      title: form.value.title,
      description: form.value.description,
      priority: form.value.priority,
      status: form.value.status,
      dueDate: form.value.dueDate ? dayjs(form.value.dueDate).toISOString() : undefined,
      categoryId: form.value.categoryId || undefined
    }

    if (isEdit.value && currentTask.value.id) {
      await taskStore.updateTask(currentTask.value.id, data)
      ElMessage.success(t('common.success'))
    } else {
      await taskStore.createTask(data)
      ElMessage.success(t('common.success'))
    }
    dialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || t('common.failed'))
  }
}

async function handleDelete(task: Task) {
  try {
    await ElMessageBox.confirm(t('common.confirm') + '?', 'Warning', {
      type: 'warning'
    })
    await taskStore.deleteTask(task.id)
    ElMessage.success(t('common.success'))
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || t('common.failed'))
    }
  }
}

async function toggleTaskStatus(task: Task) {
  const newStatus = task.status === 'COMPLETED' ? 'PENDING' : 'COMPLETED'
  try {
    await taskStore.updateTask(task.id, { status: newStatus })
    ElMessage.success(t('common.success'))
  } catch (error: any) {
    ElMessage.error(t('common.failed'))
  }
}

function getPriorityColor(priority: string) {
  const option = priorityOptions.value.find((o: any) => o.value === priority)
  return option?.color || '#409eff'
}

function formatDueDate(date?: string) {
  if (!date) return ''
  return dayjs(date).format('MMM DD, YYYY')
}

function isOverdue(task: Task): boolean {
  if (!task.dueDate || task.status === 'COMPLETED') return false
  return dayjs(task.dueDate).isBefore(dayjs(), 'day')
}
</script>

<template>
  <div class="todo-page">
    <div class="todo-header">
      <h1 class="page-title">{{ t('task.title') }}</h1>
      <div class="todo-actions">
        <el-input
          v-model="searchKeyword"
          :placeholder="t('common.search')"
          prefix-icon="Search"
          style="width: 200px"
          clearable
        />
        <el-select v-model="filterStatus" :placeholder="t('common.filter')" style="width: 150px" clearable>
          <el-option :label="t('common.filter')" value="" />
          <el-option :label="t('task.statuses.pending')" value="PENDING" />
          <el-option :label="t('task.statuses.inProgress')" value="IN_PROGRESS" />
          <el-option :label="t('task.statuses.completed')" value="COMPLETED" />
        </el-select>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          {{ t('task.newTask') }}
        </el-button>
      </div>
    </div>

    <div class="task-list">
      <el-card
        v-for="task in filteredTasks"
        :key="task.id"
        shadow="hover"
        class="task-card"
        :class="{ completed: task.status === 'COMPLETED', overdue: isOverdue(task) }"
      >
        <div class="task-content">
          <el-checkbox
            :model-value="task.status === 'COMPLETED'"
            @change="toggleTaskStatus(task)"
            class="task-checkbox"
          />
          <div class="task-info" @click="openDialog(task)">
            <div class="task-title">
              <span :class="{ 'line-through': task.status === 'COMPLETED' }">{{ task.title }}</span>
              <el-tag :color="getPriorityColor(task.priority)" size="small" class="priority-tag">
                {{ task.priority }}
              </el-tag>
            </div>
            <div v-if="task.description" class="task-description">
              {{ task.description }}
            </div>
            <div class="task-meta">
              <el-tag v-if="task.dueDate" size="small" :type="isOverdue(task) ? 'danger' : 'info'">
                <el-icon><Clock /></el-icon>
                {{ formatDueDate(task.dueDate) }}
              </el-tag>
              <el-tag v-if="task.category" size="small">
                {{ task.category.name }}
              </el-tag>
              <span class="subtask-count" v-if="task.subTasks?.length">
                <el-icon><List /></el-icon>
                {{ task.subTasks.filter(s => s.isCompleted).length }}/{{ task.subTasks.length }}
              </span>
            </div>
          </div>
          <div class="task-actions">
            <el-button type="danger" text @click.stop="handleDelete(task)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </el-card>

      <el-empty v-if="filteredTasks.length === 0" :description="t('common.noData')" />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? t('task.editTask') : t('task.newTask')"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item :label="t('task.taskTitle')" required>
          <el-input v-model="form.title" :placeholder="t('task.taskTitle')" />
        </el-form-item>

        <el-form-item :label="t('task.description')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item :label="t('task.priority')">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option
              v-for="option in priorityOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item :label="t('task.status')">
          <el-select v-model="form.status" style="width: 100%">
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item :label="t('task.dueDate')">
          <el-date-picker
            v-model="form.dueDate"
            type="datetime"
            :placeholder="t('task.dueDate')"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ isEdit ? t('common.edit') : t('common.create') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.todo-page {
  max-width: 1200px;
  margin: 0 auto;
}

.todo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .page-title {
    margin: 0;
  }

  .todo-actions {
    display: flex;
    gap: 12px;
  }
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-card {
  transition: all 0.2s;

  &.completed {
    opacity: 0.7;

    .task-title {
      text-decoration: line-through;
    }
  }

  &.overdue {
    border-left: 3px solid #f56c6c;
  }

  .task-content {
    display: flex;
    align-items: flex-start;
    gap: 12px;
  }

  .task-checkbox {
    margin-top: 4px;
  }

  .task-info {
    flex: 1;
    cursor: pointer;
  }

  .task-title {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 4px;
    display: flex;
    align-items: center;
    gap: 8px;

    .priority-tag {
      color: #fff;
      border: none;
    }
  }

  .task-description {
    font-size: 14px;
    color: #606266;
    margin-bottom: 8px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .task-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;

    .subtask-count {
      font-size: 12px;
      color: #909399;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .task-actions {
    display: flex;
    gap: 4px;
  }
}

.line-through {
  text-decoration: line-through;
}
</style>
