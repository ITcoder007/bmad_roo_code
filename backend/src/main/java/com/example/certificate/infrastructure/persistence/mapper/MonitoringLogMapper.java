package com.example.certificate.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.certificate.infrastructure.persistence.entity.MonitoringLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface MonitoringLogMapper extends BaseMapper<MonitoringLogEntity> {
    
    List<MonitoringLogEntity> findRecentLogs(@Param("certificateId") Long certificateId, 
                                            @Param("limit") int limit);
    
    int countByTypeAndDateRange(@Param("logType") String logType,
                                @Param("startDate") Date startDate,
                                @Param("endDate") Date endDate);
}