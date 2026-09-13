package com.flandreyu.vo;

import lombok.Data;

/**
 * 日历打点：某一天是否有日记 / 是否已打卡（首页侧边栏日历用）
 */
@Data
public class CalendarMarkVO {

    /** 日期 yyyy-MM-dd */
    private String date;
    /** 当天有日记 */
    private boolean diary;
    /** 当天已打卡 */
    private boolean checkin;
}
