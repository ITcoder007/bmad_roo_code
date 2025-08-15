package com.example.certificate.controller.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 基础分页请求类
 * 定义通用的分页参数和验证规则
 */
@Data
public class PageRequest {
    
    /**
     * 页码，从1开始
     */
    @Min(value = 1, message = "页码必须大于等于1")
    private int pageNum = 1;
    
    /**
     * 每页大小，默认10条，最大100条
     */
    @Min(value = 1, message = "每页大小必须大于等于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private int pageSize = 10;
    
    /**
     * 排序字段
     * 支持的字段: name, domain, expiryDate, createdAt
     */
    private String sortBy = "createdAt";
    
    /**
     * 排序方向
     * 支持: ASC（升序）、DESC（降序）
     */
    private String sortOrder = "DESC";
    
    /**
     * 验证排序字段是否有效
     */
    public boolean isValidSortField() {
        if (sortBy == null) {
            return false;
        }
        return "name".equals(sortBy) || 
               "domain".equals(sortBy) || 
               "expiryDate".equals(sortBy) || 
               "createdAt".equals(sortBy);
    }
    
    /**
     * 验证排序方向是否有效
     */
    public boolean isValidSortOrder() {
        if (sortOrder == null) {
            return false;
        }
        return "ASC".equalsIgnoreCase(sortOrder) || 
               "DESC".equalsIgnoreCase(sortOrder);
    }
    
    /**
     * 获取标准化的排序方向
     */
    public String getNormalizedSortOrder() {
        if (sortOrder == null) {
            return "DESC";
        }
        return sortOrder.toUpperCase();
    }
}