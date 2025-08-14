package com.example.certificate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CertificateManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertificateManagementApplication.class, args);
    }
}