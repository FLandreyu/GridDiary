package com.flandreyu.vo;

import java.util.List;

import lombok.Data;

/**
 * 写作热力图统计数据（个人主页「我的写作」用）
 * <p>
 * points 只包含「有写作的日子」，前端按 from ~ to 补全空白格。
 */
@Data
public class HeatmapVO {

    /** 统计区间开始日期（含）yyyy-MM-dd */
    private String from;
    /** 统计区间结束日期（含）yyyy-MM-dd */
    private String to;
    /** 区间内写作总篇数 */
    private int totalCount;
    /** 区间内写作总字数 */
    private int totalWords;
    /** 有写作的天数 */
    private int activeDays;
    /** 当前连续写作天数（今天或昨天有写作才算连续） */
    private int currentStreak;
    /** 区间内最长连续写作天数 */
    private int maxStreak;
    /** 有写作的日期明细 */
    private List<HeatmapPointVO> points;
}
