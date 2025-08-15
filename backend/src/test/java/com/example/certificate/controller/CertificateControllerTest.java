package com.example.certificate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.certificate.common.exception.BusinessException;
import com.example.certificate.common.exception.ErrorCode;
import com.example.certificate.service.CertificateService;
import com.example.certificate.service.dto.CertificateCreateDto;
import com.example.certificate.service.dto.CertificateDto;
import com.example.certificate.service.dto.CertificateUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 证书控制器单元测试
 * 使用 @WebMvcTest 注解配置测试环境，模拟 HTTP 请求和响应
 */
@WebMvcTest(controllers = CertificateController.class, 
           excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
class CertificateControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private CertificateService certificateService;
    
    private CertificateDto testCertificateDto;
    private CertificateCreateDto testCreateDto;
    private CertificateUpdateDto testUpdateDto;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        Date now = new Date();
        Date futureDate = new Date(now.getTime() + 86400000L * 365); // 一年后
        
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
    void createCertificate_Success() throws Exception {
        // 准备
        when(certificateService.createCertificate(any(CertificateCreateDto.class)))
                .thenReturn(testCertificateDto);
        
        // 执行和验证
        mockMvc.perform(post("/api/v1/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("证书创建成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试证书"))
                .andExpect(jsonPath("$.data.domain").value("example.com"));
        
        verify(certificateService).createCertificate(any(CertificateCreateDto.class));
    }
    
    @Test
    void createCertificate_ValidationFailed() throws Exception {
        // 准备无效数据
        CertificateCreateDto invalidDto = CertificateCreateDto.builder()
                .name("") // 空名称，应该验证失败
                .domain("invalid-domain") // 无效域名格式
                .build();
        
        // 执行和验证
        mockMvc.perform(post("/api/v1/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
        
        verify(certificateService, never()).createCertificate(any());
    }
    
    @Test
    void createCertificate_BusinessException() throws Exception {
        // 准备
        when(certificateService.createCertificate(any(CertificateCreateDto.class)))
                .thenThrow(BusinessException.of(ErrorCode.CERTIFICATE_DOMAIN_EXISTS));
        
        // 执行和验证
        mockMvc.perform(post("/api/v1/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(10003));
        
        verify(certificateService).createCertificate(any(CertificateCreateDto.class));
    }
    
    @Test
    void getCertificate_Success() throws Exception {
        // 准备
        when(certificateService.getCertificateById(1L)).thenReturn(testCertificateDto);
        
        // 执行和验证
        mockMvc.perform(get("/api/v1/certificates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试证书"));
        
        verify(certificateService).getCertificateById(1L);
    }
    
    @Test
    void getCertificate_NotFound() throws Exception {
        // 准备
        when(certificateService.getCertificateById(999L))
                .thenThrow(BusinessException.of(ErrorCode.CERTIFICATE_NOT_FOUND));
        
        // 执行和验证
        mockMvc.perform(get("/api/v1/certificates/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(10001));
        
        verify(certificateService).getCertificateById(999L);
    }
    
    @Test
    void getCertificateList_Success() throws Exception {
        // 准备
        Page<CertificateDto> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(Arrays.asList(testCertificateDto));
        
        when(certificateService.getCertificateList(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);
        
        // 执行和验证
        mockMvc.perform(get("/api/v1/certificates")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("sortBy", "createdAt")
                        .param("sortOrder", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1));
        
        verify(certificateService).getCertificateList(1, 10, "createdAt", "DESC");
    }
    
    @Test
    void searchCertificates_Success() throws Exception {
        // 准备
        Page<CertificateDto> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(Arrays.asList(testCertificateDto));
        
        when(certificateService.getCertificateListWithFilter(
                eq(1), eq(10), eq("createdAt"), eq("DESC"), 
                eq("NORMAL"), eq("example"), isNull()))
                .thenReturn(page);
        
        // 执行和验证
        mockMvc.perform(get("/api/v1/certificates/search")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "NORMAL")
                        .param("domain", "example"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
        
        verify(certificateService).getCertificateListWithFilter(
                eq(1), eq(10), eq("createdAt"), eq("DESC"), 
                eq("NORMAL"), eq("example"), isNull());
    }
    
    @Test
    void updateCertificate_Success() throws Exception {
        // 准备
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
        
        when(certificateService.updateCertificate(eq(1L), any(CertificateUpdateDto.class)))
                .thenReturn(updatedDto);
        
        // 执行和验证
        mockMvc.perform(put("/api/v1/certificates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("证书更新成功"))
                .andExpect(jsonPath("$.data.name").value("更新后的证书"));
        
        verify(certificateService).updateCertificate(eq(1L), any(CertificateUpdateDto.class));
    }
    
    @Test
    void updateCertificate_NotFound() throws Exception {
        // 准备
        when(certificateService.updateCertificate(eq(999L), any(CertificateUpdateDto.class)))
                .thenThrow(BusinessException.of(ErrorCode.CERTIFICATE_NOT_FOUND));
        
        // 执行和验证
        mockMvc.perform(put("/api/v1/certificates/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(10001));
        
        verify(certificateService).updateCertificate(eq(999L), any(CertificateUpdateDto.class));
    }
    
    @Test
    void deleteCertificate_Success() throws Exception {
        // 准备
        doNothing().when(certificateService).deleteCertificate(1L);
        
        // 执行和验证
        mockMvc.perform(delete("/api/v1/certificates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("证书删除成功"));
        
        verify(certificateService).deleteCertificate(1L);
    }
    
    @Test
    void deleteCertificate_NotFound() throws Exception {
        // 准备
        doThrow(BusinessException.of(ErrorCode.CERTIFICATE_NOT_FOUND))
                .when(certificateService).deleteCertificate(999L);
        
        // 执行和验证
        mockMvc.perform(delete("/api/v1/certificates/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(10001));
        
        verify(certificateService).deleteCertificate(999L);
    }
}