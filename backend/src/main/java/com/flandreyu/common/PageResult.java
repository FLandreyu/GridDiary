package com.flandreyu.common;

import java.util.List;

import lombok.Data;

/**
 * 通用分页返回体
 */
@Data
public class PageResult<T> {

    /** 总记录数 */
    private long total;
    /** 当前页数据 */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
