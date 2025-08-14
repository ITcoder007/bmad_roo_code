package com.example.certificate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // MVP阶段：暂时禁用所有安全检查
        http
            .csrf().disable()
            .authorizeRequests()
            .anyRequest().permitAll()
            .and()
            .headers().frameOptions().disable();
    }
}