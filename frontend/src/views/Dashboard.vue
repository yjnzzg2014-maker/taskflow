<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { statsApi } from '@/api'

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
            { value: stats.value.completedTasks, name: t('task.statuses.completed'), itemStyle: { color: '#67c23a' } },
            { value: stats.value.pendingTasks, name: t('task.statuses.pending'), itemStyle: { color: '#909399' } },
            { value: stats.value.inProgressTasks, name: t('task.statuses.inProgress'), itemStyle: { color: '#409eff' } },
            { value: stats.value.overdueTasks, name: t('dashboard.overdueTasks'), itemStyle: { color: '#f56c6c' } }
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
          itemStyle: { color: '#409eff' }
        },
        {
          name: t('task.statuses.completed'),
          type: 'line',
          data: trendData.map((d: any) => d.completed),
          smooth: true,
          itemStyle: { color: '#67c23a' }
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
              const colors = ['#909399', '#409eff', '#e6a23c', '#f56c6c']
              return colors[params.dataIndex] || '#409eff'
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

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409eff">
            <el-icon :size="32"><List /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(stats?.totalTasks) }}</div>
            <div class="stat-label">{{ t('dashboard.totalTasks') }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(stats?.completedTasks) }}</div>
            <div class="stat-label">{{ t('dashboard.completedTasks') }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(stats?.todayTasks) }}</div>
            <div class="stat-label">{{ t('dashboard.todayTasks') }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f56c6c">
            <el-icon :size="32"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(stats?.overdueTasks) }}</div>
            <div class="stat-label">{{ t('dashboard.overdueTasks') }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('dashboard.taskStatus') }}</span>
          </template>
          <v-chart :option="pieOption" :autoresize="true" style="height: 300px" />
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('dashboard.taskTrend') }}</span>
          </template>
          <v-chart :option="lineOption" :autoresize="true" style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('dashboard.priorityDistribution') }}</span>
          </template>
          <v-chart :option="barOption" :autoresize="true" style="height: 300px" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('dashboard.quickStats') }}</span>
          </template>
          <div class="quick-stats">
            <div class="quick-stat-item">
              <span class="label">{{ t('dashboard.completionRate') }}</span>
              <el-progress :percentage="Math.round(stats?.completionRate || 0)" :stroke-width="12" />
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
        </el-card>
      </el-col>
    </el-row>
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
  align-items: center;
  gap: 16px;

  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .stat-content {
    flex: 1;

    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #303133;

      .dark & {
        color: #e5e5e5;
      }
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }
}

.chart-row {
  margin-bottom: 20px;
}

.quick-stats {
  padding: 10px 0;

  .quick-stat-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid var(--border-color);

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: #606266;
    }

    .value {
      font-size: 18px;
      font-weight: 600;
      color: #303133;

      .dark & {
        color: #e5e5e5;
      }
    }
  }
}
</style>
