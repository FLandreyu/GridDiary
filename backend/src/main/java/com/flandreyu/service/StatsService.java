package com.flandreyu.service;

import java.util.List;

import com.flandreyu.vo.CalendarMarkVO;
import com.flandreyu.vo.HeatmapVO;
import com.flandreyu.vo.SiteStatsVO;
import com.flandreyu.vo.TagCountVO;

/**
 * 站点统计 / 日历打点（首页侧边栏）
 */
public interface StatsService {

    /** 站点统计（匿名可见） */
    SiteStatsVO siteStats();

    /** 标签云：公开日记里使用最多的标签（按日记数倒序，取前 limit 个） */
    List<TagCountVO> topTags(int limit);

    /**
     * 某用户某月的日历打点（有日记 / 已打卡）
     *
     * @param month yyyy-MM，为空取当月
     */
    List<CalendarMarkVO> calendar(long userId, String month);

    /**
     * 写作热力图：最近 days 天的每日篇数/字数 + 活跃天数/连续天数
     *
     * @param onlyPublic true 时只统计公开日记（看他人主页）
     */
    HeatmapVO heatmap(long userId, int days, boolean onlyPublic);
}
