<template>
  <el-card class="stats-card">
    <div class="stats-header">
      <h3>证书概览</h3>
      <el-icon class="stats-icon">
        <Document />
      </el-icon>
    </div>
    
    <div
      v-if="loading"
      class="loading-container"
    >
      <el-skeleton
        :rows="3"
        animated
      />
    </div>
    
    <div
      v-else
      class="stats-content"
    >
      <!-- 总数显示 -->
      <div class="total-count">
        <span class="count-number">{{ stats.total || 0 }}</span>
        <span class="count-label">证书总数</span>
      </div>
      
      <!-- 状态分布 -->
      <div class="status-distribution">
        <div class="status-item status-normal">
          <span class="status-count">{{ stats.normal || 0 }}</span>
          <span class="status-label">正常</span>
        </div>
        <div class="status-item status-expiring">
          <span class="status-count">{{ stats.expiring || 0 }}</span>
          <span class="status-label">即将过期</span>
        </div>
        <div class="status-item status-expired">
          <span class="status-count">{{ stats.expired || 0 }}</span>
          <span class="status-label">已过期</span>
        </div>
      </div>
      
      <!-- ECharts 饼图 -->
      <div
        ref="chartRef"
        class="chart-container"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Document } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// Props
const props = defineProps({
  stats: {
    type: Object,
    default: () => ({
      total: 0,
      normal: 0,
      expiring: 0,
      expired: 0
    })
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// 响应式数据
const chartRef = ref(null)
let chartInstance = null

// 计算属性
const chartData = computed(() => [
  { name: '正常', value: props.stats.normal || 0, color: '#67C23A' },
  { name: '即将过期', value: props.stats.expiring || 0, color: '#E6A23C' },
  { name: '已过期', value: props.stats.expired || 0, color: '#F56C6C' }
])

// 方法
const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [
      {
        name: '证书状态分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        data: chartData.value,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        itemStyle: {
          color: params => params.data.color
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

const destroyChart = () => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
}

// 监听器
watch(() => props.stats, () => {
  nextTick(() => updateChart())
}, { deep: true })

// 生命周期
onMounted(() => {
  nextTick(() => initChart())
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  destroyChart()
  window.removeEventListener('resize', resizeChart)
})
</script>

<style scoped>
.stats-card {
  height: 100%;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.stats-icon {
  font-size: 24px;
  color: #409eff;
}

.loading-container {
  padding: 20px 0;
}

.total-count {
  text-align: center;
  margin-bottom: 20px;
}

.count-number {
  display: block;
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.count-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.status-distribution {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.status-item {
  text-align: center;
}

.status-count {
  display: block;
  font-size: 18px;
  font-weight: bold;
}

.status-label {
  display: block;
  font-size: 12px;
  margin-top: 2px;
  color: #909399;
}

.status-normal .status-count {
  color: #67C23A;
}

.status-expiring .status-count {
  color: #E6A23C;
}

.status-expired .status-count {
  color: #F56C6C;
}

.chart-container {
  height: 200px;
  width: 100%;
}
</style>