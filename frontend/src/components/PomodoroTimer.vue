<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElPopover } from 'element-plus'

const { t } = useI18n()

// Timer state
const isRunning = ref(false)
const isPaused = ref(false)
const mode = ref<'work' | 'shortBreak' | 'longBreak'>('work')
const timeLeft = ref(25 * 60) // seconds
const totalTime = ref(25 * 60)
const sessionsCompleted = ref(0)

let timerInterval: ReturnType<typeof setInterval> | null = null

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

function timerComplete() {
  resetTimer()

  if (soundEnabled.value) {
    playNotificationSound()
  }

  if (mode.value === 'work') {
    sessionsCompleted.value++
    ElMessage.success(t('pomodoro.workComplete'))

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

// Watch for mode changes
watch(mode, () => {
  setDuration()
})

onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }
})
</script>

<template>
  <div class="pomodoro-compact">
    <el-popover
      placement="bottom-end"
      :width="280"
      trigger="click"
    >
      <template #reference>
        <div class="timer-display">
          <div class="timer-ring" :style="{ '--progress': progress, '--color': modeColor }">
            <span class="timer-time">{{ formattedTime }}</span>
          </div>
        </div>
      </template>

      <div class="pomodoro-panel">
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
            @click="startTimer"
          >
            <el-icon><VideoPlay /></el-icon>
          </el-button>
          <el-button
            v-else-if="!isPaused"
            type="warning"
            circle
            size="small"
            @click="pauseTimer"
          >
            <el-icon><VideoPause /></el-icon>
          </el-button>
          <el-button
            v-else
            type="primary"
            circle
            size="small"
            @click="startTimer"
          >
            <el-icon><VideoPlay /></el-icon>
          </el-button>
          <el-button circle size="small" @click="resetTimer">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>

        <div class="mode-switcher">
          <el-button
            :type="mode === 'work' ? 'danger' : 'default'"
            size="small"
            @click="switchMode('work')"
          >
            {{ t('pomodoro.work') }}
          </el-button>
          <el-button
            :type="mode === 'shortBreak' ? 'success' : 'default'"
            size="small"
            @click="switchMode('shortBreak')"
          >
            {{ t('pomodoro.shortBreak') }}
          </el-button>
          <el-button
            :type="mode === 'longBreak' ? 'primary' : 'default'"
            size="small"
            @click="switchMode('longBreak')"
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
              <el-input-number v-model="workDuration" :min="1" :max="60" controls-position="right" />
            </el-form-item>
            <el-form-item :label="t('pomodoro.shortBreakDuration')">
              <el-input-number v-model="shortBreakDuration" :min="1" :max="30" controls-position="right" />
            </el-form-item>
            <el-form-item :label="t('pomodoro.longBreakDuration')">
              <el-input-number v-model="longBreakDuration" :min="1" :max="60" controls-position="right" />
            </el-form-item>
            <el-form-item :label="t('pomodoro.autoStartBreaks')">
              <el-switch v-model="autoStartBreaks" />
            </el-form-item>
            <el-form-item :label="t('pomodoro.autoStartWork')">
              <el-switch v-model="autoStartWork" />
            </el-form-item>
            <el-form-item :label="t('pomodoro.sound')">
              <el-switch v-model="soundEnabled" />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<style scoped lang="scss">
.pomodoro-compact {
  display: inline-flex;
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
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
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
  :deep(.el-form-item) {
    margin-bottom: 8px;
  }

  :deep(.el-input-number) {
    width: 100px;
  }
}
</style>
