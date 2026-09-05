package com.flandreyu.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 点赞实体，对应表 diary_like（diaryId + userId 唯一，防重复点赞）
 */
@Data
public class DiaryLike {
    private Long id;
    /** 日记ID */
    private Long diaryId;
    /** 点赞人ID */
    private Long userId;
    /** 点赞时间 */
    private LocalDateTime createdAt;
}
