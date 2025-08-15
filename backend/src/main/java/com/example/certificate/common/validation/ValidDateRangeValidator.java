package com.example.certificate.common.validation;

import org.springframework.util.ReflectionUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Date;

/**
 * 日期范围验证器实现
 * 验证颁发日期必须早于到期日期
 */
public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    
    private String issueDateField;
    private String expiryDateField;
    
    @Override
    public void initialize(ValidDateRange annotation) {
        this.issueDateField = annotation.issueDateField();
        this.expiryDateField = annotation.expiryDateField();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        try {
            Field issueDateFieldObj = ReflectionUtils.findField(value.getClass(), issueDateField);
            Field expiryDateFieldObj = ReflectionUtils.findField(value.getClass(), expiryDateField);
            
            if (issueDateFieldObj == null || expiryDateFieldObj == null) {
                return true; // 如果字段不存在，跳过验证
            }
            
            ReflectionUtils.makeAccessible(issueDateFieldObj);
            ReflectionUtils.makeAccessible(expiryDateFieldObj);
            
            Date issueDate = (Date) ReflectionUtils.getField(issueDateFieldObj, value);
            Date expiryDate = (Date) ReflectionUtils.getField(expiryDateFieldObj, value);
            
            // 如果任一日期为空，跳过验证
            if (issueDate == null || expiryDate == null) {
                return true;
            }
            
            // 验证颁发日期必须早于到期日期
            return issueDate.before(expiryDate);
            
        } catch (Exception e) {
            // 反射异常时，跳过验证
            return true;
        }
    }
}