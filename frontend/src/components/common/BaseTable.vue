<template>
  <div class="base-table">
    <el-table
      ref="tableRef"
      :data="data"
      :loading="loading"
      v-bind="$attrs"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      v-on="$listeners"
    >
      <slot />
    </el-table>
    
    <!-- 分页组件 -->
    <div v-if="showPagination" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="pageSizes"
        :layout="paginationLayout"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

// 定义 props
const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  total: {
    type: Number,
    default: 0
  },
  page: {
    type: Number,
    default: 1
  },
  size: {
    type: Number,
    default: 20
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  }
})

// 定义事件
const emit = defineEmits([
  'update:page',
  'update:size',
  'selection-change',
  'sort-change',
  'page-change',
  'size-change'
])

// 响应式数据
const tableRef = ref(null)
const currentPage = ref(props.page)
const pageSize = ref(props.size)

// 监听 props 变化
watch(() => props.page, (newVal) => {
  currentPage.value = newVal
})

watch(() => props.size, (newVal) => {
  pageSize.value = newVal
})

// 事件处理
const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

const handleSortChange = (sortInfo) => {
  emit('sort-change', sortInfo)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  emit('update:size', size)
  emit('size-change', size)
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  emit('update:page', page)
  emit('page-change', page)
}

// 暴露方法
const clearSelection = () => {
  tableRef.value?.clearSelection()
}

const toggleRowSelection = (row, selected) => {
  tableRef.value?.toggleRowSelection(row, selected)
}

const toggleAllSelection = () => {
  tableRef.value?.toggleAllSelection()
}

const setCurrentRow = (row) => {
  tableRef.value?.setCurrentRow(row)
}

const clearSort = () => {
  tableRef.value?.clearSort()
}

const doLayout = () => {
  tableRef.value?.doLayout()
}

defineExpose({
  tableRef,
  clearSelection,
  toggleRowSelection,
  toggleAllSelection,
  setCurrentRow,
  clearSort,
  doLayout
})
</script>

<style scoped>
.base-table {
  width: 100%;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pagination-wrapper {
    margin-top: 16px;
    padding: 12px 0;
  }
  
  .pagination-wrapper :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>