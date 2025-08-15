package com.example.certificate.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 证书状态计算单元测试
 * 测试证书领域模型中的状态计算功能
 * 
 * @author Auto Generated
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("证书状态计算测试")
class CertificateStatusTest {
    
    private Certificate certificate;
    private Date now;
    
    @BeforeEach
    void setUp() {
        now = new Date();
        certificate = Certificate.builder()
                .id(1L)
                .name("测试证书")
                .domain("test.example.com")
                .issuer("Test CA")
                .issueDate(getDateDaysFromNow(-365)) // 一年前颁发
                .certificateType("SSL")
                .build();
    }
    
    @Test
    @DisplayName("测试正常状态计算 - 距离到期超过30天")
    void calculateStatus_normal_moreThan30Days() {
        // Given: 证书60天后到期
        certificate.setExpiryDate(getDateDaysFromNow(60));
        
        // When: 计算状态
        CertificateStatus status = certificate.calculateStatus();
        
        // Then: 状态应为 NORMAL
        assertThat(status).isEqualTo(CertificateStatus.NORMAL);
    }
    
    @Test
    @DisplayName("测试即将过期状态计算 - 距离到期30天以内")
    void calculateStatus_expiringSoon_within30Days() {
        // Given: 证书15天后到期
        certificate.setExpiryDate(getDateDaysFromNow(15));
        
        // When: 计算状态
        CertificateStatus status = certificate.calculateStatus();
        
        // Then: 状态应为 EXPIRING_SOON
        assertThat(status).isEqualTo(CertificateStatus.EXPIRING_SOON);
    }
    
    @Test
    @DisplayName("测试已过期状态计算 - 已经过期")
    void calculateStatus_expired_pastExpiryDate() {
        // Given: 证书5天前已过期
        certificate.setExpiryDate(getDateDaysFromNow(-5));
        
        // When: 计算状态
        CertificateStatus status = certificate.calculateStatus();
        
        // Then: 状态应为 EXPIRED
        assertThat(status).isEqualTo(CertificateStatus.EXPIRED);
    }
    
    @Test
    @DisplayName("测试边界条件 - 正好30天到期")
    void calculateStatus_boundary_exactly30Days() {
        // Given: 证书正好30天后到期
        certificate.setExpiryDate(getDateDaysFromNow(30));
        
        // When: 计算状态
        CertificateStatus status = certificate.calculateStatus();
        
        // Then: 状态应为 EXPIRING_SOON（30天以内包含30天）
        assertThat(status).isEqualTo(CertificateStatus.EXPIRING_SOON);
    }
    
    @Test
    @DisplayName("测试边界条件 - 正好0天到期")
    void calculateStatus_boundary_exactly0Days() {
        // Given: 证书今天到期
        certificate.setExpiryDate(getDateDaysFromNow(0));
        
        // When: 计算状态
        CertificateStatus status = certificate.calculateStatus();
        
        // Then: 状态应为 EXPIRING_SOON（今天到期还未过期）
        assertThat(status).isEqualTo(CertificateStatus.EXPIRING_SOON);
    }
    
    @Test
    @DisplayName("测试自定义阈值状态计算 - 15天阈值")
    void calculateStatus_customThreshold_15Days() {
        // Given: 证书20天后到期，使用15天阈值
        certificate.setExpiryDate(getDateDaysFromNow(20));
        
        // When: 使用15天阈值计算状态
        CertificateStatus status = certificate.calculateStatus(15);
        
        // Then: 状态应为 NORMAL（超过15天阈值）
        assertThat(status).isEqualTo(CertificateStatus.NORMAL);
    }
    
    @Test
    @DisplayName("测试自定义阈值状态计算 - 10天内即将过期")
    void calculateStatus_customThreshold_expiringSoon() {
        // Given: 证书8天后到期，使用15天阈值
        certificate.setExpiryDate(getDateDaysFromNow(8));
        
        // When: 使用15天阈值计算状态
        CertificateStatus status = certificate.calculateStatus(15);
        
        // Then: 状态应为 EXPIRING_SOON（在15天阈值内）
        assertThat(status).isEqualTo(CertificateStatus.EXPIRING_SOON);
    }
    
    @Test
    @DisplayName("测试 getDaysUntilExpiry 方法 - 未来日期")
    void getDaysUntilExpiry_futureDate() {
        // Given: 证书45天后到期
        certificate.setExpiryDate(getDateDaysFromNow(45));
        
        // When: 获取距离到期天数
        long days = certificate.getDaysUntilExpiry();
        
        // Then: 应返回接近45天的数值
        assertThat(days).isBetween(44L, 45L); // 允许一些时间误差
    }
    
    @Test
    @DisplayName("测试 getDaysUntilExpiry 方法 - 过去日期")
    void getDaysUntilExpiry_pastDate() {
        // Given: 证书10天前已过期
        certificate.setExpiryDate(getDateDaysFromNow(-10));
        
        // When: 获取距离到期天数
        long days = certificate.getDaysUntilExpiry();
        
        // Then: 应返回负数
        assertThat(days).isBetween(-10L, -9L);
    }
    
    @Test
    @DisplayName("测试 isExpired 方法 - 已过期证书")
    void isExpired_expiredCertificate() {
        // Given: 证书已过期
        certificate.setExpiryDate(getDateDaysFromNow(-1));
        
        // When & Then: 应返回 true
        assertThat(certificate.isExpired()).isTrue();
    }
    
    @Test
    @DisplayName("测试 isExpired 方法 - 未过期证书")
    void isExpired_validCertificate() {
        // Given: 证书未过期
        certificate.setExpiryDate(getDateDaysFromNow(10));
        
        // When & Then: 应返回 false
        assertThat(certificate.isExpired()).isFalse();
    }
    
    @Test
    @DisplayName("测试 isExpiringSoon 方法 - 即将过期")
    void isExpiringSoon_expiringSoonCertificate() {
        // Given: 证书20天后过期
        certificate.setExpiryDate(getDateDaysFromNow(20));
        
        // When & Then: 应返回 true（默认30天阈值）
        assertThat(certificate.isExpiringSoon()).isTrue();
    }
    
    @Test
    @DisplayName("测试 isExpiringSoon 方法 - 不会很快过期")
    void isExpiringSoon_notExpiringSoon() {
        // Given: 证书50天后过期
        certificate.setExpiryDate(getDateDaysFromNow(50));
        
        // When & Then: 应返回 false
        assertThat(certificate.isExpiringSoon()).isFalse();
    }
    
    @Test
    @DisplayName("测试 isExpiringSoon 自定义阈值方法")
    void isExpiringSoon_customThreshold() {
        // Given: 证书25天后过期
        certificate.setExpiryDate(getDateDaysFromNow(25));
        
        // When & Then: 
        assertThat(certificate.isExpiringSoon(20)).isFalse(); // 20天阈值
        assertThat(certificate.isExpiringSoon(30)).isTrue();  // 30天阈值
    }
    
    @Test
    @DisplayName("测试 updateStatus 方法")
    void updateStatus_shouldUpdateCertificateStatus() {
        // Given: 证书15天后过期
        certificate.setExpiryDate(getDateDaysFromNow(15));
        
        // When: 更新状态
        certificate.updateStatus();
        
        // Then: 状态应被设置为 EXPIRING_SOON
        assertThat(certificate.getStatus()).isEqualTo(CertificateStatus.EXPIRING_SOON);
    }
    
    @Test
    @DisplayName("测试 updateStatus 自定义阈值方法")
    void updateStatus_customThreshold() {
        // Given: 证书25天后过期
        certificate.setExpiryDate(getDateDaysFromNow(25));
        
        // When: 使用20天阈值更新状态
        certificate.updateStatus(20);
        
        // Then: 状态应为 NORMAL
        assertThat(certificate.getStatus()).isEqualTo(CertificateStatus.NORMAL);
    }
    
    @Test
    @DisplayName("测试空的到期日期")
    void calculateStatus_nullExpiryDate() {
        // Given: 到期日期为空
        certificate.setExpiryDate(null);
        
        // When: 计算状态
        CertificateStatus status = certificate.calculateStatus();
        
        // Then: 应返回 NORMAL
        assertThat(status).isEqualTo(CertificateStatus.NORMAL);
    }
    
    @Test
    @DisplayName("测试空的到期日期的天数计算")
    void getDaysUntilExpiry_nullExpiryDate() {
        // Given: 到期日期为空
        certificate.setExpiryDate(null);
        
        // When: 获取距离到期天数
        long days = certificate.getDaysUntilExpiry();
        
        // Then: 应返回最大值
        assertThat(days).isEqualTo(Long.MAX_VALUE);
    }
    
    /**
     * 获取从现在开始指定天数后的日期
     */
    private Date getDateDaysFromNow(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
}