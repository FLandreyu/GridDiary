package com.flandreyu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flandreyu.common.Result;
import com.flandreyu.dto.MessageSendRequest;
import com.flandreyu.service.MessageService;
import com.flandreyu.util.UserSessionUtil;
import com.flandreyu.vo.ConversationVO;
import com.flandreyu.vo.MessageDetailVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 私信接口（需求 8，均需登录）
 */
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** 会话列表 */
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations(HttpSession session) {
        return Result.ok(messageService.conversations(UserSessionUtil.requireId(session)));
    }

    /** 我的未读私信总数（导航红点） */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(HttpSession session) {
        return Result.ok(messageService.unreadCount(UserSessionUtil.requireId(session)));
    }

    /** 与某人的聊天记录（打开即标记已读） */
    @GetMapping("/with/{peerId}")
    public Result<MessageDetailVO> conversation(@PathVariable Long peerId,
            @RequestParam(defaultValue = "100") int limit,
            HttpSession session) {
        int safeLimit = Math.min(Math.max(1, limit), 500);
        return Result.ok(messageService.detail(UserSessionUtil.requireId(session), peerId, safeLimit));
    }

    /** 发送私信 */
    @PostMapping
    public Result<Long> send(@Valid @RequestBody MessageSendRequest req, HttpSession session) {
        return Result.ok(messageService.send(UserSessionUtil.requireId(session), req));
    }
}
