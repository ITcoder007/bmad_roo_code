package com.example.certificate.service;

import com.example.certificate.domain.model.Certificate;

public interface AlertService {
    
    /**
     * 发送证书预警
     */
    void sendAlert(Certificate certificate);
    
    /**
     * 检查是否需要发送预警
     */
    boolean shouldSendAlert(long daysUntilExpiry);
}