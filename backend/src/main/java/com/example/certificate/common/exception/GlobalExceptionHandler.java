package com.example.certificate.common.exception;

import com.example.certificate.common.response.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 使用 @RestControllerAdvice 注解捕获全局异常，提供统一的异常处理和响应格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage(), e);
        ApiError apiError = ApiError.of(e.getCode(), e.getMessage());
        return ResponseEntity.status(getHttpStatus(e.getCode())).body(apiError);
    }
    
    /**
     * 处理参数验证异常 (@Valid 注解)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.warn("参数验证异常: {}", e.getMessage());
        
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        ApiError apiError = ApiError.of(ErrorCode.VALIDATION_FAILED.getCode(), 
                "参数验证失败: " + errorMessage);
        return ResponseEntity.badRequest().body(apiError);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(BindException e) {
        logger.warn("绑定异常: {}", e.getMessage());
        
        String errorMessage = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        ApiError apiError = ApiError.of(ErrorCode.VALIDATION_FAILED.getCode(), 
                "参数绑定失败: " + errorMessage);
        return ResponseEntity.badRequest().body(apiError);
    }
    
    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException e) {
        logger.warn("约束验证异常: {}", e.getMessage());
        
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        ApiError apiError = ApiError.of(ErrorCode.VALIDATION_FAILED.getCode(), 
                "约束验证失败: " + errorMessage);
        return ResponseEntity.badRequest().body(apiError);
    }
    
    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        logger.warn("参数类型不匹配异常: {}", e.getMessage());
        
        String errorMessage = String.format("参数 '%s' 的值 '%s' 无法转换为类型 %s", 
                e.getName(), e.getValue(), e.getRequiredType().getSimpleName());
        
        ApiError apiError = ApiError.of(ErrorCode.PARAMETER_INVALID.getCode(), errorMessage);
        return ResponseEntity.badRequest().body(apiError);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("非法参数异常: {}", e.getMessage());
        ApiError apiError = ApiError.of(ErrorCode.PARAMETER_INVALID.getCode(), 
                "参数错误: " + e.getMessage());
        return ResponseEntity.badRequest().body(apiError);
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常: {}", e.getMessage(), e);
        ApiError apiError = ApiError.of(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), 
                "系统内部错误", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception e) {
        logger.error("未知异常: {}", e.getMessage(), e);
        ApiError apiError = ApiError.of(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), 
                "系统异常，请联系管理员", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
    
    /**
     * 根据错误代码获取HTTP状态码
     */
    private HttpStatus getHttpStatus(int errorCode) {
        if (errorCode >= 400 && errorCode < 500) {
            return HttpStatus.valueOf(errorCode);
        } else if (errorCode >= 10000) {
            // 业务错误代码，返回400
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}