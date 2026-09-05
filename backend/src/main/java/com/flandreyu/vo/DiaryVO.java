package com.flandreyu.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.flandreyu.entity.DiaryImage;

import lombok.Data;

/**
 * 日记展示对象：含作者昵称/头像；详情页额外携带 images
 */
@Data
public class DiaryVO {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    /** 封面缩略图 */
    private String cover;
    private Boolean isPublic;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 作者昵称 */
    private String authorNickname;
    /** 作者头像 */
    private String authorAvatar;

    /** 详情页的图片列表（列表查询时不填充） */
    private List<DiaryImage> images;
}
