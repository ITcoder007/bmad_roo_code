package com.example.certificate.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.certificate.domain.model.LogType;
import com.example.certificate.domain.model.MonitoringLog;
import com.example.certificate.domain.repository.MonitoringLogRepository;
import com.example.certificate.infrastructure.persistence.entity.MonitoringLogEntity;
import com.example.certificate.infrastructure.persistence.mapper.MonitoringLogMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MonitoringLogRepositoryImpl implements MonitoringLogRepository {
    
    @Resource
    private MonitoringLogMapper monitoringLogMapper;
    
    @Override
    public Optional<MonitoringLog> findById(Long id) {
        MonitoringLogEntity entity = monitoringLogMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }
    
    @Override
    public List<MonitoringLog> findAll() {
        List<MonitoringLogEntity> entities = monitoringLogMapper.selectList(null);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public MonitoringLog save(MonitoringLog monitoringLog) {
        MonitoringLogEntity entity = toEntity(monitoringLog);
        if (monitoringLog.getId() == null) {
            monitoringLogMapper.insert(entity);
        } else {
            monitoringLogMapper.updateById(entity);
        }
        return toDomain(entity);
    }
    
    @Override
    public void deleteById(Long id) {
        monitoringLogMapper.deleteById(id);
    }
    
    @Override
    public List<MonitoringLog> findByCertificateId(Long certificateId) {
        QueryWrapper<MonitoringLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("certificate_id", certificateId);
        List<MonitoringLogEntity> entities = monitoringLogMapper.selectList(wrapper);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<MonitoringLog> findByLogType(LogType logType) {
        QueryWrapper<MonitoringLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("log_type", logType.name());
        List<MonitoringLogEntity> entities = monitoringLogMapper.selectList(wrapper);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<MonitoringLog> findByLogTimeBetween(Date startTime, Date endTime) {
        QueryWrapper<MonitoringLogEntity> wrapper = new QueryWrapper<>();
        wrapper.between("log_time", startTime, endTime);
        List<MonitoringLogEntity> entities = monitoringLogMapper.selectList(wrapper);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    private MonitoringLog toDomain(MonitoringLogEntity entity) {
        MonitoringLog log = new MonitoringLog();
        BeanUtils.copyProperties(entity, log);
        log.setLogType(LogType.valueOf(entity.getLogType()));
        return log;
    }
    
    private MonitoringLogEntity toEntity(MonitoringLog log) {
        MonitoringLogEntity entity = new MonitoringLogEntity();
        BeanUtils.copyProperties(log, entity);
        entity.setLogType(log.getLogType().name());
        return entity;
    }
}