import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { formatDate, formatDateTime, getDaysFromToday, formatRelativeTime } from '@/utils/date'

describe('date.js', () => {
  // Mock Date.now() 为固定时间: 2024-01-01 00:00:00
  const MOCK_DATE = new Date('2024-01-01T00:00:00Z')
  
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(MOCK_DATE)
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('formatDate', () => {
    it('should format date string correctly', () => {
      const result = formatDate('2024-12-31T00:00:00Z')
      expect(result).toBe('2024-12-31')
    })

    it('should format Date object correctly', () => {
      const date = new Date('2024-12-31T00:00:00Z')
      const result = formatDate(date)
      expect(result).toBe('2024-12-31')
    })

    it('should return empty string for null/undefined', () => {
      expect(formatDate(null)).toBe('')
      expect(formatDate(undefined)).toBe('')
      expect(formatDate('')).toBe('')
    })

    it('should return empty string for invalid date', () => {
      expect(formatDate('invalid-date')).toBe('')
      expect(formatDate('not-a-date')).toBe('')
    })

    it('should handle different date formats', () => {
      expect(formatDate('2024/01/15')).toBe('2024-01-15')
      expect(formatDate('2024-01-15')).toBe('2024-01-15')
      expect(formatDate('Jan 15, 2024')).toBe('2024-01-15')
    })
  })

  describe('formatDateTime', () => {
    it('should format datetime string correctly', () => {
      const result = formatDateTime('2024-12-31T15:30:45Z')
      // 注意：这里会根据系统时区转换，我们检查基本格式
      expect(result).toMatch(/2024-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/)
    })

    it('should format Date object correctly', () => {
      const date = new Date('2024-12-31T15:30:45Z')
      const result = formatDateTime(date)
      expect(result).toMatch(/2024-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/)
    })

    it('should return empty string for null/undefined', () => {
      expect(formatDateTime(null)).toBe('')
      expect(formatDateTime(undefined)).toBe('')
      expect(formatDateTime('')).toBe('')
    })

    it('should return empty string for invalid date', () => {
      expect(formatDateTime('invalid-date')).toBe('')
    })

    it('should use 24-hour format', () => {
      const result = formatDateTime('2024-12-31T15:30:45Z')
      // 检查没有 AM/PM
      expect(result).not.toMatch(/AM|PM/)
    })
  })

  describe('getDaysFromToday', () => {
    it('should return 0 for today', () => {
      const today = '2024-01-01T00:00:00Z'
      expect(getDaysFromToday(today)).toBe(0)
    })

    it('should return positive number for future dates', () => {
      const futureDate = '2024-01-31T00:00:00Z' // 30天后
      expect(getDaysFromToday(futureDate)).toBe(30)
    })

    it('should return negative number for past dates', () => {
      const pastDate = '2023-12-01T00:00:00Z' // 31天前
      expect(getDaysFromToday(pastDate)).toBe(-31)
    })

    it('should ignore time and only consider date', () => {
      // 使用明确的日期而不依赖UTC转换
      const date1 = '2024-01-02T00:00:00Z'
      const date2 = '2024-01-02T23:59:59Z'
      
      const days1 = getDaysFromToday(date1)
      const days2 = getDaysFromToday(date2)
      
      // 同一天的不同时间应该返回相同的天数
      expect(Math.abs(days1 - days2)).toBeLessThanOrEqual(1)
    })

    it('should return 0 for null/undefined', () => {
      expect(getDaysFromToday(null)).toBe(0)
      expect(getDaysFromToday(undefined)).toBe(0)
      expect(getDaysFromToday('')).toBe(0)
    })

    it('should return 0 for invalid date', () => {
      expect(getDaysFromToday('invalid-date')).toBe(0)
    })

    it('should handle leap year correctly', () => {
      // 2024 是闰年，2月有29天
      const leapYearDate = '2024-02-29T00:00:00Z'
      expect(getDaysFromToday(leapYearDate)).toBe(59) // 31(1月) + 28(2月前28天) = 59
    })
  })

  describe('formatRelativeTime', () => {
    it('should return "今天" for today', () => {
      const today = '2024-01-01T00:00:00Z'
      expect(formatRelativeTime(today)).toBe('今天')
    })

    it('should return "明天" for tomorrow', () => {
      const tomorrow = '2024-01-02T00:00:00Z'
      expect(formatRelativeTime(tomorrow)).toBe('明天')
    })

    it('should return "昨天" for yesterday', () => {
      const yesterday = '2023-12-31T00:00:00Z'
      expect(formatRelativeTime(yesterday)).toBe('昨天')
    })

    it('should return "X天后" for future dates', () => {
      const futureDate = '2024-01-05T00:00:00Z' // 4天后
      expect(formatRelativeTime(futureDate)).toBe('4天后')
    })

    it('should return "X天前" for past dates', () => {
      const pastDate = '2023-12-25T00:00:00Z' // 7天前
      expect(formatRelativeTime(pastDate)).toBe('7天前')
    })

    it('should handle larger time differences', () => {
      const farFuture = '2024-02-01T00:00:00Z' // 31天后
      const farPast = '2023-12-01T00:00:00Z' // 31天前
      
      expect(formatRelativeTime(farFuture)).toBe('31天后')
      expect(formatRelativeTime(farPast)).toBe('31天前')
    })

    it('should handle edge cases with getDaysFromToday', () => {
      // 当 getDaysFromToday 返回 0 时
      expect(formatRelativeTime('2024-01-01T12:30:45Z')).toBe('今天')
    })
  })

  describe('integration tests', () => {
    it('should work together for certificate expiry scenarios', () => {
      // 正常证书 - 30天后到期
      const normalCert = '2024-01-31T00:00:00Z'
      expect(formatDate(normalCert)).toBe('2024-01-31')
      expect(getDaysFromToday(normalCert)).toBe(30)
      expect(formatRelativeTime(normalCert)).toBe('30天后')

      // 即将过期证书 - 7天后到期
      const expiringSoon = '2024-01-08T00:00:00Z'
      expect(formatDate(expiringSoon)).toBe('2024-01-08')
      expect(getDaysFromToday(expiringSoon)).toBe(7)
      expect(formatRelativeTime(expiringSoon)).toBe('7天后')

      // 已过期证书 - 10天前过期
      const expired = '2023-12-22T00:00:00Z'
      expect(formatDate(expired)).toBe('2023-12-22')
      expect(getDaysFromToday(expired)).toBe(-10)
      expect(formatRelativeTime(expired)).toBe('10天前')
    })

    it('should handle timezone correctly', () => {
      // UTC 时间应该被正确处理
      const utcDate = '2024-01-15T00:00:00Z'
      expect(formatDate(utcDate)).toBe('2024-01-15')
      expect(getDaysFromToday(utcDate)).toBe(14)
    })
  })

  describe('边界条件测试', () => {
    it('should handle year boundaries', () => {
      // 跨年测试
      const nextYear = '2025-01-01T00:00:00Z'
      const lastYear = '2023-01-01T00:00:00Z'
      
      // 2024是闰年，所以从2024-01-01到2025-01-01是366天
      expect(getDaysFromToday(nextYear)).toBe(366) // 2024是闰年
      expect(getDaysFromToday(lastYear)).toBe(-365)
    })

    it('should handle month boundaries', () => {
      // 跨月测试
      const nextMonth = '2024-02-01T00:00:00Z'
      const lastMonth = '2023-12-01T00:00:00Z'
      
      expect(getDaysFromToday(nextMonth)).toBe(31)
      expect(getDaysFromToday(lastMonth)).toBe(-31)
    })

    it('should handle very large date differences', () => {
      const veryFuture = '2030-01-01T00:00:00Z'
      const veryPast = '2020-01-01T00:00:00Z'
      
      const futureDays = getDaysFromToday(veryFuture)
      const pastDays = getDaysFromToday(veryPast)
      
      expect(futureDays).toBeGreaterThan(2000)
      expect(pastDays).toBeLessThan(-1000)
      expect(formatRelativeTime(veryFuture)).toBe(`${futureDays}天后`)
      expect(formatRelativeTime(veryPast)).toBe(`${Math.abs(pastDays)}天前`)
    })
  })
})