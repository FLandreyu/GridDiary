package com.flandreyu.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 评论展示对象：一级评论包含其回复列表 replies（两级结构）
 */
@Data
public class CommentVO {

    private Long id;
    private Long diaryId;
    /** 评论人ID */
    private Long userId;
    /** 父评论ID（一级评论为 null；回复指向一级评论ID） */
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;

    /** 评论人昵称/头像（JOIN user） */
    private String authorNickname;
    private String authorAvatar;

    /** 一级评论下的回复列表（仅一级评论有值） */
    private List<CommentVO> replies;
}
