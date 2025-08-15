package com.example.certificate.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 日期范围验证注解
 * 验证颁发日期必须早于到期日期
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
@Documented
public @interface ValidDateRange {
    
    String message() default "颁发日期必须早于到期日期";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 颁发日期字段名
     */
    String issueDateField() default "issueDate";
    
    /**
     * 到期日期字段名
     */
    String expiryDateField() default "expiryDate";
}