package com.example.certificate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.certificate.common.exception.BusinessException;
import com.example.certificate.common.exception.ErrorCode;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.service.converter.CertificateServiceConverter;
import com.example.certificate.service.dto.CertificateCreateDto;
import com.example.certificate.service.dto.CertificateDto;
import com.example.certificate.service.dto.CertificateUpdateDto;
import com.example.certificate.service.impl.CertificateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 证书服务层单元测试
 * 使用 Mockito 模拟依赖，测试业务逻辑和异常处理
 */
@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {
    
    @Mock
    private CertificateRepository certificateRepository;
    
    @Mock
    private CertificateServiceConverter serviceConverter;
    
    @InjectMocks
    private CertificateServiceImpl certificateService;
    
    private Certificate testCertificate;
    private CertificateDto testCertificateDto;
    private CertificateCreateDto testCreateDto;
    private CertificateUpdateDto testUpdateDto;
    
    @BeforeEach
    void setUp() {
        Date now = new Date();
        Date futureDate = new Date(now.getTime() + 86400000L * 365); // 一年后
        
        testCertificate = Certificate.builder()
                .id(1L)
                .name("测试证书")
                .domain("example.com")
                .issuer("Let's Encrypt")
                .issueDate(now)
                .expiryDate(futureDate)
                .certificateType("SSL")
                .status(CertificateStatus.NORMAL)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        testCertificateDto = CertificateDto.builder()
                .id(1L)
                .name("测试证书")
                .domain("example.com")
                .issuer("Let's Encrypt")
                .issueDate(now)
                .expiryDate(futureDate)
                .certificateType("SSL")
                .status("NORMAL")
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        testCreateDto = CertificateCreateDto.builder()
                .name("测试证书")
                .domain("example.com")
                .issuer("Let's Encrypt")
                .issueDate(now)
                .expiryDate(futureDate)
                .certificateType("SSL")
                .build();
        
        testUpdateDto = CertificateUpdateDto.builder()
                .name("更新后的证书")
                .domain("updated.example.com")
                .issuer("DigiCert")
                .certificateType("SSL")
                .build();
    }
    
    @Test
    void createCertificate_Success() {
        // 准备
        when(certificateRepository.findByDomain("example.com")).thenReturn(Optional.empty());
        when(serviceConverter.toCreateDomain(testCreateDto)).thenReturn(testCertificate);
        when(certificateRepository.save(any(Certificate.class))).thenReturn(testCertificate);
        when(serviceConverter.toDto(testCertificate)).thenReturn(testCertificateDto);
        
        // 执行
        CertificateDto result = certificateService.createCertificate(testCreateDto);
        
        // 验证
        assertNotNull(result);
        assertEquals("测试证书", result.getName());
        assertEquals("example.com", result.getDomain());
        
        verify(certificateRepository).findByDomain("example.com");
        verify(serviceConverter).toCreateDomain(testCreateDto);
        verify(certificateRepository).save(any(Certificate.class));
        verify(serviceConverter).toDto(testCertificate);
    }
    
    @Test
    void createCertificate_DomainExists() {
        // 准备
        when(certificateRepository.findByDomain("example.com")).thenReturn(Optional.of(testCertificate));
        
        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> certificateService.createCertificate(testCreateDto));
        
        assertEquals(ErrorCode.CERTIFICATE_DOMAIN_EXISTS.getCode(), exception.getCode());
        
        verify(certificateRepository).findByDomain("example.com");
        verify(serviceConverter, never()).toCreateDomain(any());
        verify(certificateRepository, never()).save(any());
    }
    
    @Test
    void createCertificate_InvalidDateRange() {
        // 准备无效日期范围的数据
        Date now = new Date();
        Date pastDate = new Date(now.getTime() - 86400000L); // 昨天
        
        CertificateCreateDto invalidDto = CertificateCreateDto.builder()
                .name(testCreateDto.getName())
                .domain(testCreateDto.getDomain())
                .issuer(testCreateDto.getIssuer())
                .issueDate(now)
                .expiryDate(pastDate) // 到期日期早于颁发日期
                .certificateType(testCreateDto.getCertificateType())
                .build();
        
        when(certificateRepository.findByDomain("example.com")).thenReturn(Optional.empty());
        
        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> certificateService.createCertificate(invalidDto));
        
        assertEquals(ErrorCode.CERTIFICATE_INVALID_DATE.getCode(), exception.getCode());
        
        verify(certificateRepository).findByDomain("example.com");
        verify(serviceConverter, never()).toCreateDomain(any());
    }
    
    @Test
    void getCertificateById_Success() {
        // 准备
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));
        when(serviceConverter.toDto(testCertificate)).thenReturn(testCertificateDto);
        
        // 执行
        CertificateDto result = certificateService.getCertificateById(1L);
        
        // 验证
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试证书", result.getName());
        
        verify(certificateRepository).findById(1L);
        verify(serviceConverter).toDto(testCertificate);
    }
    
    @Test
    void getCertificateById_NotFound() {
        // 准备
        when(certificateRepository.findById(999L)).thenReturn(Optional.empty());
        
        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> certificateService.getCertificateById(999L));
        
        assertEquals(ErrorCode.CERTIFICATE_NOT_FOUND.getCode(), exception.getCode());
        
        verify(certificateRepository).findById(999L);
        verify(serviceConverter, never()).toDto(any());
    }
    
    @Test
    void updateCertificate_Success() {
        // 准备
        Certificate updatedCertificate = Certificate.builder()
                .id(testCertificate.getId())
                .name("更新后的证书")
                .domain("updated.example.com")
                .issuer(testCertificate.getIssuer())
                .issueDate(testCertificate.getIssueDate())
                .expiryDate(testCertificate.getExpiryDate())
                .certificateType(testCertificate.getCertificateType())
                .status(testCertificate.getStatus())
                .createdAt(testCertificate.getCreatedAt())
                .updatedAt(testCertificate.getUpdatedAt())
                .build();
        
        CertificateDto updatedDto = CertificateDto.builder()
                .id(testCertificateDto.getId())
                .name("更新后的证书")
                .domain("updated.example.com")
                .issuer(testCertificateDto.getIssuer())
                .issueDate(testCertificateDto.getIssueDate())
                .expiryDate(testCertificateDto.getExpiryDate())
                .certificateType(testCertificateDto.getCertificateType())
                .status(testCertificateDto.getStatus())
                .createdAt(testCertificateDto.getCreatedAt())
                .updatedAt(testCertificateDto.getUpdatedAt())
                .build();
        
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));
        when(certificateRepository.findByDomainExcludeId("updated.example.com", 1L))
                .thenReturn(Optional.empty());
        when(serviceConverter.mergeUpdateDto(testCertificate, testUpdateDto))
                .thenReturn(updatedCertificate);
        when(certificateRepository.save(updatedCertificate)).thenReturn(updatedCertificate);
        when(serviceConverter.toDto(updatedCertificate)).thenReturn(updatedDto);
        
        // 执行
        CertificateDto result = certificateService.updateCertificate(1L, testUpdateDto);
        
        // 验证
        assertNotNull(result);
        assertEquals("更新后的证书", result.getName());
        assertEquals("updated.example.com", result.getDomain());
        
        verify(certificateRepository).findById(1L);
        verify(certificateRepository).findByDomainExcludeId("updated.example.com", 1L);
        verify(serviceConverter).mergeUpdateDto(testCertificate, testUpdateDto);
        verify(certificateRepository).save(updatedCertificate);
        verify(serviceConverter).toDto(updatedCertificate);
    }
    
    @Test
    void updateCertificate_NotFound() {
        // 准备
        when(certificateRepository.findById(999L)).thenReturn(Optional.empty());
        
        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> certificateService.updateCertificate(999L, testUpdateDto));
        
        assertEquals(ErrorCode.CERTIFICATE_NOT_FOUND.getCode(), exception.getCode());
        
        verify(certificateRepository).findById(999L);
        verify(certificateRepository, never()).save(any());
    }
    
    @Test
    void updateCertificate_DomainExists() {
        // 准备
        Certificate existingWithSameDomain = Certificate.builder()
                .id(2L)
                .domain("updated.example.com")
                .build();
        
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));
        when(certificateRepository.findByDomainExcludeId("updated.example.com", 1L))
                .thenReturn(Optional.of(existingWithSameDomain));
        
        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> certificateService.updateCertificate(1L, testUpdateDto));
        
        assertEquals(ErrorCode.CERTIFICATE_DOMAIN_EXISTS.getCode(), exception.getCode());
        
        verify(certificateRepository).findById(1L);
        verify(certificateRepository).findByDomainExcludeId("updated.example.com", 1L);
        verify(certificateRepository, never()).save(any());
    }
    
    @Test
    void deleteCertificate_Success() {
        // 准备
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(testCertificate));
        doNothing().when(certificateRepository).deleteById(1L);
        
        // 执行
        assertDoesNotThrow(() -> certificateService.deleteCertificate(1L));
        
        // 验证
        verify(certificateRepository).findById(1L);
        verify(certificateRepository).deleteById(1L);
    }
    
    @Test
    void deleteCertificate_NotFound() {
        // 准备
        when(certificateRepository.findById(999L)).thenReturn(Optional.empty());
        
        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> certificateService.deleteCertificate(999L));
        
        assertEquals(ErrorCode.CERTIFICATE_NOT_FOUND.getCode(), exception.getCode());
        
        verify(certificateRepository).findById(999L);
        verify(certificateRepository, never()).deleteById(any());
    }
    
    @Test
    void getCertificateList_Success() {
        // 准备
        Page<Certificate> certificatePage = new Page<>(1, 10);
        certificatePage.setTotal(1);
        certificatePage.setRecords(Arrays.asList(testCertificate));
        
        when(certificateRepository.findPage(1, 10, "createdAt", "DESC"))
                .thenReturn(certificatePage);
        when(serviceConverter.toDto(testCertificate)).thenReturn(testCertificateDto);
        
        // 执行
        Page<CertificateDto> result = certificateService.getCertificateList(1, 10, "createdAt", "DESC");
        
        // 验证
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("测试证书", result.getRecords().get(0).getName());
        
        verify(certificateRepository).findPage(1, 10, "createdAt", "DESC");
        verify(serviceConverter).toDto(testCertificate);
    }
    
    @Test
    void getCertificateListWithFilter_Success() {
        // 准备
        Page<Certificate> certificatePage = new Page<>(1, 10);
        certificatePage.setTotal(1);
        certificatePage.setRecords(Arrays.asList(testCertificate));
        
        when(certificateRepository.findPageWithFilter(1, 10, "createdAt", "DESC", 
                "NORMAL", "example", "Let's Encrypt"))
                .thenReturn(certificatePage);
        when(serviceConverter.toDto(testCertificate)).thenReturn(testCertificateDto);
        
        // 执行
        Page<CertificateDto> result = certificateService.getCertificateListWithFilter(
                1, 10, "createdAt", "DESC", "NORMAL", "example", "Let's Encrypt");
        
        // 验证
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        
        verify(certificateRepository).findPageWithFilter(1, 10, "createdAt", "DESC", 
                "NORMAL", "example", "Let's Encrypt");
        verify(serviceConverter).toDto(testCertificate);
    }
    
    @Test
    void validatePageParameters_InvalidPageNum() {
        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> certificateService.getCertificateList(0, 10, "createdAt", "DESC"));
        
        assertEquals(ErrorCode.PAGE_PARAMETER_INVALID.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("页码必须大于0"));
    }
    
    @Test
    void validatePageParameters_InvalidPageSize() {
        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> certificateService.getCertificateList(1, 101, "createdAt", "DESC"));
        
        assertEquals(ErrorCode.PAGE_PARAMETER_INVALID.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("每页大小必须在1-100之间"));
    }
    
    @Test
    void isDomainExists_True() {
        // 准备
        when(certificateRepository.findByDomain("example.com")).thenReturn(Optional.of(testCertificate));
        
        // 执行
        boolean result = certificateService.isDomainExists("example.com");
        
        // 验证
        assertTrue(result);
        verify(certificateRepository).findByDomain("example.com");
    }
    
    @Test
    void isDomainExists_False() {
        // 准备
        when(certificateRepository.findByDomain("nonexistent.com")).thenReturn(Optional.empty());
        
        // 执行
        boolean result = certificateService.isDomainExists("nonexistent.com");
        
        // 验证
        assertFalse(result);
        verify(certificateRepository).findByDomain("nonexistent.com");
    }
    
    @Test
    void isDomainExistsExcludeId_True() {
        // 准备
        Certificate otherCertificate = Certificate.builder().id(2L).domain("example.com").build();
        when(certificateRepository.findByDomainExcludeId("example.com", 1L))
                .thenReturn(Optional.of(otherCertificate));
        
        // 执行
        boolean result = certificateService.isDomainExists("example.com", 1L);
        
        // 验证
        assertTrue(result);
        verify(certificateRepository).findByDomainExcludeId("example.com", 1L);
    }
    
    @Test
    void isDomainExistsExcludeId_False() {
        // 准备
        when(certificateRepository.findByDomainExcludeId("example.com", 1L))
                .thenReturn(Optional.empty());
        
        // 执行
        boolean result = certificateService.isDomainExists("example.com", 1L);
        
        // 验证
        assertFalse(result);
        verify(certificateRepository).findByDomainExcludeId("example.com", 1L);
    }
}