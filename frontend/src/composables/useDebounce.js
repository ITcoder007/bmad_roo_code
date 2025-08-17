import { ref } from 'vue'

/**
 * 防抖组合函数
 * @param {Function} fn 需要防抖的函数
 * @param {number} delay 防抖延迟（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function useDebounce(fn, delay = 300) {
  const timer = ref(null)
  
  return function(...args) {
    if (timer.value) {
      clearTimeout(timer.value)
    }
    
    timer.value = setTimeout(() => {
      fn.apply(this, args)
      timer.value = null
    }, delay)
  }
}

/**
 * 防抖值组合函数
 * @param {any} value 需要防抖的响应式值
 * @param {number} delay 防抖延迟（毫秒）
 * @returns {Object} 包含防抖值和方法的对象
 */
export function useDebouncedRef(value, delay = 300) {
  const debouncedValue = ref(value)
  const timer = ref(null)
  
  const updateDebouncedValue = (newValue) => {
    if (timer.value) {
      clearTimeout(timer.value)
    }
    
    timer.value = setTimeout(() => {
      debouncedValue.value = newValue
      timer.value = null
    }, delay)
  }
  
  return {
    debouncedValue,
    updateDebouncedValue
  }
}