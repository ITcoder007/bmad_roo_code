/**
 * 文本高亮工具函数
 * 提供关键词高亮显示功能
 */

/**
 * 高亮匹配的文本
 * @param {string} text - 原始文本
 * @param {string} keyword - 搜索关键词
 * @param {string} className - 高亮样式类名
 * @returns {string} 包含高亮标签的HTML字符串
 */
export function highlightText(text, keyword, className = 'highlight') {
  if (!text || !keyword) return text
  
  try {
    // 转义特殊字符
    const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    // 创建全局不区分大小写的正则表达式
    const regex = new RegExp(`(${escapedKeyword})`, 'gi')
    
    return text.replace(regex, `<span class="${className}">$1</span>`)
  } catch (error) {
    console.warn('Failed to highlight text:', error)
    return text
  }
}

/**
 * 高亮多个关键词
 * @param {string} text - 原始文本
 * @param {string[]} keywords - 搜索关键词数组
 * @param {string} className - 高亮样式类名
 * @returns {string} 包含高亮标签的HTML字符串
 */
export function highlightMultipleKeywords(text, keywords, className = 'highlight') {
  if (!text || !keywords || keywords.length === 0) return text
  
  let result = text
  keywords.forEach(keyword => {
    if (keyword.trim()) {
      result = highlightText(result, keyword.trim(), className)
    }
  })
  
  return result
}

/**
 * 检查文本是否包含关键词
 * @param {string} text - 原始文本
 * @param {string} keyword - 搜索关键词
 * @returns {boolean} 是否包含关键词
 */
export function containsKeyword(text, keyword) {
  if (!text || !keyword) return false
  
  return text.toLowerCase().includes(keyword.toLowerCase())
}

/**
 * 从文本中提取包含关键词的片段
 * @param {string} text - 原始文本
 * @param {string} keyword - 搜索关键词
 * @param {number} contextLength - 上下文长度
 * @returns {string} 包含关键词的文本片段
 */
export function extractSnippet(text, keyword, contextLength = 50) {
  if (!text || !keyword) return text
  
  const lowerText = text.toLowerCase()
  const lowerKeyword = keyword.toLowerCase()
  const index = lowerText.indexOf(lowerKeyword)
  
  if (index === -1) return text
  
  const start = Math.max(0, index - contextLength)
  const end = Math.min(text.length, index + keyword.length + contextLength)
  
  let snippet = text.substring(start, end)
  
  // 添加省略号
  if (start > 0) snippet = '...' + snippet
  if (end < text.length) snippet = snippet + '...'
  
  return snippet
}

/**
 * 高亮文本并限制长度
 * @param {string} text - 原始文本
 * @param {string} keyword - 搜索关键词
 * @param {number} maxLength - 最大显示长度
 * @param {string} className - 高亮样式类名
 * @returns {string} 高亮且截断的文本
 */
export function highlightAndTruncate(text, keyword, maxLength = 100, className = 'highlight') {
  if (!text) return ''
  
  if (!keyword) {
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
  }
  
  // 如果文本包含关键词，尝试提取包含关键词的片段
  if (containsKeyword(text, keyword)) {
    const snippet = extractSnippet(text, keyword, Math.floor(maxLength / 2))
    return highlightText(snippet, keyword, className)
  }
  
  // 如果不包含关键词，直接截断
  const truncated = text.length > maxLength ? text.substring(0, maxLength) + '...' : text
  return highlightText(truncated, keyword, className)
}

/**
 * 移除HTML标签，获取纯文本
 * @param {string} html - 包含HTML的字符串
 * @returns {string} 纯文本
 */
export function stripHtmlTags(html) {
  if (!html) return ''
  
  return html.replace(/<[^>]*>/g, '')
}

/**
 * 智能高亮，支持分词搜索
 * @param {string} text - 原始文本
 * @param {string} query - 搜索查询（可能包含多个词）
 * @param {string} className - 高亮样式类名
 * @returns {string} 高亮后的文本
 */
export function smartHighlight(text, query, className = 'highlight') {
  if (!text || !query) return text
  
  // 将查询分解为单词
  const keywords = query.trim().split(/\s+/).filter(word => word.length > 0)
  
  if (keywords.length === 0) return text
  
  // 对每个关键词进行高亮
  return highlightMultipleKeywords(text, keywords, className)
}