<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { useMessage, useDialog } from 'naive-ui'
import { useTaskStore } from '@/stores/task'
import type { Task } from '@/types'
import { Add, Trash, Time, List } from '@vicons/ionicons5'

const { t } = useI18n()
const message = useMessage()
const dialog = useDialog()
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
  dueDate: number | null
  categoryId: number | null
}>({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'PENDING',
  dueDate: null,
  categoryId: null
})

const priorityOptions = computed(() => [
  { label: t('task.priorities.low'), value: 'LOW', color: '#909399' },
  { label: t('task.priorities.medium'), value: 'MEDIUM', color: '#18a058' },
  { label: t('task.priorities.high'), value: 'HIGH', color: '#f0a020' },
  { label: t('task.priorities.urgent'), value: 'URGENT', color: '#d03050' }
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
      dueDate: task.dueDate ? new Date(task.dueDate).getTime() : null,
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
      dueDate: null,
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
      message.success(t('common.success'))
    } else {
      await taskStore.createTask(data)
      message.success(t('common.success'))
    }
    dialogVisible.value = false
  } catch (error: any) {
    message.error(error.response?.data?.message || t('common.failed'))
  }
}

async function handleDelete(task: Task) {
  dialog.warning({
    title: t('common.confirm'),
    content: t('common.confirm') + '?',
    positiveText: t('common.delete'),
    negativeText: t('common.cancel'),
    onPositiveClick: async () => {
      await taskStore.deleteTask(task.id)
      message.success(t('common.success'))
    }
  })
}

async function toggleTaskStatus(task: Task) {
  const newStatus = task.status === 'COMPLETED' ? 'PENDING' : 'COMPLETED'
  try {
    await taskStore.updateTask(task.id, { status: newStatus })
    message.success(t('common.success'))
  } catch (error: any) {
    message.error(t('common.failed'))
  }
}

function getPriorityColor(priority: string) {
  const option = priorityOptions.value.find((o: any) => o.value === priority)
  return option?.color || '#18a058'
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
        <n-input
          v-model:value="searchKeyword"
          :placeholder="t('common.search')"
          style="width: 200px"
          clearable
        />
        <n-select
          v-model:value="filterStatus"
          :placeholder="t('common.filter')"
          style="width: 150px"
          clearable
          :options="[
            { label: t('common.filter'), value: '' },
            { label: t('task.statuses.pending'), value: 'PENDING' },
            { label: t('task.statuses.inProgress'), value: 'IN_PROGRESS' },
            { label: t('task.statuses.completed'), value: 'COMPLETED' }
          ]"
        />
        <n-button type="primary" @click="openDialog()">
          <template #icon>
            <n-icon :component="Add" />
          </template>
          {{ t('task.newTask') }}
        </n-button>
      </div>
    </div>

    <div class="task-list">
      <n-card
        v-for="task in filteredTasks"
        :key="task.id"
        hoverable
        class="task-card"
        :class="{ completed: task.status === 'COMPLETED', overdue: isOverdue(task) }"
      >
        <div class="task-content">
          <n-checkbox
            :checked="task.status === 'COMPLETED'"
            @update:checked="toggleTaskStatus(task)"
          />
          <div class="task-info" @click="openDialog(task)">
            <div class="task-title">
              <span :class="{ 'line-through': task.status === 'COMPLETED' }">{{ task.title }}</span>
              <n-tag :color="{ color: getPriorityColor(task.priority), textColor: '#fff' }" size="small">
                {{ task.priority }}
              </n-tag>
            </div>
            <div v-if="task.description" class="task-description">
              {{ task.description }}
            </div>
            <div class="task-meta">
              <n-tag v-if="task.dueDate" size="small" :type="isOverdue(task) ? 'error' : 'default'">
                <template #icon>
                  <n-icon :component="Time" />
                </template>
                {{ formatDueDate(task.dueDate) }}
              </n-tag>
              <n-tag v-if="task.category" size="small">
                {{ task.category.name }}
              </n-tag>
              <span class="subtask-count" v-if="task.subTasks?.length">
                <n-icon :component="List" />
                {{ task.subTasks.filter(s => s.isCompleted).length }}/{{ task.subTasks.length }}
              </span>
            </div>
          </div>
          <div class="task-actions">
            <n-button text type="error" @click.stop="handleDelete(task)">
              <template #icon>
                <n-icon :component="Trash" />
              </template>
            </n-button>
          </div>
        </div>
      </n-card>

      <n-empty v-if="filteredTasks.length === 0" :description="t('common.noData')" />
    </div>

    <n-modal
      v-model:show="dialogVisible"
      :title="isEdit ? t('task.editTask') : t('task.newTask')"
      preset="card"
      style="width: 500px"
    >
      <n-form :model="form" label-placement="left" label-width="100px">
        <n-form-item :label="t('task.taskTitle')" required>
          <n-input v-model:value="form.title" :placeholder="t('task.taskTitle')" />
        </n-form-item>

        <n-form-item :label="t('task.description')">
          <n-input v-model:value="form.description" type="textarea" :rows="3" />
        </n-form-item>

        <n-form-item :label="t('task.priority')">
          <n-select v-model:value="form.priority" :options="priorityOptions" />
        </n-form-item>

        <n-form-item :label="t('task.status')">
          <n-select v-model:value="form.status" :options="statusOptions" />
        </n-form-item>

        <n-form-item :label="t('task.dueDate')">
          <n-date-picker
            v-model:value="form.dueDate"
            type="datetime"
            :placeholder="t('task.dueDate')"
            style="width: 100%"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <div class="dialog-footer">
          <n-button @click="dialogVisible = false">{{ t('common.cancel') }}</n-button>
          <n-button type="primary" @click="handleSubmit">{{ isEdit ? t('common.edit') : t('common.create') }}</n-button>
        </div>
      </template>
    </n-modal>
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
    border-left: 3px solid #d03050;
  }

  .task-content {
    display: flex;
    align-items: flex-start;
    gap: 12px;
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
  }

  .task-description {
    font-size: 14px;
    color: var(--n-text-color-3);
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
      color: var(--n-text-color-3);
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
