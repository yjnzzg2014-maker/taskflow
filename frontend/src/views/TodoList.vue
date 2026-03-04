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
const sortBy = ref<string>('priority')

const sortOptions = computed(() => [
  { label: t('task.sort.priority'), value: 'priority' },
  { label: t('task.sort.dueDate'), value: 'dueDate' },
  { label: t('task.sort.createdAt'), value: 'createdAt' },
  { label: t('task.sort.title'), value: 'title' }
])

const form = ref<{
  title: string
  description: string
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'
  dueDate: number | null
  categoryId: number | null
  repeatType: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'
  repeatInterval: number | null
  repeatEndDate: number | null
}>({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'PENDING',
  dueDate: null,
  categoryId: null,
  repeatType: 'NONE',
  repeatInterval: null,
  repeatEndDate: null
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

const repeatOptions = computed(() => [
  { label: t('task.repeatTypes.none'), value: 'NONE' },
  { label: t('task.repeatTypes.daily'), value: 'DAILY' },
  { label: t('task.repeatTypes.weekly'), value: 'WEEKLY' },
  { label: t('task.repeatTypes.monthly'), value: 'MONTHLY' },
  { label: t('task.repeatTypes.yearly'), value: 'YEARLY' }
])

const showRepeatSettings = computed(() => form.value.repeatType !== 'NONE')

const filteredTasks = computed(() => {
  let tasks = [...taskStore.tasks]

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

  // Sort tasks
  const priorityOrder = { 'URGENT': 0, 'HIGH': 1, 'MEDIUM': 2, 'LOW': 3 }

  tasks.sort((a, b) => {
    switch (sortBy.value) {
      case 'priority':
        // Completed tasks go to bottom
        if (a.status === 'COMPLETED' && b.status !== 'COMPLETED') return 1
        if (b.status === 'COMPLETED' && a.status !== 'COMPLETED') return -1
        return (priorityOrder[a.priority] || 3) - (priorityOrder[b.priority] || 3)
      case 'dueDate':
        if (a.status === 'COMPLETED' && b.status !== 'COMPLETED') return 1
        if (b.status === 'COMPLETED' && a.status !== 'COMPLETED') return -1
        if (!a.dueDate) return 1
        if (!b.dueDate) return -1
        return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime()
      case 'createdAt':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'title':
        return a.title.localeCompare(b.title)
      default:
        return 0
    }
  })

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
      categoryId: task.category?.id || null,
      repeatType: (task as any).repeatType || 'NONE',
      repeatInterval: (task as any).repeatInterval || null,
      repeatEndDate: (task as any).repeatEndDate ? new Date((task as any).repeatEndDate).getTime() : null
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
      categoryId: null,
      repeatType: 'NONE',
      repeatInterval: null,
      repeatEndDate: null
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
      dueDate: form.value.dueDate ? dayjs(form.value.dueDate).format('YYYY-MM-DDTHH:mm:ss') : undefined,
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
          style="width: 150px"
          clearable
        />
        <n-select
          v-model:value="sortBy"
          :options="sortOptions"
          style="width: 150px"
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

        <n-form-item :label="t('task.repeat')">
          <n-select v-model:value="form.repeatType" :options="repeatOptions" />
        </n-form-item>

        <template v-if="showRepeatSettings">
          <n-form-item :label="t('task.repeatInterval')">
            <n-input-number v-model:value="form.repeatInterval" :min="1" :max="30" />
          </n-form-item>

          <n-form-item :label="t('task.repeatEndDate')">
            <n-date-picker
              v-model:value="form.repeatEndDate"
              type="date"
              :placeholder="t('task.repeatEndDate')"
              clearable
              style="width: 100%"
            />
          </n-form-item>
        </template>
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

// 移动端适配
@media (max-width: 768px) {
  .todo-page {
    padding: 0;

    .todo-header {
      flex-direction: column;
      align-items: stretch;
      gap: 12px;
      margin-bottom: 16px;

      .page-title {
        font-size: 18px;
        text-align: center;
      }

      .todo-actions {
        flex-wrap: wrap;
        justify-content: center;
        gap: 8px;
      }
    }

    .task-list {
      gap: 8px;
    }

    .task-card {
      :deep(.n-card__content) {
        padding: 12px;
      }

      .task-content {
        gap: 8px;
      }

      .task-checkbox {
        flex-shrink: 0;
      }

      .task-info {
        flex: 1;
        min-width: 0;

        .task-title {
          font-size: 14px;
          word-break: break-word;
        }

        .task-meta {
          flex-wrap: wrap;
          gap: 6px;

          .meta-item {
            font-size: 11px;
          }
        }
      }

      .task-tags {
        flex-wrap: wrap;
        gap: 4px;
        margin-top: 8px;

        :deep(.n-tag) {
          font-size: 10px;
          padding: 0 6px;
          height: 20px;
        }
      }

      .task-actions {
        flex-shrink: 0;
      }
    }
  }
}

@media (max-width: 480px) {
  .todo-page {
    .task-card {
      .task-tags {
        display: none;
      }

      .task-actions {
        display: none;
      }
    }
  }
}
</style>
