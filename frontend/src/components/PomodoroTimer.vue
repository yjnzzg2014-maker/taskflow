<script setup lang="ts">
import { ref, computed, watch, onUnmounted, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { pomodoroApi } from '@/api'
import { useTaskStore } from '@/stores/task'

const { t } = useI18n()
const taskStore = useTaskStore()

// Timer state
const isRunning = ref(false)
const isPaused = ref(false)
const mode = ref<'work' | 'shortBreak' | 'longBreak'>('work')
const timeLeft = ref(25 * 60) // seconds
const totalTime = ref(25 * 60)
const sessionsCompleted = ref(0)
const selectedTaskId = ref<number | null>(null)
const showPanel = ref(false)

let timerInterval: ReturnType<typeof setInterval> | null = null

// Stats
const stats = ref<any>(null)

// Settings
const workDuration = ref(25)
const shortBreakDuration = ref(5)
const longBreakDuration = ref(15)
const autoStartBreaks = ref(false)
const autoStartWork = ref(false)
const soundEnabled = ref(true)

const modeLabel = computed(() => {
  switch (mode.value) {
    case 'work': return t('pomodoro.work')
    case 'shortBreak': return t('pomodoro.shortBreak')
    case 'longBreak': return t('pomodoro.longBreak')
  }
})

const progress = computed(() => {
  return ((totalTime.value - timeLeft.value) / totalTime.value) * 100
})

const formattedTime = computed(() => {
  const minutes = Math.floor(timeLeft.value / 60)
  const seconds = timeLeft.value % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

const modeColor = computed(() => {
  switch (mode.value) {
    case 'work': return '#f56c6c'
    case 'shortBreak': return '#67c23a'
    case 'longBreak': return '#409eff'
  }
})

const selectedTask = computed(() => {
  return taskStore.tasks.find(t => t.id === selectedTaskId.value)
})

// Fetch tasks and stats on mount
taskStore.fetchTasks()
fetchStats()

async function fetchStats() {
  try {
    const response = await pomodoroApi.getStats()
    stats.value = response.data
  } catch (e) {
    console.error('Failed to fetch pomodoro stats', e)
  }
}

function togglePanel() {
  showPanel.value = !showPanel.value
}

function closePanel() {
  showPanel.value = false
}

function startTimer() {
  if (isRunning.value && !isPaused.value) return

  isRunning.value = true
  isPaused.value = false

  timerInterval = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--
    } else {
      timerComplete()
    }
  }, 1000)
}

function pauseTimer() {
  isPaused.value = true
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

function resetTimer() {
  isRunning.value = false
  isPaused.value = false
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
  setDuration()
}

function setDuration() {
  switch (mode.value) {
    case 'work':
      timeLeft.value = workDuration.value * 60
      totalTime.value = workDuration.value * 60
      break
    case 'shortBreak':
      timeLeft.value = shortBreakDuration.value * 60
      totalTime.value = shortBreakDuration.value * 60
      break
    case 'longBreak':
      timeLeft.value = longBreakDuration.value * 60
      totalTime.value = longBreakDuration.value * 60
      break
  }
}

function switchMode(newMode: 'work' | 'shortBreak' | 'longBreak') {
  resetTimer()
  mode.value = newMode
  setDuration()
}

async function timerComplete() {
  resetTimer()

  if (soundEnabled.value) {
    playNotificationSound()
  }

  if (mode.value === 'work') {
    sessionsCompleted.value++
    ElMessage.success(t('pomodoro.workComplete'))

    // Save pomodoro record
    try {
      await pomodoroApi.createRecord({
        taskId: selectedTaskId.value || undefined,
        mode: 'WORK',
        duration: workDuration.value,
        notes: ''
      })
      fetchStats()
    } catch (e) {
      console.error('Failed to save pomodoro record', e)
    }

    // After work, take a break
    if (sessionsCompleted.value % 4 === 0) {
      switchMode('longBreak')
      if (autoStartBreaks.value) startTimer()
    } else {
      switchMode('shortBreak')
      if (autoStartBreaks.value) startTimer()
    }
  } else {
    ElMessage.success(t('pomodoro.breakComplete'))
    switchMode('work')
    if (autoStartWork.value) startTimer()
  }
}

function playNotificationSound() {
  try {
    const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()

    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)

    oscillator.frequency.value = 800
    oscillator.type = 'sine'
    gainNode.gain.value = 0.3

    oscillator.start()
    setTimeout(() => {
      oscillator.stop()
      audioContext.close()
    }, 500)
  } catch (e) {
    console.log('Audio not supported')
  }
}

// Close panel when clicking outside
function handleClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (!target.closest('.pomodoro-wrapper')) {
    closePanel()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  if (timerInterval) {
    clearInterval(timerInterval)
  }
})

// Watch for mode changes
watch(mode, () => {
  setDuration()
})
</script>

<template>
  <div class="pomodoro-wrapper">
    <div class="timer-display" @click="togglePanel">
      <div class="timer-ring" :style="{ '--progress': progress, '--color': modeColor }">
        <span class="timer-time">{{ formattedTime }}</span>
      </div>
    </div>

    <!-- Custom Panel -->
    <Transition name="fade">
      <div v-if="showPanel" class="pomodoro-panel">
        <!-- Stats Section -->
        <div class="stats-section" v-if="stats">
          <div class="stat-item">
            <span class="stat-label">{{ t('pomodoro.today') }}</span>
            <span class="stat-value">{{ stats.todaySessions }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">{{ t('pomodoro.todayMinutes') }}</span>
            <span class="stat-value">{{ stats.todayMinutes }}min</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">{{ t('pomodoro.week') }}</span>
            <span class="stat-value">{{ stats.weekSessions }}</span>
          </div>
        </div>

        <!-- Task Selection -->
        <div class="task-selection" v-if="mode === 'work'">
          <el-select
            v-model="selectedTaskId"
            :placeholder="t('pomodoro.selectTask')"
            clearable
            size="small"
            style="width: 100%"
            @click.stop
          >
            <el-option
              v-for="task in taskStore.tasks"
              :key="task.id"
              :label="task.title"
              :value="task.id"
            />
          </el-select>
          <div class="selected-task" v-if="selectedTask">
            <el-tag size="small" type="info">{{ selectedTask.title }}</el-tag>
          </div>
        </div>

        <div class="timer-display-large">
          <div class="timer-ring" :style="{ '--progress': progress, '--color': modeColor }">
            <div class="timer-inner">
              <span class="timer-mode">{{ modeLabel }}</span>
              <span class="timer-time-large">{{ formattedTime }}</span>
            </div>
          </div>
        </div>

        <div class="timer-controls">
          <el-button
            v-if="!isRunning"
            type="primary"
            circle
            size="small"
            @click.stop="startTimer"
          >
            <el-icon><VideoPlay /></el-icon>
          </el-button>
          <el-button
            v-else-if="!isPaused"
            type="warning"
            circle
            size="small"
            @click.stop="pauseTimer"
          >
            <el-icon><VideoPause /></el-icon>
          </el-button>
          <el-button
            v-else
            type="primary"
            circle
            size="small"
            @click.stop="startTimer"
          >
            <el-icon><VideoPlay /></el-icon>
          </el-button>
          <el-button circle size="small" @click.stop="resetTimer">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>

        <div class="mode-switcher">
          <el-button
            :type="mode === 'work' ? 'danger' : 'default'"
            size="small"
            @click.stop="switchMode('work')"
          >
            {{ t('pomodoro.work') }}
          </el-button>
          <el-button
            :type="mode === 'shortBreak' ? 'success' : 'default'"
            size="small"
            @click.stop="switchMode('shortBreak')"
          >
            {{ t('pomodoro.shortBreak') }}
          </el-button>
          <el-button
            :type="mode === 'longBreak' ? 'primary' : 'default'"
            size="small"
            @click.stop="switchMode('longBreak')"
          >
            {{ t('pomodoro.longBreak') }}
          </el-button>
        </div>

        <div class="session-count">
          {{ t('pomodoro.sessions') }}: {{ sessionsCompleted }}
        </div>

        <el-divider style="margin: 12px 0" />

        <div class="settings-section">
          <el-form label-width="100px" size="small">
            <el-form-item :label="t('pomodoro.workDuration')">
              <el-input-number v-model="workDuration" :min="1" :max="60" controls-position="right" @click.stop />
            </el-form-item>
            <el-form-item :label="t('pomodoro.shortBreakDuration')">
              <el-input-number v-model="shortBreakDuration" :min="1" :max="30" controls-position="right" @click.stop />
            </el-form-item>
            <el-form-item :label="t('pomodoro.longBreakDuration')">
              <el-input-number v-model="longBreakDuration" :min="1" :max="60" controls-position="right" @click.stop />
            </el-form-item>
            <el-form-item :label="t('pomodoro.autoStartBreaks')">
              <el-switch v-model="autoStartBreaks" @click.stop />
            </el-form-item>
            <el-form-item :label="t('pomodoro.autoStartWork')">
              <el-switch v-model="autoStartWork" @click.stop />
            </el-form-item>
            <el-form-item :label="t('pomodoro.sound')">
              <el-switch v-model="soundEnabled" @click.stop />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped lang="scss">
.pomodoro-wrapper {
  position: relative;
  display: inline-block;
}

.timer-display {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.timer-ring {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: conic-gradient(
    var(--color) calc(var(--progress) * 1%),
    var(--border-color) calc(var(--progress) * 1%)
  );
  display: flex;
  align-items: center;
  justify-content: center;
}

.timer-time {
  font-size: 9px;
  font-weight: 600;
  color: var(--text-color);
}

.pomodoro-panel {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  width: 300px;
  padding: 12px;
  background: var(--bg-color);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.stats-section {
  display: flex;
  justify-content: space-around;
  width: 100%;
  padding: 8px;
  background: var(--bg-color-secondary);
  border-radius: 8px;

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .stat-label {
      font-size: 10px;
      color: var(--text-color-secondary);
    }

    .stat-value {
      font-size: 16px;
      font-weight: 600;
      color: var(--primary-color);
    }
  }
}

.task-selection {
  width: 100%;

  .selected-task {
    margin-top: 4px;
    text-align: center;
  }
}

.timer-display-large {
  .timer-ring {
    width: 80px;
    height: 80px;
  }

  .timer-inner {
    width: 68px;
    height: 68px;
    border-radius: 50%;
    background: var(--bg-color);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  .timer-mode {
    font-size: 10px;
    color: var(--color);
    text-transform: uppercase;
  }

  .timer-time-large {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color);
  }
}

.timer-controls {
  display: flex;
  gap: 8px;
}

.mode-switcher {
  display: flex;
  gap: 4px;
}

.session-count {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.settings-section {
  width: 100%;

  :deep(.el-form-item) {
    margin-bottom: 8px;
  }

  :deep(.el-input-number) {
    width: 100px;
  }
}
</style>
