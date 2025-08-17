<template>
  <div class="certificate-status-badge">
    <el-tag
      :type="statusConfig.type"
      :effect="statusConfig.effect"
      size="large"
    >
      <el-icon class="status-icon">
        <component :is="statusConfig.icon" />
      </el-icon>
      <span class="status-text">{{ statusConfig.text }}</span>
    </el-tag>
    <div class="status-details">
      <p class="days-info">
        {{ daysInfo }}
      </p>
      <p class="expiry-date">
        到期时间：{{ formatDate(expiryDate) }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { 
  Check, 
  WarningFilled, 
  CircleCloseFilled 
} from '@element-plus/icons-vue'
import { formatDate, getDaysFromToday } from '@/utils/date'

const props = defineProps({
  status: {
    type: String,
    required: true,
    validator: (value) => ['NORMAL', 'EXPIRING_SOON', 'EXPIRED'].includes(value)
  },
  expiryDate: {
    type: String,
    required: true
  }
})

// 证书状态常量
const CERTIFICATE_STATUS = {
  NORMAL: 'NORMAL',
  EXPIRING_SOON: 'EXPIRING_SOON',
  EXPIRED: 'EXPIRED'
}

// 计算距离到期天数
const daysToExpiry = computed(() => getDaysFromToday(props.expiryDate))

// 状态配置映射
const STATUS_CONFIG_MAP = {
  [CERTIFICATE_STATUS.NORMAL]: {
    type: 'success',
    effect: 'dark',
    icon: Check,
    text: '正常',
    color: '#67C23A'
  },
  [CERTIFICATE_STATUS.EXPIRING_SOON]: {
    type: 'warning',
    effect: 'dark',
    icon: WarningFilled,
    text: '即将过期',
    color: '#E6A23C'
  },
  [CERTIFICATE_STATUS.EXPIRED]: {
    type: 'danger',
    effect: 'dark',
    icon: CircleCloseFilled,
    text: '已过期',
    color: '#F56C6C'
  }
}

// 状态配置
const statusConfig = computed(() => {
  return STATUS_CONFIG_MAP[props.status] || {
    type: 'info',
    effect: 'plain',
    icon: Check,
    text: '未知状态',
    color: '#909399'
  }
})

// 天数信息
const daysInfo = computed(() => {
  const days = daysToExpiry.value
  
  if (days < 0) {
    return `已过期 ${Math.abs(days)} 天`
  } else if (days === 0) {
    return '今天到期'
  } else if (days === 1) {
    return '明天到期'
  } else if (days <= 30) {
    return `${days} 天后到期`
  } else {
    return `${days} 天后到期`
  }
})
</script>

<style scoped>
.certificate-status-badge {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.el-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  width: fit-content;
}

.status-icon {
  font-size: 16px;
}

.status-text {
  font-weight: 500;
}

.status-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.days-info {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: var(--el-text-color-primary);
}

.expiry-date {
  font-size: 14px;
  margin: 0;
  color: var(--el-text-color-regular);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .certificate-status-badge {
    text-align: center;
  }
  
  .el-tag {
    justify-content: center;
    width: 100%;
  }
}
</style>