package com.example.certificate.integration;

import com.example.certificate.common.response.ApiResponse;
import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.infrastructure.persistence.entity.CertificateEntity;
import com.example.certificate.infrastructure.persistence.mapper.CertificateMapper;
import com.example.certificate.service.dto.CertificateDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 证书状态集成测试
 * 验证证书状态计算在 API 层的表现
 * 
 * @author Auto Generated
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("证书状态集成测试")
class CertificateStatusIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private CertificateMapper certificateMapper;
    
    @Autowired
    private CertificateStatusConfig certificateStatusConfig;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Date now;
    
    @BeforeEach
    void setUp() {
        now = new Date();
        // 清空测试数据
        certificateMapper.delete(null);
    }
    
    @Test
    @DisplayName("测试证书创建时自动计算状态")
    void createCertificate_shouldAutoCalculateStatus() throws Exception {
        // Given: 创建即将过期的证书数据
        String certificateJson = String.format("{\n" +
                "    \"name\": \"集成测试证书\",\n" +
                "    \"domain\": \"integration-test.example.com\",\n" +
                "    \"issuer\": \"Test CA\",\n" +
                "    \"issueDate\": \"%s\",\n" +
                "    \"expiryDate\": \"%s\",\n" +
                "    \"certificateType\": \"SSL\"\n" +
                "}",
                formatDate(getDateDaysFromNow(-30)),
                formatDate(getDateDaysFromNow(15)) // 15天后过期
        );
        
        // When: 创建证书
        MvcResult result = mockMvc.perform(post("/v1/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(certificateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("EXPIRING_SOON"))
                .andExpect(jsonPath("$.data.daysUntilExpiry").exists())
                .andReturn();
        
        // Then: 验证响应中包含正确的状态信息
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<CertificateDto> apiResponse = objectMapper.readValue(
                responseContent, new TypeReference<ApiResponse<CertificateDto>>() {});
        
        CertificateDto certificate = apiResponse.getData();
        assertThat(certificate.getStatus()).isEqualTo("EXPIRING_SOON");
        assertThat(certificate.getDaysUntilExpiry()).isBetween(14L, 15L);
    }
    
    @Test
    @DisplayName("测试证书列表API返回状态信息")
    void getCertificateList_shouldReturnStatusInformation() throws Exception {
        // Given: 创建不同状态的证书
        createTestCertificate("正常证书", "normal.example.com", 60); // 正常
        createTestCertificate("即将过期证书", "expiring.example.com", 15); // 即将过期
        createTestCertificate("已过期证书", "expired.example.com", -5); // 已过期
        
        // When: 获取证书列表
        MvcResult result = mockMvc.perform(get("/v1/certificates")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isNotEmpty())
                .andReturn();
        
        // Then: 验证每个证书都包含状态信息
        String responseContent = result.getResponse().getContentAsString();
        // 解析响应并验证状态
        assertThat(responseContent).contains("NORMAL");
        assertThat(responseContent).contains("EXPIRING_SOON");
        assertThat(responseContent).contains("EXPIRED");
        assertThat(responseContent).contains("daysUntilExpiry");
    }
    
    @Test
    @DisplayName("测试基于状态的筛选API功能")
    void getCertificateListWithStatusFilter_shouldFilterCorrectly() throws Exception {
        // Given: 创建不同状态的证书
        createTestCertificate("正常证书1", "normal1.example.com", 60);
        createTestCertificate("正常证书2", "normal2.example.com", 45);
        createTestCertificate("即将过期证书", "expiring.example.com", 15);
        createTestCertificate("已过期证书", "expired.example.com", -5);
        
        // When: 只筛选即将过期的证书
        mockMvc.perform(get("/v1/certificates")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "EXPIRING_SOON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[?(@.status == 'EXPIRING_SOON')]").exists());
        
        // When: 只筛选已过期的证书
        mockMvc.perform(get("/v1/certificates")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "EXPIRED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[?(@.status == 'EXPIRED')]").exists());
    }
    
    @Test
    @DisplayName("测试多状态筛选功能")
    void getCertificateListWithMultipleStatusFilter() throws Exception {
        // Given: 创建不同状态的证书
        createTestCertificate("正常证书", "normal.example.com", 60);
        createTestCertificate("即将过期证书", "expiring.example.com", 15);
        createTestCertificate("已过期证书", "expired.example.com", -5);
        
        // When: 筛选即将过期的证书
        MvcResult result1 = mockMvc.perform(get("/v1/certificates/search")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "EXPIRING_SOON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andReturn();
        
        // When: 筛选已过期的证书
        MvcResult result2 = mockMvc.perform(get("/v1/certificates/search")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "EXPIRED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andReturn();
        
        // Then: 验证筛选结果
        String expiringSoonContent = result1.getResponse().getContentAsString();
        String expiredContent = result2.getResponse().getContentAsString();
        
        assertThat(expiringSoonContent).contains("EXPIRING_SOON");
        assertThat(expiringSoonContent).doesNotContain("\"status\":\"NORMAL\"");
        assertThat(expiredContent).contains("EXPIRED");
        assertThat(expiredContent).doesNotContain("\"status\":\"NORMAL\"");
    }
    
    @Test
    @DisplayName("测试状态计算的一致性")
    void statusCalculationConsistency() throws Exception {
        // Given: 创建证书
        Long certificateId = createTestCertificate("一致性测试证书", "consistency.example.com", 20);
        
        // When: 多次获取同一证书
        MvcResult result1 = mockMvc.perform(get("/v1/certificates/" + certificateId))
                .andExpect(status().isOk())
                .andReturn();
        
        MvcResult result2 = mockMvc.perform(get("/v1/certificates/" + certificateId))
                .andExpect(status().isOk())
                .andReturn();
        
        // Then: 状态应该一致
        String response1 = result1.getResponse().getContentAsString();
        String response2 = result2.getResponse().getContentAsString();
        
        ApiResponse<CertificateDto> apiResponse1 = objectMapper.readValue(
                response1, new TypeReference<ApiResponse<CertificateDto>>() {});
        ApiResponse<CertificateDto> apiResponse2 = objectMapper.readValue(
                response2, new TypeReference<ApiResponse<CertificateDto>>() {});
        
        assertThat(apiResponse1.getData().getStatus()).isEqualTo(apiResponse2.getData().getStatus());
        assertThat(apiResponse1.getData().getDaysUntilExpiry()).isEqualTo(apiResponse2.getData().getDaysUntilExpiry());
    }
    
    @Test
    @DisplayName("测试证书更新时状态重新计算")
    void updateCertificate_shouldRecalculateStatus() throws Exception {
        // Given: 创建正常状态的证书
        Long certificateId = createTestCertificate("更新测试证书", "update-test.example.com", 60);
        
        // When: 更新证书的到期日期为即将过期
        String updateJson = String.format("{\n" +
                "    \"expiryDate\": \"%s\"\n" +
                "}",
                formatDate(getDateDaysFromNow(10))); // 10天后过期
        
        mockMvc.perform(put("/v1/certificates/" + certificateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("EXPIRING_SOON"))
                .andExpect(jsonPath("$.data.daysUntilExpiry").value(10));
    }
    
    @Test
    @DisplayName("测试日期范围筛选与状态筛选的组合")
    void combinedDateRangeAndStatusFilter() throws Exception {
        // Given: 创建不同到期时间的证书
        createTestCertificate("近期过期证书", "near-expiry.example.com", 10);
        createTestCertificate("远期过期证书", "far-expiry.example.com", 100);
        createTestCertificate("已过期证书", "expired.example.com", -10);
        
        // When: 筛选30天内到期且即将过期的证书
        Date dateFrom = new Date();
        Date dateTo = getDateDaysFromNow(30);
        
        mockMvc.perform(get("/v1/certificates")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "EXPIRING_SOON")
                        .param("expiryDateFrom", formatDate(dateFrom))
                        .param("expiryDateTo", formatDate(dateTo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    @DisplayName("验证配置参数对状态计算的影响")
    void configurationParametersImpactOnStatusCalculation() throws Exception {
        // Given: 获取当前配置的阈值
        int thresholdDays = certificateStatusConfig.getExpiringSoonDays();
        
        // 创建刚好在阈值边界的证书
        createTestCertificate("边界测试证书", "boundary.example.com", thresholdDays);
        
        // When: 获取证书列表
        MvcResult result = mockMvc.perform(get("/v1/certificates")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andReturn();
        
        // Then: 状态应该根据配置正确计算
        String responseContent = result.getResponse().getContentAsString();
        // 在阈值天数时应该是 EXPIRING_SOON
        assertThat(responseContent).contains("EXPIRING_SOON");
    }
    
    /**
     * 创建测试用证书
     */
    private Long createTestCertificate(String name, String domain, int daysUntilExpiry) {
        Date expiryDate = getDateDaysFromNow(daysUntilExpiry);
        
        // 使用 Certificate 领域模型计算正确的状态
        Certificate certificate = Certificate.builder()
                .name(name)
                .domain(domain)
                .issuer("Test CA")
                .issueDate(getDateDaysFromNow(-365))
                .expiryDate(expiryDate)
                .certificateType("SSL")
                .build();
        
        // 计算状态
        CertificateStatus calculatedStatus = certificate.calculateStatus();
        
        CertificateEntity entity = CertificateEntity.builder()
                .name(name)
                .domain(domain)
                .issuer("Test CA")
                .issueDate(getDateDaysFromNow(-365))
                .expiryDate(expiryDate)
                .certificateType("SSL")
                .status(calculatedStatus.name()) // 使用计算后的正确状态
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        certificateMapper.insert(entity);
        return entity.getId();
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
    
    /**
     * 格式化日期为字符串
     */
    private String formatDate(Date date) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(date);
    }
}