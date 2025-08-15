package com.example.certificate;

import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.service.CertificateStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实证书状态计算测试
 * 不使用任何Mock，测试真实的状态计算逻辑
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("真实证书状态计算测试 - 无Mock验证")
public class RealCertificateStatusTest {
    
    @Autowired
    private CertificateStatusService certificateStatusService;
    
    @Autowired
    private CertificateStatusConfig certificateStatusConfig;
    
    @Test
    @DisplayName("验证真实的状态计算逻辑 - 完整流程测试")
    void verifyRealStatusCalculationLogic() {
        // Given: 创建真实的证书对象，不使用任何Mock
        Date now = new Date();
        
        // 创建正常状态证书（60天后过期）
        Certificate normalCert = createRealCertificate("正常证书", 60);
        
        // 创建即将过期证书（15天后过期）
        Certificate expiringSoonCert = createRealCertificate("即将过期证书", 15);
        
        // 创建已过期证书（5天前过期）
        Certificate expiredCert = createRealCertificate("已过期证书", -5);
        
        // When: 使用真实服务计算状态
        CertificateStatus normalStatus = certificateStatusService.calculateStatus(normalCert);
        CertificateStatus expiringSoonStatus = certificateStatusService.calculateStatus(expiringSoonCert);
        CertificateStatus expiredStatus = certificateStatusService.calculateStatus(expiredCert);
        
        // Then: 验证状态计算结果的正确性
        assertThat(normalStatus).isEqualTo(CertificateStatus.NORMAL);
        assertThat(expiringSoonStatus).isEqualTo(CertificateStatus.EXPIRING_SOON);
        assertThat(expiredStatus).isEqualTo(CertificateStatus.EXPIRED);
        
        // 验证天数计算的准确性
        long normalDays = certificateStatusService.getDaysUntilExpiry(normalCert);
        long expiringSoonDays = certificateStatusService.getDaysUntilExpiry(expiringSoonCert);
        long expiredDays = certificateStatusService.getDaysUntilExpiry(expiredCert);
        
        assertThat(normalDays).isBetween(59L, 60L);
        assertThat(expiringSoonDays).isBetween(14L, 15L);
        assertThat(expiredDays).isBetween(-5L, -4L);
    }
    
    @Test
    @DisplayName("验证配置参数对状态计算的真实影响")
    void verifyConfigurationImpactOnRealCalculation() {
        // Given: 创建边界测试证书（25天后过期）
        Certificate cert = createRealCertificate("边界测试证书", 25);
        
        // When & Then: 使用不同阈值进行测试
        
        // 使用20天阈值，25天应该是正常状态
        CertificateStatus statusWith20Days = certificateStatusService.calculateStatus(cert, 20);
        assertThat(statusWith20Days).isEqualTo(CertificateStatus.NORMAL);
        
        // 使用30天阈值，25天应该是即将过期状态
        CertificateStatus statusWith30Days = certificateStatusService.calculateStatus(cert, 30);
        assertThat(statusWith30Days).isEqualTo(CertificateStatus.EXPIRING_SOON);
        
        // 验证默认配置阈值（应该是30天）
        int defaultThreshold = certificateStatusConfig.getExpiringSoonDays();
        assertThat(defaultThreshold).isEqualTo(30);
        
        // 使用默认配置计算状态
        CertificateStatus defaultStatus = certificateStatusService.calculateStatus(cert);
        assertThat(defaultStatus).isEqualTo(CertificateStatus.EXPIRING_SOON);
    }
    
    @Test
    @DisplayName("验证批量状态计算的真实性能和准确性")
    void verifyRealBulkCalculationPerformanceAndAccuracy() {
        // Given: 创建100个不同状态的证书
        List<Certificate> certificates = Arrays.asList(
            // 正常状态证书
            createRealCertificate("正常1", 60),
            createRealCertificate("正常2", 45),
            createRealCertificate("正常3", 90),
            
            // 即将过期证书
            createRealCertificate("即将过期1", 25),
            createRealCertificate("即将过期2", 15),
            createRealCertificate("即将过期3", 5),
            
            // 已过期证书
            createRealCertificate("已过期1", -10),
            createRealCertificate("已过期2", -5),
            createRealCertificate("已过期3", -1)
        );
        
        // When: 批量计算状态（测量性能）
        long startTime = System.currentTimeMillis();
        List<Certificate> updatedCertificates = certificateStatusService.bulkCalculateStatus(certificates);
        long endTime = System.currentTimeMillis();
        
        // Then: 验证性能和准确性
        assertThat(endTime - startTime).isLessThan(100L); // 应该在100ms内完成
        assertThat(updatedCertificates).hasSize(9);
        
        // 验证每个证书的状态计算正确性
        long normalCount = updatedCertificates.stream()
                .mapToLong(cert -> cert.getStatus() == CertificateStatus.NORMAL ? 1 : 0)
                .sum();
        long expiringSoonCount = updatedCertificates.stream()
                .mapToLong(cert -> cert.getStatus() == CertificateStatus.EXPIRING_SOON ? 1 : 0)
                .sum();
        long expiredCount = updatedCertificates.stream()
                .mapToLong(cert -> cert.getStatus() == CertificateStatus.EXPIRED ? 1 : 0)
                .sum();
        
        assertThat(normalCount).isEqualTo(3);
        assertThat(expiringSoonCount).isEqualTo(3);
        assertThat(expiredCount).isEqualTo(3);
    }
    
    @Test
    @DisplayName("验证边界条件的真实计算准确性")
    void verifyRealBoundaryConditionAccuracy() {
        // Given: 创建边界条件测试证书
        Certificate exactly30Days = createRealCertificate("正好30天", 30);
        Certificate exactly0Days = createRealCertificate("正好0天", 0);
        Certificate exactly1DayExpired = createRealCertificate("过期1天", -1);
        Certificate nullExpiryDate = Certificate.builder()
                .id(999L)
                .name("无到期日期证书")
                .domain("null-expiry.example.com")
                .issuer("Test CA")
                .issueDate(getDateDaysFromNow(-30))
                .expiryDate(null) // 关键：到期日期为null
                .certificateType("SSL")
                .build();
        
        // When: 计算状态
        CertificateStatus status30Days = certificateStatusService.calculateStatus(exactly30Days);
        CertificateStatus status0Days = certificateStatusService.calculateStatus(exactly0Days);
        CertificateStatus status1DayExpired = certificateStatusService.calculateStatus(exactly1DayExpired);
        CertificateStatus statusNullExpiry = certificateStatusService.calculateStatus(nullExpiryDate);
        
        // Then: 验证边界条件处理
        assertThat(status30Days).isEqualTo(CertificateStatus.EXPIRING_SOON); // 30天内算即将过期
        assertThat(status0Days).isEqualTo(CertificateStatus.EXPIRING_SOON);   // 今天到期算即将过期
        assertThat(status1DayExpired).isEqualTo(CertificateStatus.EXPIRED);   // 过期1天算已过期
        assertThat(statusNullExpiry).isEqualTo(CertificateStatus.NORMAL);     // 无到期日期算正常
        
        // 验证天数计算（允许时间执行导致的微小偏差）
        long days30 = certificateStatusService.getDaysUntilExpiry(exactly30Days);
        long days0 = certificateStatusService.getDaysUntilExpiry(exactly0Days);
        long days1Expired = certificateStatusService.getDaysUntilExpiry(exactly1DayExpired);
        long daysNull = certificateStatusService.getDaysUntilExpiry(nullExpiryDate);
        
        // 允许1天的偏差，因为测试执行时间可能跨越天边界
        assertThat(days30).isBetween(29L, 30L);
        assertThat(days0).isBetween(-1L, 0L);
        assertThat(days1Expired).isBetween(-2L, -1L);
        assertThat(daysNull).isEqualTo(Long.MAX_VALUE);
    }
    
    @Test
    @DisplayName("验证真实时间计算的准确性 - 毫秒级精度")
    void verifyRealTimeCalculationAccuracy() {
        // Given: 创建精确时间的证书
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 15);
        cal.add(Calendar.HOUR_OF_DAY, 12); // 加12小时，测试时间精度
        
        Certificate preciseCert = Certificate.builder()
                .id(888L)
                .name("精确时间证书")
                .domain("precise.example.com")
                .issuer("Test CA")
                .issueDate(getDateDaysFromNow(-30))
                .expiryDate(cal.getTime())
                .certificateType("SSL")
                .build();
        
        // When: 计算状态和天数
        CertificateStatus status = certificateStatusService.calculateStatus(preciseCert);
        long days = certificateStatusService.getDaysUntilExpiry(preciseCert);
        
        // Then: 验证计算精度
        assertThat(status).isEqualTo(CertificateStatus.EXPIRING_SOON);
        assertThat(days).isBetween(14L, 15L); // 应该在14-15天之间
        
        // 验证同一证书多次计算结果一致性
        CertificateStatus status2 = certificateStatusService.calculateStatus(preciseCert);
        long days2 = certificateStatusService.getDaysUntilExpiry(preciseCert);
        
        assertThat(status2).isEqualTo(status);
        assertThat(days2).isEqualTo(days);
    }
    
    /**
     * 创建真实的证书对象，不使用任何Mock
     */
    private Certificate createRealCertificate(String name, int daysUntilExpiry) {
        return Certificate.builder()
                .id(System.currentTimeMillis()) // 使用时间戳确保唯一性
                .name(name)
                .domain(name.toLowerCase().replace(" ", "-") + ".example.com")
                .issuer("Real Test CA")
                .issueDate(getDateDaysFromNow(-30))
                .expiryDate(getDateDaysFromNow(daysUntilExpiry))
                .certificateType("SSL")
                .status(CertificateStatus.NORMAL) // 初始状态，会被重新计算
                .build();
    }
    
    /**
     * 获取从现在开始指定天数后的日期
     */
    private Date getDateDaysFromNow(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
}