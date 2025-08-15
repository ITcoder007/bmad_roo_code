package com.example.certificate.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.certificate.infrastructure.persistence.entity.CertificateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface CertificateMapper extends BaseMapper<CertificateEntity> {
    
    List<CertificateEntity> findExpiringSoon(@Param("days") int days);
    
    List<CertificateEntity> findByDateRange(@Param("startDate") Date startDate, 
                                           @Param("endDate") Date endDate);
    
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}