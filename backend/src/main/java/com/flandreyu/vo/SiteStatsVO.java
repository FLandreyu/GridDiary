package com.flandreyu.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 站点统计（首页右侧边栏「站点统计」卡片）
 * 只统计公开内容；lastActiveAt 为最近一篇公开日记的发布时间。
 */
@Data
public class SiteStatsVO {

    /** 公开日记数 */
    private long postCount;
    /** 注册用户数 */
    private long userCount;
    /** 公开日记总字数 */
    private long wordCount;
    /** 公开日记总获赞数 */
    private long likeCount;
    /** 评论总数 */
    private long commentCount;
    /** 分类数（公开日记的不重复分类） */
    private long categoryCount;
    /** 标签数（公开日记的不重复标签） */
    private long tagCount;
    /** 站点运行天数（首篇日记至今，含当天） */
    private long runDays;
    /** 最近一篇公开日记的发布时间，可能为 null */
    private LocalDateTime lastActiveAt;
}
