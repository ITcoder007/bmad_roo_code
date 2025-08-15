package com.example.certificate.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.certificate.infrastructure.persistence.mapper")
public class MyBatisConfig {
}