package com.example.certificate.common.response;

import lombok.Data;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> content;
    private long total;
    private int page;
    private int size;
    private int totalPages;
    
    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setContent(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPage((int) page.getCurrent());
        result.setSize((int) page.getSize());
        result.setTotalPages((int) page.getPages());
        return result;
    }
}