package com.example.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.certificate.domain.enums.LogType;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.MonitoringLog;
import com.example.certificate.infrastructure.persistence.mapper.CertificateMapper;
import com.example.certificate.infrastructure.persistence.mapper.MonitoringLogMapper;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.dto.MonitoringLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringLogServiceImpl implements MonitoringLogService {
    
    private final MonitoringLogMapper monitoringLogMapper;
    private final CertificateMapper certificateMapper;
    
    @Override
    public IPage<MonitoringLogDto> findPage(int page, int size, Long certificateId) {
        Page<MonitoringLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MonitoringLog> wrapper = new LambdaQueryWrapper<>();
        
        if (certificateId != null) {
            wrapper.eq(MonitoringLog::getCertificateId, certificateId);
        }
        
        wrapper.orderByDesc(MonitoringLog::getLogTime);
        
        IPage<MonitoringLog> result = monitoringLogMapper.selectPage(pageParam, wrapper);
        
        return result.convert(this::toDto);
    }
    
    @Override
    public void logCertificateCreated(Certificate certificate) {
        String message = String.format("证书 [%s] 已创建，域名: %s，到期日期: %s", 
            certificate.getName(), certificate.getDomain(), certificate.getExpiryDate());
        
        MonitoringLog log = MonitoringLog.createLog(
            certificate.getId(), 
            LogType.MONITORING, 
            message,
            (int) certificate.getDaysUntilExpiry()
        );
        
        monitoringLogMapper.insert(log);
        log.info(message);
    }
    
    @Override
    public void logCertificateUpdated(Certificate certificate) {
        String message = String.format("证书 [%s] 已更新，状态: %s", 
            certificate.getName(), certificate.getStatus().getDescription());
        
        MonitoringLog log = MonitoringLog.createLog(
            certificate.getId(),
            LogType.MONITORING,
            message,
            (int) certificate.getDaysUntilExpiry()
        );
        
        monitoringLogMapper.insert(log);
        log.info(message);
    }
    
    @Override
    public void logCertificateDeleted(Certificate certificate) {
        String message = String.format("证书 [%s] 已删除", certificate.getName());
        
        MonitoringLog log = MonitoringLog.createLog(
            certificate.getId(),
            LogType.MONITORING,
            message,
            null
        );
        
        monitoringLogMapper.insert(log);
        log.info(message);
    }
    
    @Override
    public void logCertificateMonitoring(Certificate certificate) {
        String message = String.format("证书 [%s] 监控检查，状态: %s，剩余 %d 天到期", 
            certificate.getName(), 
            certificate.getStatus().getDescription(),
            certificate.getDaysUntilExpiry()
        );
        
        MonitoringLog log = MonitoringLog.createLog(
            certificate.getId(),
            LogType.MONITORING,
            message,
            (int) certificate.getDaysUntilExpiry()
        );
        
        monitoringLogMapper.insert(log);
        log.info(message);
    }
    
    @Override
    public void logCertificateAlert(Certificate certificate, String alertType) {
        LogType logType = "EMAIL".equals(alertType) ? LogType.ALERT_EMAIL : LogType.ALERT_SMS;
        String message = String.format("证书 [%s] %s预警已发送，剩余 %d 天到期", 
            certificate.getName(),
            logType.getDescription(),
            certificate.getDaysUntilExpiry()
        );
        
        MonitoringLog log = MonitoringLog.createLog(
            certificate.getId(),
            logType,
            message,
            (int) certificate.getDaysUntilExpiry()
        );
        
        monitoringLogMapper.insert(log);
        log.warn(message);
    }
    
    private MonitoringLogDto toDto(MonitoringLog log) {
        MonitoringLogDto dto = new MonitoringLogDto();
        BeanUtils.copyProperties(log, dto);
        
        // 获取证书信息
        Certificate certificate = certificateMapper.selectById(log.getCertificateId());
        if (certificate != null) {
            dto.setCertificateName(certificate.getName());
            dto.setDomain(certificate.getDomain());
        }
        
        return dto;
    }
}