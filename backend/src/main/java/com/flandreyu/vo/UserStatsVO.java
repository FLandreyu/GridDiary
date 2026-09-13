package com.flandreyu.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户的作品统计（个人主页 / 详情页「关于作者」卡片）
 * 只统计该用户的公开日记与其收到的评论。
 */
@Data
public class UserStatsVO {

    /** 公开日记数 */
    private long postCount;
    /** 公开日记累计获赞 */
    private long likeCount;
    /** 收到的评论数（TA 的日记下所有评论） */
    private long commentCount;
    /** 注册天数（含当天） */
    private long joinDays;
    /** 注册时间 */
    private LocalDateTime createdAt;
}
