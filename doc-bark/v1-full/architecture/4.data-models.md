## 数据模型

定义将在前端和后端之间共享的核心数据模型/实体：

### Certificate（证书）

**目的：** 存储和管理证书的基本信息和状态

**关键属性：**
- id: Long - 证书唯一标识符
- name: String - 证书名称
- domain: String - 证书关联的域名
- issuer: String - 证书颁发机构
- issueDate: Date - 证书颁发日期
- expiryDate: Date - 证书到期日期
- certificateType: String - 证书类型（如 SSL/TLS、代码签名等）
- status: CertificateStatus - 证书状态（正常、即将过期、已过期）
- createdAt: Date - 记录创建时间
- updatedAt: Date - 记录更新时间

#### TypeScript 接口

```typescript
export interface Certificate {
  id: number;
  name: string;
  domain: string;
  issuer: string;
  issueDate: Date;
  expiryDate: Date;
  certificateType: string;
  status: CertificateStatus;
  createdAt: Date;
  updatedAt: Date;
}

export enum CertificateStatus {
  NORMAL = 'NORMAL',
  EXPIRING_SOON = 'EXPIRING_SOON',
  EXPIRED = 'EXPIRED'
}
```

#### 关系

- 与 MonitoringLog 一对多关系（一个证书可以有多个监控日志）

### MonitoringLog（监控日志）

**目的：** 记录证书监控和预警活动的日志

**关键属性：**
- id: Long - 日志唯一标识符
- certificateId: Long - 关联的证书ID
- logType: String - 日志类型（MONITORING、ALERT_EMAIL、ALERT_SMS）
- logTime: Date - 日志记录时间
- message: String - 日志消息内容
- daysUntilExpiry: Integer - 距离到期的天数
- createdAt: Date - 记录创建时间

#### TypeScript 接口

```typescript
export interface MonitoringLog {
  id: number;
  certificateId: number;
  logType: string;
  logTime: Date;
  message: string;
  daysUntilExpiry: number;
  createdAt: Date;
}

export enum LogType {
  MONITORING = 'MONITORING',
  ALERT_EMAIL = 'ALERT_EMAIL',
  ALERT_SMS = 'ALERT_SMS'
}
```

#### 关系

- 与 Certificate 多对一关系（多个监控日志属于一个证书）