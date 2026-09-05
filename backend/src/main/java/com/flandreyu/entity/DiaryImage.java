package com.flandreyu.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 日记图片实体，对应表 diary_image（一篇日记多张图）
 */
@Data
public class DiaryImage {
    private Long id;
    /** 所属日记ID */
    private Long diaryId;
    /** 原图地址 */
    private String originalUrl;
    /** 缩略图地址 */
    private String thumbUrl;
    /** 排序（越小越靠前） */
    private Integer sortOrder;
    /** 上传时间 */
    private LocalDateTime createdAt;
}
