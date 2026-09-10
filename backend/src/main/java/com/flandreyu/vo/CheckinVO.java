package com.flandreyu.vo;

import java.util.List;

import lombok.Data;

/**
 * 打卡状态 + 今日运势视图对象
 * checked=false 时表示今天还没打卡，此时运势字段为空，只返回连续/累计天数。
 */
@Data
public class CheckinVO {

    /** 今天是否已打卡 */
    private boolean checked;
    /** 今天日期 yyyy-MM-dd */
    private String date;
    /** 连续打卡天数 */
    private int streak;
    /** 累计打卡天数 */
    private long total;

    /** 运势等级：大吉 / 中吉 / 小吉 / 平 */
    private String level;
    /** 运势星级 1~5 */
    private int stars;
    /** 幸运色名称 */
    private String luckyColor;
    /** 幸运色色值 */
    private String luckyColorHex;
    /** 幸运数字 */
    private int luckyNumber;
    /** 宜 */
    private List<String> suit;
    /** 忌 */
    private List<String> avoid;
    /** 运势签文 */
    private String text;
}
