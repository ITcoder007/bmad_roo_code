package com.example.certificate.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    private Long id;
    private String name;
    private String domain;
    private String issuer;
    private Date issueDate;
    private Date expiryDate;
    private String certificateType;
    private CertificateStatus status;
    private Date createdAt;
    private Date updatedAt;
    
    /**
     * 计算证书的状态（使用默认30天阈值）
     * @return 根据到期时间计算的证书状态
     */
    public CertificateStatus calculateStatus() {
        return calculateStatus(30);
    }
    
    /**
     * 计算证书的状态
     * @param expiringSoonThresholdDays 即将过期的阈值天数
     * @return 根据到期时间计算的证书状态
     */
    public CertificateStatus calculateStatus(int expiringSoonThresholdDays) {
        if (expiryDate == null) {
            return CertificateStatus.NORMAL;
        }
        
        Date now = new Date();
        long diffInMillis = expiryDate.getTime() - now.getTime();
        long daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        
        if (daysUntilExpiry < 0) {
            return CertificateStatus.EXPIRED;
        } else if (daysUntilExpiry <= expiringSoonThresholdDays) {
            return CertificateStatus.EXPIRING_SOON;
        } else {
            return CertificateStatus.NORMAL;
        }
    }
    
    /**
     * 获取距离到期的天数
     * @return 距离到期的天数，负数表示已过期
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return Long.MAX_VALUE;
        }
        
        Date now = new Date();
        long diffInMillis = expiryDate.getTime() - now.getTime();
        return TimeUnit.MILLISECONDS.toDays(diffInMillis);
    }
    
    /**
     * 判断证书是否已过期
     * @return true 如果证书已过期
     */
    public boolean isExpired() {
        return getDaysUntilExpiry() < 0;
    }
    
    /**
     * 判断证书是否即将过期（30天内）
     * @return true 如果证书即将过期
     */
    public boolean isExpiringSoon() {
        return isExpiringSoon(30);
    }
    
    /**
     * 判断证书是否在指定天数内即将过期
     * @param days 阈值天数
     * @return true 如果证书在指定天数内即将过期
     */
    public boolean isExpiringSoon(int days) {
        long daysUntilExpiry = getDaysUntilExpiry();
        return daysUntilExpiry >= 0 && daysUntilExpiry <= days;
    }
    
    /**
     * 更新证书状态（使用默认30天阈值）
     */
    public void updateStatus() {
        this.status = calculateStatus();
    }
    
    /**
     * 更新证书状态
     * @param expiringSoonThresholdDays 即将过期的阈值天数
     */
    public void updateStatus(int expiringSoonThresholdDays) {
        this.status = calculateStatus(expiringSoonThresholdDays);
    }
}