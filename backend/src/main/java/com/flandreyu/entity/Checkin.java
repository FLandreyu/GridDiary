package com.flandreyu.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 每日打卡实体，对应表 checkin（user_id + checkin_date 唯一，一天只能打卡一次）
 * 打卡时同时生成并保存当天运势，之后查看结果保持一致。
 */
@Data
public class Checkin {
    private Long id;
    /** 打卡用户ID */
    private Long userId;
    /** 打卡日期 */
    private LocalDate checkinDate;
    /** 运势等级：大吉 / 中吉 / 小吉 / 平 */
    private String fortuneLevel;
    /** 运势星级 1~5 */
    private Integer fortuneStars;
    /** 幸运色名称 */
    private String luckyColor;
    /** 幸运色色值（前端展示色块） */
    private String luckyColorHex;
    /** 幸运数字 1~9 */
    private Integer luckyNumber;
    /** 宜：多个事项以英文逗号分隔 */
    private String suit;
    /** 忌：多个事项以英文逗号分隔 */
    private String avoid;
    /** 运势签文（一句话） */
    private String fortuneText;
    /** 打卡时间 */
    private LocalDateTime createdAt;
}
