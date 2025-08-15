package com.example.certificate.domain.repository;

import com.example.certificate.domain.model.MonitoringLog;
import com.example.certificate.domain.model.LogType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MonitoringLogRepository {
    
    Optional<MonitoringLog> findById(Long id);
    
    List<MonitoringLog> findAll();
    
    MonitoringLog save(MonitoringLog monitoringLog);
    
    void deleteById(Long id);
    
    List<MonitoringLog> findByCertificateId(Long certificateId);
    
    List<MonitoringLog> findByLogType(LogType logType);
    
    List<MonitoringLog> findByLogTimeBetween(Date startTime, Date endTime);
}