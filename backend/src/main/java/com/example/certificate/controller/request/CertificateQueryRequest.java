package com.example.certificate.controller.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 证书查询请求类
 * 继承基础分页请求类，添加证书特定的筛选参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CertificateQueryRequest extends PageRequest {
    
    /**
     * 证书状态筛选
     * 支持的状态: NORMAL, EXPIRING_SOON, EXPIRED
     */
    @Pattern(regexp = "^(NORMAL|EXPIRING_SOON|EXPIRED)$", 
             message = "证书状态必须是 NORMAL、EXPIRING_SOON 或 EXPIRED")
    private String status;
    
    /**
     * 域名筛选（模糊搜索）
     */
    @Size(max = 255, message = "域名长度不能超过255个字符")
    private String domain;
    
    /**
     * 颁发机构筛选（模糊搜索）
     */
    @Size(max = 100, message = "颁发机构长度不能超过100个字符")
    private String issuer;
    
    /**
     * 到期日期范围筛选 - 开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expiryDateFrom;
    
    /**
     * 到期日期范围筛选 - 结束日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expiryDateTo;
    
    /**
     * 验证日期范围是否有效
     */
    public boolean isValidDateRange() {
        if (expiryDateFrom == null || expiryDateTo == null) {
            return true; // 空值不验证
        }
        return !expiryDateFrom.after(expiryDateTo);
    }
    
    /**
     * 检查是否有筛选条件
     */
    public boolean hasFilters() {
        return status != null || 
               domain != null || 
               issuer != null || 
               expiryDateFrom != null || 
               expiryDateTo != null;
    }
}