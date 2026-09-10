package com.flandreyu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flandreyu.common.Result;
import com.flandreyu.service.CheckinService;
import com.flandreyu.util.UserSessionUtil;
import com.flandreyu.vo.CheckinVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 每日打卡接口（需求新增：首页打卡 + 今日运势）
 * 两个接口都需要登录（未标 @PublicApi，由 LoginInterceptor 拦截）
 */
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    /** 今日打卡状态 + 今日运势（已打卡时返回完整运势） */
    @GetMapping("/today")
    public Result<CheckinVO> today(HttpSession session) {
        return Result.ok(checkinService.today(UserSessionUtil.requireId(session)));
    }

    /** 打卡：生成今日运势并保存（同一天重复调用幂等） */
    @PostMapping
    public Result<CheckinVO> checkin(HttpSession session) {
        return Result.ok(checkinService.checkin(UserSessionUtil.requireId(session)));
    }
}
