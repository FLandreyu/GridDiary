package com.flandreyu.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 日记实体，对应表 diary
 */
@Data
public class Diary {
    private Long id;
    /** 作者ID */
    private Long userId;
    /** 标题 */
    private String title;
    /** 正文 */
    private String content;
    /** 封面缩略图（取第一张图） */
    private String cover;
    /** 是否公开：true公开 / false仅自己 */
    private Boolean isPublic;
    /** 分类（单选，可为空） */
    private String category;
    /** 标签，逗号分隔存库（最多 5 个） */
    private String tags;
    /** 点赞数（冗余计数） */
    private Integer likeCount;
    /** 发布时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
