<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { statsApi } from '@/api'
import { GridOutline, CheckmarkCircleOutline, TimeOutline, WarningOutline } from '@vicons/ionicons5'

const { t } = useI18n()

use([CanvasRenderer, PieChart, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const loading = ref(false)
const stats = ref<any>(null)

const pieOption = ref({})
const lineOption = ref({})
const barOption = ref({})

onMounted(async () => {
  await fetchDashboardData()
})

async function fetchDashboardData() {
  loading.value = true
  try {
    const response = await statsApi.getDashboard()
    stats.value = response.data.stats

    // Pie chart - Task status
    pieOption.value = {
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%' },
      series: [
        {
          name: 'Tasks',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          data: [
            { value: stats.value.completedTasks, name: t('task.statuses.completed'), itemStyle: { color: '#18a058' } },
            { value: stats.value.pendingTasks, name: t('task.statuses.pending'), itemStyle: { color: '#909399' } },
            { value: stats.value.inProgressTasks, name: t('task.statuses.inProgress'), itemStyle: { color: '#18a058' } },
            { value: stats.value.overdueTasks, name: t('dashboard.overdueTasks'), itemStyle: { color: '#d03050' } }
          ]
        }
      ]
    }

    // Line chart - Task trend
    const trendData = response.data.trend.data || []
    lineOption.value = {
      tooltip: { trigger: 'axis' },
      legend: { bottom: '0%' },
      xAxis: {
        type: 'category',
        data: trendData.map((d: any) => d.date)
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: t('task.statuses.completed'),
          type: 'line',
          data: trendData.map((d: any) => d.created),
          smooth: true,
          itemStyle: { color: '#18a058' }
        },
        {
          name: t('task.statuses.completed'),
          type: 'line',
          data: trendData.map((d: any) => d.completed),
          smooth: true,
          itemStyle: { color: '#18a058' }
        }
      ]
    }

    // Bar chart - Priority distribution
    const priorityData = response.data.priorityDistribution.data || []
    barOption.value = {
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: priorityData.map((d: any) => d.priority) },
      yAxis: { type: 'value' },
      series: [
        {
          name: 'Tasks',
          type: 'bar',
          data: priorityData.map((d: any) => d.count),
          itemStyle: {
            color: (params: any) => {
              const colors = ['#909399', '#18a058', '#f0a020', '#d03050']
              return colors[params.dataIndex] || '#18a058'
            }
          }
        }
      ]
    }
  } finally {
    loading.value = false
  }
}

const formatNumber = (num: number) => {
  return num?.toLocaleString() || '0'
}
</script>

<template>
  <div class="dashboard">
    <h1 class="page-title">{{ t('dashboard.title') }}</h1>

    <n-grid :cols="4" :x-gap="20" :y-gap="20" class="stats-row">
      <n-gi>
        <n-card hoverable class="stat-card">
          <div class="stat-icon" style="background: #18a058">
            <n-icon :size="32"><GridOutline /></n-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(stats?.totalTasks) }}</div>
            <div class="stat-label">{{ t('dashboard.totalTasks') }}</div>
          </div>
        </n-card>
      </n-gi>

      <n-gi>
        <n-card hoverable class="stat-card">
          <div class="stat-icon" style="background: #18a058">
            <n-icon :size="32"><CheckmarkCircleOutline /></n-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(stats?.completedTasks) }}</div>
            <div class="stat-label">{{ t('dashboard.completedTasks') }}</div>
          </div>
        </n-card>
      </n-gi>

      <n-gi>
        <n-card hoverable class="stat-card">
          <div class="stat-icon" style="background: #f0a020">
            <n-icon :size="32"><TimeOutline /></n-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(stats?.todayTasks) }}</div>
            <div class="stat-label">{{ t('dashboard.todayTasks') }}</div>
          </div>
        </n-card>
      </n-gi>

      <n-gi>
        <n-card hoverable class="stat-card">
          <div class="stat-icon" style="background: #d03050">
            <n-icon :size="32"><WarningOutline /></n-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(stats?.overdueTasks) }}</div>
            <div class="stat-label">{{ t('dashboard.overdueTasks') }}</div>
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <n-grid :cols="24" :x-gap="20" :y-gap="20" class="chart-row">
      <n-gi :span="8">
        <n-card hoverable :header-style="{ textAlign: 'center' }">
          <template #header>
            <span>{{ t('dashboard.taskStatus') }}</span>
          </template>
          <v-chart :option="pieOption" :autoresize="true" style="height: 300px" />
        </n-card>
      </n-gi>

      <n-gi :span="16">
        <n-card hoverable :header-style="{ textAlign: 'center' }">
          <template #header>
            <span>{{ t('dashboard.taskTrend') }}</span>
          </template>
          <v-chart :option="lineOption" :autoresize="true" style="height: 300px" />
        </n-card>
      </n-gi>
    </n-grid>

    <n-grid :cols="24" :x-gap="20" :y-gap="20">
      <n-gi :span="12">
        <n-card hoverable :header-style="{ textAlign: 'center' }">
          <template #header>
            <span>{{ t('dashboard.priorityDistribution') }}</span>
          </template>
          <v-chart :option="barOption" :autoresize="true" style="height: 300px" />
        </n-card>
      </n-gi>

      <n-gi :span="12">
        <n-card hoverable :header-style="{ textAlign: 'center' }">
          <template #header>
            <span>{{ t('dashboard.quickStats') }}</span>
          </template>
          <div class="quick-stats">
            <div class="quick-stat-item">
              <span class="label">{{ t('dashboard.completionRate') }}</span>
              <n-progress type="line" :percentage="Math.round(stats?.completionRate || 0)" :stroke-width="12" />
            </div>
            <div class="quick-stat-item">
              <span class="label">{{ t('dashboard.totalEvents') }}</span>
              <span class="value">{{ formatNumber(stats?.totalEvents) }}</span>
            </div>
            <div class="quick-stat-item">
              <span class="label">{{ t('dashboard.todayEvents') }}</span>
              <span class="value">{{ formatNumber(stats?.todayEvents) }}</span>
            </div>
            <div class="quick-stat-item">
              <span class="label">{{ t('dashboard.pendingTasks') }}</span>
              <span class="value">{{ formatNumber(stats?.pendingTasks) }}</span>
            </div>
          </div>
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 20px 16px;

  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin-bottom: 12px;
  }

  .stat-content {
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: var(--n-text-color);
    }

    .stat-label {
      font-size: 14px;
      color: var(--n-text-color-3);
      margin-top: 4px;
    }
  }
}

.chart-row {
  margin-bottom: 20px;
}

.chart-title {
  display: block;
  text-align: center;
  width: 100%;
}

.quick-stats {
  padding: 10px 0;

  .quick-stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 12px 0;
    border-bottom: 1px solid var(--n-border-color);

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: var(--n-text-color-3);
      margin-bottom: 8px;
    }

    .value {
      font-size: 18px;
      font-weight: 600;
      color: var(--n-text-color);
    }

    :deep(.n-progress) {
      width: 80%;
    }
  }
}
</style>
