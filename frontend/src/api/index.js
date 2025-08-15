/**
 * API 接口统一导出
 */

// 导入各模块的 API
import * as certificate from './certificate'
import * as monitoring from './monitoring'
import * as auth from './auth'
import * as system from './system'

// 统一导出
export default {
  certificate,
  monitoring,
  auth,
  system
}

// 也可以按需导出
export {
  certificate,
  monitoring,
  auth,
  system
}