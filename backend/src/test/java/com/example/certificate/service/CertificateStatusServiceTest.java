package com.example.certificate.service;

import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.service.impl.CertificateStatusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 证书状态服务测试类
 * 测试证书状态计算和批量更新功能
 * 
 * @author Auto Generated
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("证书状态服务测试")
class CertificateStatusServiceTest {
    
    @Mock
    private CertificateRepository certificateRepository;
    
    @Mock
    private CertificateStatusConfig certificateStatusConfig;
    
    @InjectMocks
    private CertificateStatusServiceImpl certificateStatusService;
    
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
                .issueDate(getDateDaysFromNow(-365))
                .expiryDate(getDateDaysFromNow(15)) // 15天后过期
                .certificateType("SSL")
                .status(CertificateStatus.NORMAL)
                .build();
    }
    
    @Test
    @DisplayName("测试单个证书状态计算 - 使用默认阈值")
    void calculateStatus_singleCertificate_defaultThreshold() {
        // Given: 设置配置
        when(certificateStatusConfig.getExpiringSoonDays()).thenReturn(30);
        
        // When: 计算状态
        CertificateStatus status = certificateStatusService.calculateStatus(certificate);
        
        // Then: 状态应为 EXPIRING_SOON（15天 < 30天阈值）
        assertThat(status).isEqualTo(CertificateStatus.EXPIRING_SOON);
    }
    
    @Test
    @DisplayName("测试单个证书状态计算 - 使用自定义阈值")
    void calculateStatus_singleCertificate_customThreshold() {
        // When: 使用10天阈值计算状态
        CertificateStatus status = certificateStatusService.calculateStatus(certificate, 10);
        
        // Then: 状态应为 NORMAL（15天 > 10天阈值）
        assertThat(status).isEqualTo(CertificateStatus.NORMAL);
    }
    
    @Test
    @DisplayName("测试空证书对象的状态计算")
    void calculateStatus_nullCertificate() {
        // When: 计算空证书的状态
        CertificateStatus status = certificateStatusService.calculateStatus(null);
        
        // Then: 应返回 NORMAL
        assertThat(status).isEqualTo(CertificateStatus.NORMAL);
    }
    
    @Test
    @DisplayName("测试批量证书状态计算 - 使用默认阈值")
    void bulkCalculateStatus_defaultThreshold() {
        // Given: 多个证书和配置的默认阈值
        when(certificateStatusConfig.getExpiringSoonDays()).thenReturn(30);
        Certificate cert1 = createCertificate(1L, 45); // 正常
        Certificate cert2 = createCertificate(2L, 15); // 即将过期
        Certificate cert3 = createCertificate(3L, -5); // 已过期
        List<Certificate> certificates = Arrays.asList(cert1, cert2, cert3);
        
        // When: 批量计算状态
        List<Certificate> result = certificateStatusService.bulkCalculateStatus(certificates);
        
        // Then: 状态应被正确设置
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getStatus()).isEqualTo(CertificateStatus.NORMAL);
        assertThat(result.get(1).getStatus()).isEqualTo(CertificateStatus.EXPIRING_SOON);
        assertThat(result.get(2).getStatus()).isEqualTo(CertificateStatus.EXPIRED);
    }
    
    @Test
    @DisplayName("测试批量证书状态计算 - 使用自定义阈值")
    void bulkCalculateStatus_customThreshold() {
        // Given: 多个证书
        Certificate cert1 = createCertificate(1L, 25); // 在20天阈值下为正常
        Certificate cert2 = createCertificate(2L, 15); // 在20天阈值下为即将过期
        List<Certificate> certificates = Arrays.asList(cert1, cert2);
        
        // When: 使用20天阈值批量计算状态
        List<Certificate> result = certificateStatusService.bulkCalculateStatus(certificates, 20);
        
        // Then: 状态应被正确设置
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(CertificateStatus.NORMAL);
        assertThat(result.get(1).getStatus()).isEqualTo(CertificateStatus.EXPIRING_SOON);
    }
    
    @Test
    @DisplayName("测试空证书列表的批量状态计算")
    void bulkCalculateStatus_emptyCertificateList() {
        // When: 计算空列表的状态
        List<Certificate> result = certificateStatusService.bulkCalculateStatus(Collections.emptyList());
        
        // Then: 应返回空列表
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("测试空证书列表的批量状态计算")
    void bulkCalculateStatus_nullCertificateList() {
        // When: 计算 null 列表的状态
        List<Certificate> result = certificateStatusService.bulkCalculateStatus(null);
        
        // Then: 应返回 null
        assertThat(result).isNull();
    }
    
    @Test
    @DisplayName("测试获取证书距离到期天数")
    void getDaysUntilExpiry_validCertificate() {
        // When: 获取距离到期天数
        long days = certificateStatusService.getDaysUntilExpiry(certificate);
        
        // Then: 应返回接近15天的数值
        assertThat(days).isBetween(14L, 15L);
    }
    
    @Test
    @DisplayName("测试空证书的距离到期天数")
    void getDaysUntilExpiry_nullCertificate() {
        // When: 获取空证书的距离到期天数
        long days = certificateStatusService.getDaysUntilExpiry(null);
        
        // Then: 应返回最大值
        assertThat(days).isEqualTo(Long.MAX_VALUE);
    }
    
    @Test
    @DisplayName("测试批量更新所有证书状态 - 使用默认阈值")
    void updateAllCertificateStatus_defaultThreshold() {
        // Given: 模拟分页数据和配置
        when(certificateStatusConfig.getExpiringSoonDays()).thenReturn(30);
        when(certificateStatusConfig.getBatchSize()).thenReturn(100);
        Certificate cert1 = createCertificate(1L, 45);
        Certificate cert2 = createCertificate(2L, 15);
        List<Certificate> page1 = Arrays.asList(cert1, cert2);
        List<Certificate> emptyPage = Collections.emptyList();
        
        when(certificateRepository.selectAllPaged(1, 100)).thenReturn(page1);
        when(certificateRepository.selectAllPaged(2, 100)).thenReturn(emptyPage);
        when(certificateRepository.updateStatus(anyLong(), any(CertificateStatus.class))).thenReturn(1);
        
        // When: 更新所有证书状态
        int updatedCount = certificateStatusService.updateAllCertificateStatus();
        
        // Then: 应更新2个证书
        assertThat(updatedCount).isEqualTo(2);
        verify(certificateRepository, times(2)).updateStatus(anyLong(), any(CertificateStatus.class));
    }
    
    @Test
    @DisplayName("测试批量更新所有证书状态 - 使用自定义阈值")
    void updateAllCertificateStatus_customThreshold() {
        // Given: 模拟分页数据和配置
        when(certificateStatusConfig.getBatchSize()).thenReturn(100);
        Certificate cert1 = createCertificate(1L, 25);
        List<Certificate> page1 = Arrays.asList(cert1);
        List<Certificate> emptyPage = Collections.emptyList();
        
        when(certificateRepository.selectAllPaged(1, 100)).thenReturn(page1);
        when(certificateRepository.selectAllPaged(2, 100)).thenReturn(emptyPage);
        when(certificateRepository.updateStatus(anyLong(), any(CertificateStatus.class))).thenReturn(1);
        
        // When: 使用20天阈值更新所有证书状态
        int updatedCount = certificateStatusService.updateAllCertificateStatus(20);
        
        // Then: 应更新1个证书
        assertThat(updatedCount).isEqualTo(1);
        verify(certificateRepository).updateStatus(eq(1L), eq(CertificateStatus.NORMAL));
    }
    
    @Test
    @DisplayName("测试批量更新时处理更新失败的情况")
    void updateAllCertificateStatus_handleUpdateFailure() {
        // Given: 模拟分页数据和更新失败
        when(certificateStatusConfig.getExpiringSoonDays()).thenReturn(30);
        when(certificateStatusConfig.getBatchSize()).thenReturn(100);
        Certificate cert1 = createCertificate(1L, 15);
        List<Certificate> page1 = Arrays.asList(cert1);
        List<Certificate> emptyPage = Collections.emptyList();
        
        when(certificateRepository.selectAllPaged(1, 100)).thenReturn(page1);
        when(certificateRepository.selectAllPaged(2, 100)).thenReturn(emptyPage);
        when(certificateRepository.updateStatus(anyLong(), any(CertificateStatus.class)))
                .thenThrow(new RuntimeException("数据库更新失败"));
        
        // When: 更新所有证书状态
        int updatedCount = certificateStatusService.updateAllCertificateStatus();
        
        // Then: 更新失败，应返回0
        assertThat(updatedCount).isEqualTo(0);
    }
    
    @Test
    @DisplayName("测试批量更新使用配置的批量大小")
    void updateAllCertificateStatus_usesConfiguredBatchSize() {
        // Given: 设置批量大小为50
        when(certificateStatusConfig.getBatchSize()).thenReturn(50);
        when(certificateRepository.selectAllPaged(anyInt(), eq(50))).thenReturn(Collections.emptyList());
        
        // When: 更新所有证书状态
        certificateStatusService.updateAllCertificateStatus();
        
        // Then: 应使用配置的批量大小
        verify(certificateRepository).selectAllPaged(1, 50);
    }
    
    @Test
    @DisplayName("测试状态计算的性能 - 大量证书")
    void bulkCalculateStatus_performanceTest() {
        // Given: 创建1000个证书
        List<Certificate> certificates = createMultipleCertificates(1000);
        
        // When: 测量批量计算时间
        long startTime = System.currentTimeMillis();
        List<Certificate> result = certificateStatusService.bulkCalculateStatus(certificates);
        long endTime = System.currentTimeMillis();
        
        // Then: 应在合理时间内完成（小于1秒）
        long executionTime = endTime - startTime;
        assertThat(executionTime).isLessThan(1000L);
        assertThat(result).hasSize(1000);
        
        // 验证状态计算的正确性
        long normalCount = result.stream().mapToLong(cert -> 
                cert.getStatus() == CertificateStatus.NORMAL ? 1 : 0).sum();
        long expiringSoonCount = result.stream().mapToLong(cert -> 
                cert.getStatus() == CertificateStatus.EXPIRING_SOON ? 1 : 0).sum();
        long expiredCount = result.stream().mapToLong(cert -> 
                cert.getStatus() == CertificateStatus.EXPIRED ? 1 : 0).sum();
        
        assertThat(normalCount + expiringSoonCount + expiredCount).isEqualTo(1000);
    }
    
    @Test
    @DisplayName("测试配置参数的影响")
    void configurationParametersEffect() {
        // Given: 设置不同的阈值
        when(certificateStatusConfig.getExpiringSoonDays()).thenReturn(45);
        
        Certificate cert = createCertificate(1L, 35); // 35天后过期
        
        // When: 使用配置阈值计算状态
        CertificateStatus status = certificateStatusService.calculateStatus(cert);
        
        // Then: 在45天阈值下应为即将过期
        assertThat(status).isEqualTo(CertificateStatus.EXPIRING_SOON);
    }
    
    @Test
    @DisplayName("测试时区处理的一致性")
    void timezoneHandlingConsistency() {
        // Given: 创建在不同时间创建的证书
        Certificate cert1 = createCertificate(1L, 30);
        Certificate cert2 = createCertificate(2L, 30);
        
        // When: 分别计算状态
        CertificateStatus status1 = certificateStatusService.calculateStatus(cert1);
        CertificateStatus status2 = certificateStatusService.calculateStatus(cert2);
        
        // Then: 状态应一致
        assertThat(status1).isEqualTo(status2);
    }
    
    /**
     * 创建测试用证书
     */
    private Certificate createCertificate(Long id, int daysUntilExpiry) {
        return Certificate.builder()
                .id(id)
                .name("测试证书" + id)
                .domain("test" + id + ".example.com")
                .issuer("Test CA")
                .issueDate(getDateDaysFromNow(-365))
                .expiryDate(getDateDaysFromNow(daysUntilExpiry))
                .certificateType("SSL")
                .status(CertificateStatus.NORMAL)
                .build();
    }
    
    /**
     * 创建多个测试证书
     */
    private List<Certificate> createMultipleCertificates(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    // 创建不同到期时间的证书来测试各种状态
                    int daysUntilExpiry;
                    if (i % 3 == 0) {
                        daysUntilExpiry = 60; // 正常
                    } else if (i % 3 == 1) {
                        daysUntilExpiry = 15; // 即将过期
                    } else {
                        daysUntilExpiry = -10; // 已过期
                    }
                    return createCertificate((long) i, daysUntilExpiry);
                })
                .collect(java.util.stream.Collectors.toList());
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