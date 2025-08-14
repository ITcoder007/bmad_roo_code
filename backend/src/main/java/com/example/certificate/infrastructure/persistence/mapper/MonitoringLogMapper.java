package com.example.certificate.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.certificate.domain.model.MonitoringLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MonitoringLogMapper extends BaseMapper<MonitoringLog> {
}