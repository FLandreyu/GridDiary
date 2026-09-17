package com.flandreyu.vo;

import lombok.Data;

/**
 * 写作热力图的一天（某天写了几篇、共多少字）
 */
@Data
public class HeatmapPointVO {

    /** 日期 yyyy-MM-dd */
    private String date;
    /** 当天日记篇数 */
    private int count;
    /** 当天总字数 */
    private int words;
}
