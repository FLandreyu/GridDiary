package com.flandreyu.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 评论实体，对应表 comment（支持两级：parentId 为空为一级评论）
 */
@Data
public class Comment {
    private Long id;
    /** 日记ID */
    private Long diaryId;
    /** 评论人ID */
    private Long userId;
    /** 父评论ID（NULL 为一级评论） */
    private Long parentId;
    /** 评论内容 */
    private String content;
    /** 评论时间 */
    private LocalDateTime createdAt;
}
