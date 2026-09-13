package com.flandreyu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flandreyu.common.PublicApi;
import com.flandreyu.common.Result;
import com.flandreyu.service.StatsService;
import com.flandreyu.util.UserSessionUtil;
import com.flandreyu.vo.CalendarMarkVO;
import com.flandreyu.vo.SiteStatsVO;
import com.flandreyu.vo.TagCountVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 站点统计 / 日历打点（首页右侧边栏）
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** 站点统计：日记数/用户数/总字数/获赞/评论/运行天数（匿名可见） */
    @GetMapping
    @PublicApi
    public Result<SiteStatsVO> site() {
        return Result.ok(statsService.siteStats());
    }

    /** 标签云：公开日记使用最多的标签（匿名可见），limit 默认 20 */
    @GetMapping("/tags")
    @PublicApi
    public Result<List<TagCountVO>> tags(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(statsService.topTags(Math.min(Math.max(1, limit), 50)));
    }

    /** 当前用户某月日历打点（需登录），month 形如 2025-09，为空取当月 */
    @GetMapping("/calendar")
    public Result<List<CalendarMarkVO>> calendar(@RequestParam(required = false) String month,
            HttpSession session) {
        return Result.ok(statsService.calendar(UserSessionUtil.requireId(session), month));
    }
}
