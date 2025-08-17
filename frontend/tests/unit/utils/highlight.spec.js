import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  highlightText,
  highlightMultipleKeywords,
  containsKeyword,
  extractSnippet,
  highlightAndTruncate,
  stripHtmlTags,
  smartHighlight
} from '@/utils/highlight'

describe('highlight.js', () => {
  beforeEach(() => {
    // 清理控制台警告的模拟
    vi.clearAllMocks()
  })

  describe('highlightText', () => {
    it('应该高亮匹配的关键词', () => {
      const text = 'This is a test string'
      const keyword = 'test'
      const result = highlightText(text, keyword)
      
      expect(result).toBe('This is a <span class="highlight">test</span> string')
    })

    it('应该使用自定义类名', () => {
      const text = 'Test string'
      const keyword = 'test'
      const className = 'custom-highlight'
      const result = highlightText(text, keyword, className)
      
      expect(result).toBe('<span class="custom-highlight">Test</span> string')
    })

    it('应该进行不区分大小写的匹配', () => {
      const text = 'Test STRING with Test'
      const keyword = 'test'
      const result = highlightText(text, keyword)
      
      expect(result).toBe('<span class="highlight">Test</span> STRING with <span class="highlight">Test</span>')
    })

    it('应该处理特殊正则表达式字符', () => {
      const text = 'Search for [special] characters'
      const keyword = '[special]'
      const result = highlightText(text, keyword)
      
      expect(result).toBe('Search for <span class="highlight">[special]</span> characters')
    })

    it('应该处理空输入', () => {
      expect(highlightText('', 'test')).toBe('')
      expect(highlightText('test', '')).toBe('test')
      expect(highlightText('', '')).toBe('')
      expect(highlightText(null, 'test')).toBe(null)
      expect(highlightText('test', null)).toBe('test')
    })

    it('应该在正则表达式错误时返回原文本', () => {
      // 模拟控制台警告
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      
      // 使用会导致正则表达式错误的特殊情况 - 直接修改代码模拟错误
      const originalRegExp = global.RegExp
      global.RegExp = function(pattern, flags) {
        if (pattern.includes('test-error')) {
          throw new Error('Invalid regular expression')
        }
        return new originalRegExp(pattern, flags)
      }
      
      const text = 'test string'
      const invalidRegexKeyword = 'test-error'
      
      // 这应该触发错误处理
      const result = highlightText(text, invalidRegexKeyword)
      expect(result).toBe(text)
      expect(consoleSpy).toHaveBeenCalledWith('Failed to highlight text:', expect.any(Error))
      
      // 恢复原始状态
      global.RegExp = originalRegExp
      consoleSpy.mockRestore()
    })
  })

  describe('highlightMultipleKeywords', () => {
    it('应该高亮多个关键词', () => {
      const text = 'This is a test string with multiple words'
      const keywords = ['test', 'multiple']
      const result = highlightMultipleKeywords(text, keywords)
      
      expect(result).toContain('<span class="highlight">test</span>')
      expect(result).toContain('<span class="highlight">multiple</span>')
    })

    it('应该处理空关键词数组', () => {
      const text = 'test string'
      const result = highlightMultipleKeywords(text, [])
      
      expect(result).toBe(text)
    })

    it('应该处理包含空字符串的关键词数组', () => {
      const text = 'test string'
      const keywords = ['test', '', '  ', 'string']
      const result = highlightMultipleKeywords(text, keywords)
      
      expect(result).toBe('<span class="highlight">test</span> <span class="highlight">string</span>')
    })

    it('应该处理重复的关键词', () => {
      const text = 'test test test'
      const keywords = ['test', 'test']
      const result = highlightMultipleKeywords(text, keywords)
      
      // 应该高亮所有匹配，即使关键词重复
      const matches = (result.match(/<span class="highlight">test<\/span>/g) || []).length
      expect(matches).toBe(3)
    })
  })

  describe('containsKeyword', () => {
    it('应该检测文本是否包含关键词', () => {
      expect(containsKeyword('This is a test', 'test')).toBe(true)
      expect(containsKeyword('This is a test', 'TEST')).toBe(true)
      expect(containsKeyword('This is a test', 'missing')).toBe(false)
    })

    it('应该处理空输入', () => {
      expect(containsKeyword('', 'test')).toBe(false)
      expect(containsKeyword('test', '')).toBe(false)
      expect(containsKeyword('', '')).toBe(false)
      expect(containsKeyword(null, 'test')).toBe(false)
      expect(containsKeyword('test', null)).toBe(false)
    })

    it('应该进行不区分大小写的检查', () => {
      expect(containsKeyword('Test String', 'test')).toBe(true)
      expect(containsKeyword('test string', 'TEST')).toBe(true)
      expect(containsKeyword('TeSt', 'test')).toBe(true)
    })
  })

  describe('extractSnippet', () => {
    it('应该提取包含关键词的片段', () => {
      const text = 'This is a very long text that contains the keyword somewhere in the middle and continues for a while'
      const keyword = 'keyword'
      const result = extractSnippet(text, keyword, 10)
      
      expect(result).toContain('keyword')
      expect(result.length).toBeLessThan(text.length)
      expect(result).toMatch(/^.*keyword.*$/)
    })

    it('应该在开头添加省略号', () => {
      const text = 'This is a very long text that contains the keyword at the end'
      const keyword = 'keyword'
      const result = extractSnippet(text, keyword, 10)
      
      expect(result).toMatch(/^\.\.\..*keyword/)
    })

    it('应该在结尾添加省略号', () => {
      const text = 'The keyword is at the beginning of this very long text'
      const keyword = 'keyword'
      const result = extractSnippet(text, keyword, 10)
      
      expect(result).toMatch(/keyword.*\.\.\.$/)
    })

    it('应该在不包含关键词时返回原文本', () => {
      const text = 'This text does not contain the search term'
      const keyword = 'missing'
      const result = extractSnippet(text, keyword, 10)
      
      expect(result).toBe(text)
    })

    it('应该处理短文本', () => {
      const text = 'Short test'
      const keyword = 'test'
      const result = extractSnippet(text, keyword, 50)
      
      expect(result).toBe(text)
      expect(result).not.toContain('...')
    })
  })

  describe('highlightAndTruncate', () => {
    it('应该高亮并截断长文本', () => {
      const text = 'This is a very long text that should be truncated and the keyword should be highlighted'
      const keyword = 'keyword'
      const result = highlightAndTruncate(text, keyword, 50)
      
      expect(result).toContain('<span class="highlight">keyword</span>')
      // 由于省略号(3字符)和上下文逻辑，实际长度可能稍长
      expect(result.replace(/<[^>]*>/g, '').length).toBeLessThanOrEqual(60)
    })

    it('应该在没有关键词时简单截断', () => {
      const text = 'This is a very long text that should be truncated without any highlighting'
      const result = highlightAndTruncate(text, '', 30)
      
      expect(result).toBe(text.substring(0, 30) + '...')
      expect(result).not.toContain('<span')
    })

    it('应该处理短文本', () => {
      const text = 'Short text'
      const keyword = 'text'
      const result = highlightAndTruncate(text, keyword, 50)
      
      expect(result).toBe('Short <span class="highlight">text</span>')
    })

    it('应该处理空文本', () => {
      expect(highlightAndTruncate('', 'test')).toBe('')
      expect(highlightAndTruncate(null, 'test')).toBe('')
      expect(highlightAndTruncate(undefined, 'test')).toBe('')
    })
  })

  describe('stripHtmlTags', () => {
    it('应该移除HTML标签', () => {
      const html = '<span class="highlight">test</span> <b>bold</b> text'
      const result = stripHtmlTags(html)
      
      expect(result).toBe('test bold text')
    })

    it('应该处理嵌套标签', () => {
      const html = '<div><span class="highlight">nested <b>tags</b></span></div>'
      const result = stripHtmlTags(html)
      
      expect(result).toBe('nested tags')
    })

    it('应该处理自闭合标签', () => {
      const html = 'Text with <br/> line break <img src="test.jpg"/> and more'
      const result = stripHtmlTags(html)
      
      expect(result).toBe('Text with  line break  and more')
    })

    it('应该处理空输入', () => {
      expect(stripHtmlTags('')).toBe('')
      expect(stripHtmlTags(null)).toBe('')
      expect(stripHtmlTags(undefined)).toBe('')
    })
  })

  describe('smartHighlight', () => {
    it('应该智能高亮分词查询', () => {
      const text = 'This is a test string with multiple words'
      const query = 'test multiple'
      const result = smartHighlight(text, query)
      
      expect(result).toContain('<span class="highlight">test</span>')
      expect(result).toContain('<span class="highlight">multiple</span>')
    })

    it('应该处理多个空格分隔的词', () => {
      const text = 'Search for certificate domain names'
      const query = 'certificate   domain'
      const result = smartHighlight(text, query)
      
      expect(result).toContain('<span class="highlight">certificate</span>')
      expect(result).toContain('<span class="highlight">domain</span>')
    })

    it('应该忽略空词', () => {
      const text = 'Test string'
      const query = 'test    string'
      const result = smartHighlight(text, query)
      
      expect(result).toBe('<span class="highlight">Test</span> <span class="highlight">string</span>')
    })

    it('应该处理空查询', () => {
      const text = 'test string'
      expect(smartHighlight(text, '')).toBe(text)
      expect(smartHighlight(text, '   ')).toBe(text)
      expect(smartHighlight(text, null)).toBe(text)
    })

    it('应该使用自定义类名', () => {
      const text = 'test string'
      const query = 'test'
      const className = 'custom'
      const result = smartHighlight(text, query, className)
      
      expect(result).toBe('<span class="custom">test</span> string')
    })
  })

  describe('性能和边界情况', () => {
    it('应该处理大文本', () => {
      const largeText = 'word '.repeat(10000) + 'keyword ' + 'word '.repeat(10000)
      const keyword = 'keyword'
      
      const startTime = Date.now()
      const result = highlightText(largeText, keyword)
      const endTime = Date.now()
      
      expect(result).toContain('<span class="highlight">keyword</span>')
      expect(endTime - startTime).toBeLessThan(1000) // 应该在1秒内完成
    })

    it('应该处理Unicode字符', () => {
      const text = '这是一个测试字符串'
      const keyword = '测试'
      const result = highlightText(text, keyword)
      
      expect(result).toBe('这是一个<span class="highlight">测试</span>字符串')
    })

    it('应该处理表情符号', () => {
      const text = 'Search 🔍 for certificates 📜'
      const keyword = 'Search'
      const result = highlightText(text, keyword)
      
      expect(result).toBe('<span class="highlight">Search</span> 🔍 for certificates 📜')
    })

    it('应该处理HTML实体', () => {
      const text = 'Test &amp; highlight &lt;tag&gt;'
      const keyword = 'Test'
      const result = highlightText(text, keyword)
      
      expect(result).toBe('<span class="highlight">Test</span> &amp; highlight &lt;tag&gt;')
    })
  })
})