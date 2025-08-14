package com.example.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.certificate.common.response.ApiResponse;
import com.example.certificate.common.response.PageResult;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.dto.MonitoringLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/monitoring-logs")
@RequiredArgsConstructor
public class MonitoringLogController {
    
    private final MonitoringLogService monitoringLogService;
    
    /**
     * 分页查询监控日志
     */
    @GetMapping
    public ApiResponse<PageResult<MonitoringLogDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long certificateId) {
        
        log.debug("查询监控日志: page={}, size={}, certificateId={}", page, size, certificateId);
        IPage<MonitoringLogDto> result = monitoringLogService.findPage(page, size, certificateId);
        return ApiResponse.success(PageResult.of(result));
    }
}