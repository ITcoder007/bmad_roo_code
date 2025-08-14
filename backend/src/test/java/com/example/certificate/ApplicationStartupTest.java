package com.example.certificate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationStartupTest {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {
            // 此测试验证 Spring Boot 应用上下文能够成功加载
        });
    }

    @Test
    void applicationStarts() {
        // 此测试验证应用能够成功启动
        // 如果应用无法启动，测试将失败
        assertDoesNotThrow(() -> {
            CertificateManagementApplication.main(new String[]{});
        });
    }
}