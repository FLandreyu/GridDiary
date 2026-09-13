package com.flandreyu.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 相册条目：一张公开日记的图片 + 它所属日记与作者的信息
 */
@Data
public class GalleryImageVO {

    /** 图片ID */
    private Long imageId;
    /** 所属日记ID */
    private Long diaryId;
    /** 所属日记标题 */
    private String diaryTitle;
    /** 原图地址 */
    private String originalUrl;
    /** 缩略图地址 */
    private String thumbUrl;
    /** 作者ID */
    private Long userId;
    /** 作者昵称 */
    private String authorNickname;
    /** 作者头像 */
    private String authorAvatar;
    /** 日记发布时间 */
    private LocalDateTime createdAt;
}
