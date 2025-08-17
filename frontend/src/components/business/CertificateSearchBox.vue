<template>
  <div class="certificate-search-box">
    <el-input
      v-model="searchQuery"
      :placeholder="placeholder"
      clearable
      class="search-input"
      data-testid="search-input"
      @clear="handleClear"
      @keydown.enter="handleSearch"
      @keydown.escape="handleClear"
      @focus="showHistory = true"
      @blur="handleBlur"
    >
      <template #suffix>
        <el-button 
          type="primary" 
          :icon="Search" 
          :loading="loading"
          class="search-button"
          data-testid="search-button"
          @click="handleSearch"
        >
          搜索
        </el-button>
      </template>
    </el-input>
    
    <!-- 搜索历史 -->
    <div 
      v-show="showHistory && searchHistory.length > 0" 
      class="search-history"
      @mousedown.prevent
    >
      <div class="history-header">
        <span>搜索历史</span>
        <el-button 
          type="text" 
          data-testid="clear-history-button"
          @click="clearHistory"
        >
          清除历史
        </el-button>
      </div>
      <div class="history-items">
        <el-tag
          v-for="(item, index) in searchHistory"
          :key="index"
          closable
          class="history-tag"
          :data-testid="`history-item-${index}`"
          @click="selectHistoryItem(item)"
          @close="removeHistoryItem(index)"
        >
          {{ item }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useDebounce } from '@/composables/useDebounce'

// Props
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '搜索证书名称或域名...'
  },
  debounceDelay: {
    type: Number,
    default: 300
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'search', 'clear'])

// 响应式数据
const searchQuery = ref(props.modelValue)
const showHistory = ref(false)
const searchHistory = ref([])

// 防抖搜索
const debouncedSearch = useDebounce((query) => {
  if (query.trim()) {
    emit('search', query.trim())
    addToHistory(query.trim())
  }
}, props.debounceDelay)

// 计算属性
const trimmedQuery = computed(() => searchQuery.value?.trim() || '')

// 方法
const handleSearch = () => {
  if (trimmedQuery.value) {
    emit('search', trimmedQuery.value)
    addToHistory(trimmedQuery.value)
    showHistory.value = false
  }
}

const handleClear = () => {
  searchQuery.value = ''
  emit('update:modelValue', '')
  emit('clear')
  showHistory.value = false
}

const handleBlur = () => {
  // 延迟隐藏历史记录，允许点击历史项
  nextTick(() => {
    setTimeout(() => {
      showHistory.value = false
    }, 200)
  })
}

const addToHistory = (query) => {
  if (!searchHistory.value.includes(query)) {
    searchHistory.value.unshift(query)
    if (searchHistory.value.length > 10) {
      searchHistory.value = searchHistory.value.slice(0, 10)
    }
    saveHistoryToStorage()
  }
}

const selectHistoryItem = (item) => {
  searchQuery.value = item
  emit('update:modelValue', item)
  emit('search', item)
  showHistory.value = false
}

const removeHistoryItem = (index) => {
  searchHistory.value.splice(index, 1)
  saveHistoryToStorage()
}

const clearHistory = () => {
  searchHistory.value = []
  saveHistoryToStorage()
  showHistory.value = false
}

const loadHistoryFromStorage = () => {
  try {
    const history = localStorage.getItem('certificate-search-history')
    if (history) {
      searchHistory.value = JSON.parse(history)
    }
  } catch (error) {
    console.warn('Failed to load search history:', error)
  }
}

const saveHistoryToStorage = () => {
  try {
    localStorage.setItem('certificate-search-history', JSON.stringify(searchHistory.value))
  } catch (error) {
    console.warn('Failed to save search history:', error)
  }
}

// 监听器
watch(
  () => props.modelValue,
  (newValue) => {
    searchQuery.value = newValue
  }
)

watch(searchQuery, (newValue) => {
  emit('update:modelValue', newValue)
  if (newValue.trim()) {
    debouncedSearch(newValue)
  }
})

// 生命周期
onMounted(() => {
  loadHistoryFromStorage()
})
</script>

<style scoped>
.certificate-search-box {
  position: relative;
  width: 100%;
  max-width: 400px;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 4px;
  transition: all 0.2s;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #409eff;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.search-button {
  margin-left: 8px;
  border-radius: 0 4px 4px 0;
}

.search-history {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  margin-top: 4px;
  padding: 12px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
}

.history-items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.history-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.history-tag:hover {
  background-color: #ecf5ff;
  border-color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .certificate-search-box {
    max-width: 300px;
  }
}
</style>