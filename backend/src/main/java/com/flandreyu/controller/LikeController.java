package com.flandreyu.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flandreyu.common.Result;
import com.flandreyu.service.LikeService;
import com.flandreyu.util.UserSessionUtil;
import com.flandreyu.vo.LikeVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 点赞接口（需求 6：点赞数实时返回）
 */
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /** 点赞（幂等），返回最新 {liked, likeCount} */
    @PostMapping("/{diaryId}/like")
    public Result<LikeVO> like(@PathVariable Long diaryId, HttpSession session) {
        return Result.ok(likeService.like(diaryId, UserSessionUtil.requireId(session)));
    }

    /** 取消点赞，返回最新 {liked, likeCount} */
    @DeleteMapping("/{diaryId}/like")
    public Result<LikeVO> unlike(@PathVariable Long diaryId, HttpSession session) {
        return Result.ok(likeService.unlike(diaryId, UserSessionUtil.requireId(session)));
    }

    /** 点赞状态查询（详情页初始化） */
    @GetMapping("/{diaryId}/like/status")
    public Result<LikeVO> status(@PathVariable Long diaryId, HttpSession session) {
        return Result.ok(likeService.status(diaryId, UserSessionUtil.requireId(session)));
    }
}
