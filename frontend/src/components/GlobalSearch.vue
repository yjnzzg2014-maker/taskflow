<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useEventStore } from '@/stores/event'
import { useTaskStore } from '@/stores/task'
import { Search, Calendar, List } from '@vicons/ionicons5'

const { t } = useI18n()
const router = useRouter()
const eventStore = useEventStore()
const taskStore = useTaskStore()

const searchQuery = ref('')
const showSearch = ref(false)
const searchInputRef = ref<HTMLInputElement | null>(null)

const searchResults = computed(() => {
  if (!searchQuery.value || searchQuery.value.length < 2) {
    return { tasks: [], events: [] }
  }

  const query = searchQuery.value.toLowerCase()

  const tasks = taskStore.tasks
    .filter(task => task.title?.toLowerCase().includes(query) || task.description?.toLowerCase().includes(query))
    .slice(0, 5)
    .map(task => ({
      id: task.id,
      title: task.title,
      type: 'task' as const,
      date: task.dueDate
    }))

  const events = eventStore.events
    .filter(event => event.title?.toLowerCase().includes(query) || event.description?.toLowerCase().includes(query))
    .slice(0, 5)
    .map(event => ({
      id: event.id,
      title: event.title,
      type: 'event' as const,
      date: event.startTime
    }))

  return { tasks, events }
})

const hasResults = computed(() => {
  return searchResults.value.tasks.length > 0 || searchResults.value.events.length > 0
})

function toggleSearch() {
  showSearch.value = !showSearch.value
  if (showSearch.value) {
    setTimeout(() => {
      searchInputRef.value?.focus()
    }, 100)
  } else {
    searchQuery.value = ''
  }
}

function closeSearch() {
  showSearch.value = false
  searchQuery.value = ''
}

function goToItem(item: { type: string; id: number }) {
  if (item.type === 'task') {
    router.push('/tasks')
  } else {
    router.push('/calendar')
  }
  closeSearch()
}

onMounted(() => {
  taskStore.fetchTasks()
  eventStore.fetchEvents()

  document.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleGlobalKeydown)
})

function handleGlobalKeydown(event: KeyboardEvent) {
  // Ctrl/Cmd + K to open search
  if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
    event.preventDefault()
    toggleSearch()
  }
}
</script>

<template>
  <div class="search-wrapper">
    <n-button quaternary @click="toggleSearch">
      <template #icon>
        <n-icon :component="Search" />
      </template>
    </n-button>

    <Transition name="fade">
      <div v-if="showSearch" class="search-overlay" @click="closeSearch">
        <div class="search-modal" @click.stop>
          <div class="search-input-wrapper">
            <n-icon :component="Search" />
            <input
              ref="searchInputRef"
              v-model="searchQuery"
              type="text"
              :placeholder="t('search.placeholder')"
              class="search-input"
            />
            <span class="search-hint">ESC</span>
          </div>

          <div v-if="searchQuery.length >= 2" class="search-results">
            <div v-if="!hasResults" class="search-empty">
              {{ t('search.noResults') }}
            </div>

            <div v-if="searchResults.tasks.length > 0" class="search-section">
              <div class="search-section-title">
                <n-icon :component="List" :size="14" />
                {{ t('search.tasks') }}
              </div>
              <div
                v-for="task in searchResults.tasks"
                :key="task.id"
                class="search-item"
                @click="goToItem(task)"
              >
                <span class="item-title">{{ task.title }}</span>
              </div>
            </div>

            <div v-if="searchResults.events.length > 0" class="search-section">
              <div class="search-section-title">
                <n-icon :component="Calendar" :size="14" />
                {{ t('search.events') }}
              </div>
              <div
                v-for="event in searchResults.events"
                :key="event.id"
                class="search-item"
                @click="goToItem(event)"
              >
                <span class="item-title">{{ event.title }}</span>
              </div>
            </div>
          </div>

          <div v-else-if="searchQuery.length > 0 && searchQuery.length < 2" class="search-hint-text">
            {{ t('search.hint') }}
          </div>

          <div v-else class="search-tips">
            <div class="search-tip">{{ t('search.tip') }}</div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped lang="scss">
.search-wrapper {
  position: relative;
}

.search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 100px;
  z-index: 2000;
}

.search-modal {
  width: 100%;
  max-width: 500px;
  background: var(--n-color);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--n-border-color);

  .n-icon {
    color: var(--n-text-color-3);
  }

  .search-input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 16px;
    outline: none;
    color: var(--n-text-color);
  }

  .search-hint {
    font-size: 11px;
    padding: 2px 6px;
    background: var(--n-color-hover);
    border-radius: 4px;
    color: var(--n-text-color-3);
  }
}

.search-results {
  max-height: 400px;
  overflow-y: auto;
}

.search-empty {
  padding: 24px;
  text-align: center;
  color: var(--n-text-color-3);
}

.search-section {
  padding: 8px 0;

  .search-section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    font-size: 12px;
    font-weight: 600;
    color: var(--n-text-color-3);
    text-transform: uppercase;
  }
}

.search-item {
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: var(--n-color-hover);
  }

  .item-title {
    font-size: 14px;
  }
}

.search-hint-text {
  padding: 24px;
  text-align: center;
  color: var(--n-text-color-3);
  font-size: 13px;
}

.search-tips {
  padding: 16px;

  .search-tip {
    font-size: 12px;
    color: var(--n-text-color-3);
    text-align: center;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
