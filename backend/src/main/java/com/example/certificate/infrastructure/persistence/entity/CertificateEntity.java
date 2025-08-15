package com.example.certificate.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("certificate")
public class CertificateEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("name")
    private String name;
    
    @TableField("domain")
    private String domain;
    
    @TableField("issuer")
    private String issuer;
    
    @TableField("issue_date")
    private Date issueDate;
    
    @TableField("expiry_date")
    private Date expiryDate;
    
    @TableField("certificate_type")
    private String certificateType;
    
    @TableField("status")
    private String status;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}