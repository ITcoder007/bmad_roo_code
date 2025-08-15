package com.example.certificate.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置类
 * 配置分页插件和其他功能
 */
@Configuration
public class MybatisPlusConfig {
    
    /**
     * MyBatis Plus 拦截器配置
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        
        // 设置分页参数
        paginationInterceptor.setMaxLimit(500L); // 最大分页限制
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        paginationInterceptor.setOptimizeJoin(true); // 是否优化 JOIN 查询
        
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}