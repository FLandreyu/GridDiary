package com.flandreyu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flandreyu.common.PublicApi;
import com.flandreyu.common.Result;
import com.flandreyu.dto.CommentSaveRequest;
import com.flandreyu.service.CommentService;
import com.flandreyu.util.UserSessionUtil;
import com.flandreyu.vo.CommentVO;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 评论接口（需求 6）
 */
@Tag(name = "评论模块", description = "日记评论的发布与删除")
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 某日记的评论树（公开） */
    @GetMapping("/{diaryId}/comments")
    @PublicApi
    public Result<List<CommentVO>> list(@PathVariable Long diaryId) {
        return Result.ok(commentService.listByDiary(diaryId));
    }

    /** 发表评论 / 回复（需登录） */
    @PostMapping("/{diaryId}/comments")
    public Result<CommentVO> add(@PathVariable Long diaryId,
            @Valid @RequestBody CommentSaveRequest req,
            HttpSession session) {
        return Result.ok(commentService.add(diaryId, UserSessionUtil.requireId(session), req));
    }

    /** 删除评论（评论作者或日记作者） */
    @DeleteMapping("/{diaryId}/comments/{commentId}")
    public Result<Void> delete(@PathVariable Long diaryId,
            @PathVariable Long commentId,
            HttpSession session) {
        commentService.delete(diaryId, commentId, UserSessionUtil.requireId(session));
        return Result.ok();
    }
}
