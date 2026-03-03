<script setup lang="ts">
import { ref, computed, watch, onUnmounted, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { pomodoroApi } from '@/api'
import { useTaskStore } from '@/stores/task'
import { Play, Pause, Refresh, Expand, Contract } from '@vicons/ionicons5'

const { t } = useI18n()
const message = useMessage()
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
const longBreakInterval = ref(4) // Long break after every X sessions
const selectedSound = ref('ding')

// Sound options
const soundOptions = [
  { label: '清脆提示音', value: 'ding' },
  { label: '柔和铃声', value: 'bell' },
  { label: '电子音', value: 'electronic' }
]

// Background sounds
const useBackgroundSound = ref(false)
const backgroundSoundType = ref('rain')
const backgroundVolume = ref(0.3)

const backgroundSoundOptions = [
  { label: '雨声', value: 'rain' },
  { label: '森林', value: 'forest' },
  { label: '海浪', value: 'ocean' },
  { label: '咖啡馆', value: 'cafe' },
  { label: '白噪音', value: 'white' }
]

// Fullscreen timer state
const fullscreenMode = ref(false)

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

  // Start background sound if enabled
  if (useBackgroundSound.value && mode.value === 'work') {
    playBackgroundSound()
  }

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
  stopBackgroundSound()
}

function resetTimer() {
  isRunning.value = false
  isPaused.value = false
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
  stopBackgroundSound()
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
    message.success(t('pomodoro.workComplete'))

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
    if (sessionsCompleted.value % longBreakInterval.value === 0) {
      switchMode('longBreak')
      if (autoStartBreaks.value) startTimer()
    } else {
      switchMode('shortBreak')
      if (autoStartBreaks.value) startTimer()
    }
  } else {
    message.success(t('pomodoro.breakComplete'))
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

    let frequency = 800
    if (selectedSound.value === 'bell') {
      frequency = 1000
    } else if (selectedSound.value === 'electronic') {
      frequency = 600
    }

    oscillator.frequency.value = frequency
    oscillator.type = selectedSound.value === 'electronic' ? 'square' : 'sine'
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

function playBackgroundSound() {
  stopBackgroundSound()

  // Use Web Audio API for generated sounds
  try {
    const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()

    // Create noise based on type
    const bufferSize = 2 * audioContext.sampleRate
    const noiseBuffer = audioContext.createBuffer(1, bufferSize, audioContext.sampleRate)
    const output = noiseBuffer.getChannelData(0)

    if (backgroundSoundType.value === 'white') {
      for (let i = 0; i < bufferSize; i++) {
        output[i] = Math.random() * 2 - 1
      }
    } else if (backgroundSoundType.value === 'rain') {
      for (let i = 0; i < bufferSize; i++) {
        output[i] = (Math.random() * 2 - 1) * 0.5
      }
    } else if (backgroundSoundType.value === 'forest') {
      for (let i = 0; i < bufferSize; i++) {
        output[i] = Math.sin(i * 0.01) * 0.3 + (Math.random() * 2 - 1) * 0.1
      }
    } else if (backgroundSoundType.value === 'ocean') {
      for (let i = 0; i < bufferSize; i++) {
        output[i] = Math.sin(i * 0.001) * 0.5 + (Math.random() * 2 - 1) * 0.2
      }
    } else {
      // Cafe - more random
      for (let i = 0; i < bufferSize; i++) {
        output[i] = (Math.random() * 2 - 1) * 0.4
      }
    }

    const whiteNoise = audioContext.createBufferSource()
    whiteNoise.buffer = noiseBuffer
    whiteNoise.loop = true

    const gainNode = audioContext.createGain()
    gainNode.gain.value = backgroundVolume.value * 0.1

    whiteNoise.connect(gainNode)
    gainNode.connect(audioContext.destination)
    whiteNoise.start()

    // Store for stopping later
    ;(window as any).__pomodoroBgAudio = { source: whiteNoise, context: audioContext, gain: gainNode }
  } catch (e) {
    console.log('Background sound not supported')
  }
}

function stopBackgroundSound() {
  try {
    const bgAudio = (window as any).__pomodoroBgAudio
    if (bgAudio) {
      bgAudio.source.stop()
      bgAudio.context.close()
      delete (window as any).__pomodoroBgAudio
    }
  } catch (e) {
    // Ignore
  }
}

function toggleFullscreen() {
  fullscreenMode.value = !fullscreenMode.value
  if (fullscreenMode.value) {
    showPanel.value = false
  }
}

// Close panel when clicking outside
function handleClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (!target.closest('.pomodoro-wrapper') && !target.closest('.fullscreen-timer')) {
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
  stopBackgroundSound()
})

// Watch for mode changes
watch(mode, () => {
  setDuration()
})

// Watch work duration changes
watch(workDuration, () => {
  if (!isRunning.value) {
    setDuration()
  }
})
</script>

<template>
  <div class="pomodoro-wrapper">
    <!-- Header Mini Display -->
    <div class="timer-display" @click="togglePanel">
      <div class="timer-ring" :style="{ '--progress': progress, '--color': modeColor }">
        <span class="timer-time">{{ formattedTime }}</span>
      </div>
    </div>

    <!-- Fullscreen Timer Modal -->
    <n-modal v-model:show="fullscreenMode" :mask-closable="false" class="fullscreen-modal">
      <div class="fullscreen-timer" :style="{ '--mode-color': modeColor }">
        <div class="fullscreen-header">
          <n-button quaternary circle @click="toggleFullscreen">
            <template #icon>
              <n-icon :component="Contract" />
            </template>
          </n-button>
        </div>

        <div class="fullscreen-content">
          <div class="fullscreen-task" v-if="selectedTask">
            {{ selectedTask.title }}
          </div>
          <div class="fullscreen-mode">{{ modeLabel }}</div>
          <div class="fullscreen-time">{{ formattedTime }}</div>
          <div class="fullscreen-progress">
            <n-progress
              type="line"
              :percentage="progress"
              :height="8"
              :rail-color="'rgba(255,255,255,0.2)'"
              :indicator-placement="'inside'"
            />
          </div>
          <div class="fullscreen-controls">
            <n-button
              v-if="!isRunning"
              type="primary"
              size="large"
              circle
              @click="startTimer"
            >
              <template #icon>
                <n-icon :component="Play" :size="32" />
              </template>
            </n-button>
            <n-button
              v-else-if="!isPaused"
              type="warning"
              size="large"
              circle
              @click="pauseTimer"
            >
              <template #icon>
                <n-icon :component="Pause" :size="32" />
              </template>
            </n-button>
            <n-button
              v-else
              type="primary"
              size="large"
              circle
              @click="startTimer"
            >
              <template #icon>
                <n-icon :component="Play" :size="32" />
              </template>
            </n-button>
            <n-button size="large" circle @click="resetTimer">
              <template #icon>
                <n-icon :component="Refresh" :size="24" />
              </template>
            </n-button>
          </div>
          <div class="fullscreen-sessions">
            {{ t('pomodoro.sessions') }}: {{ sessionsCompleted }}
          </div>
          <div class="fullscreen-mode-switch">
            <n-button
              :type="mode === 'work' ? 'error' : 'default'"
              @click="switchMode('work')"
            >
              {{ t('pomodoro.work') }}
            </n-button>
            <n-button
              :type="mode === 'shortBreak' ? 'success' : 'default'"
              @click="switchMode('shortBreak')"
            >
              {{ t('pomodoro.shortBreak') }}
            </n-button>
            <n-button
              :type="mode === 'longBreak' ? 'info' : 'default'"
              @click="switchMode('longBreak')"
            >
              {{ t('pomodoro.longBreak') }}
            </n-button>
          </div>
        </div>
      </div>
    </n-modal>

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
          <n-select
            v-model:value="selectedTaskId"
            :placeholder="t('pomodoro.selectTask')"
            clearable
            size="small"
            :options="taskStore.tasks.map(t => ({ label: t.title, value: t.id }))"
            @click.stop
          />
          <div class="selected-task" v-if="selectedTask">
            <n-tag size="small" type="info">{{ selectedTask.title }}</n-tag>
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
          <n-button
            v-if="!isRunning"
            type="primary"
            circle
            size="small"
            @click.stop="startTimer"
          >
            <template #icon>
              <n-icon :component="Play" />
            </template>
          </n-button>
          <n-button
            v-else-if="!isPaused"
            type="warning"
            circle
            size="small"
            @click.stop="pauseTimer"
          >
            <template #icon>
              <n-icon :component="Pause" />
            </template>
          </n-button>
          <n-button
            v-else
            type="primary"
            circle
            size="small"
            @click.stop="startTimer"
          >
            <template #icon>
              <n-icon :component="Play" />
            </template>
          </n-button>
          <n-button circle size="small" @click.stop="resetTimer">
            <template #icon>
              <n-icon :component="Refresh" />
            </template>
          </n-button>
          <n-button circle size="small" @click.stop="toggleFullscreen">
            <template #icon>
              <n-icon :component="Expand" />
            </template>
          </n-button>
        </div>

        <div class="mode-switcher">
          <n-button
            :type="mode === 'work' ? 'error' : 'default'"
            size="small"
            @click.stop="switchMode('work')"
          >
            {{ t('pomodoro.work') }}
          </n-button>
          <n-button
            :type="mode === 'shortBreak' ? 'success' : 'default'"
            size="small"
            @click.stop="switchMode('shortBreak')"
          >
            {{ t('pomodoro.shortBreak') }}
          </n-button>
          <n-button
            :type="mode === 'longBreak' ? 'info' : 'default'"
            size="small"
            @click.stop="switchMode('longBreak')"
          >
            {{ t('pomodoro.longBreak') }}
          </n-button>
        </div>

        <div class="session-count">
          {{ t('pomodoro.sessions') }}: {{ sessionsCompleted }}
        </div>

        <n-divider style="margin: 12px 0" />

        <div class="settings-section">
          <n-form label-placement="left" label-width="120px" size="small">
            <n-form-item :label="t('pomodoro.workDuration')">
              <n-input-number v-model:value="workDuration" :min="1" :max="60" />
            </n-form-item>
            <n-form-item :label="t('pomodoro.shortBreakDuration')">
              <n-input-number v-model:value="shortBreakDuration" :min="1" :max="30" />
            </n-form-item>
            <n-form-item :label="t('pomodoro.longBreakDuration')">
              <n-input-number v-model:value="longBreakDuration" :min="1" :max="60" />
            </n-form-item>
            <n-form-item :label="t('pomodoro.longBreakInterval')">
              <n-input-number v-model:value="longBreakInterval" :min="2" :max="10" />
            </n-form-item>
            <n-form-item :label="t('pomodoro.autoStartBreaks')">
              <n-switch v-model:value="autoStartBreaks" />
            </n-form-item>
            <n-form-item :label="t('pomodoro.autoStartWork')">
              <n-switch v-model:value="autoStartWork" />
            </n-form-item>
            <n-form-item :label="t('pomodoro.sound')">
              <n-switch v-model:value="soundEnabled" />
            </n-form-item>
            <n-form-item v-if="soundEnabled" :label="t('pomodoro.soundType')">
              <n-select v-model:value="selectedSound" :options="soundOptions" />
            </n-form-item>
            <n-form-item :label="t('pomodoro.backgroundSound')">
              <n-switch v-model:value="useBackgroundSound" />
            </n-form-item>
            <n-form-item v-if="useBackgroundSound" :label="t('pomodoro.soundType')">
              <n-select v-model:value="backgroundSoundType" :options="backgroundSoundOptions" />
            </n-form-item>
            <n-form-item v-if="useBackgroundSound" :label="t('pomodoro.volume')">
              <n-slider v-model:value="backgroundVolume" :min="0" :max="1" :step="0.1" />
            </n-form-item>
          </n-form>
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
    rgba(0, 0, 0, 0.1) calc(var(--progress) * 1%)
  );
  display: flex;
  align-items: center;
  justify-content: center;
}

.timer-time {
  font-size: 9px;
  font-weight: 600;
  color: #333;
}

.pomodoro-panel {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  width: 320px;
  padding: 12px;
  background: var(--n-color);
  border: 1px solid var(--n-border-color);
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
  background: var(--n-color-hover);
  border-radius: 8px;

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .stat-label {
      font-size: 10px;
      color: var(--n-text-color-3);
    }

    .stat-value {
      font-size: 16px;
      font-weight: 600;
      color: var(--n-primary-color);
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
    background: var(--n-color);
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
    color: var(--n-text-color);
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
  color: var(--n-text-color-3);
}

.settings-section {
  width: 100%;

  :deep(.n-form-item) {
    margin-bottom: 8px;
  }
}

// Fullscreen Timer Styles
.fullscreen-modal {
  :deep(.n-modal) {
    max-width: 100vw;
    max-height: 100vh;
  }
}

.fullscreen-timer {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  position: relative;
}

.fullscreen-header {
  position: absolute;
  top: 20px;
  right: 20px;
}

.fullscreen-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.fullscreen-task {
  font-size: 24px;
  color: rgba(255, 255, 255, 0.7);
}

.fullscreen-mode {
  font-size: 32px;
  text-transform: uppercase;
  letter-spacing: 4px;
  color: var(--mode-color);
}

.fullscreen-time {
  font-size: 180px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
  line-height: 1;
}

.fullscreen-progress {
  width: 400px;
}

.fullscreen-controls {
  display: flex;
  gap: 24px;
}

.fullscreen-sessions {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.6);
}

.fullscreen-mode-switch {
  display: flex;
  gap: 12px;
}
</style>
