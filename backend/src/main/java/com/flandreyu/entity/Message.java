package com.flandreyu.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 私信实体，对应表 message
 */
@Data
public class Message {
    private Long id;
    /** 发送人ID */
    private Long fromUserId;
    /** 接收人ID */
    private Long toUserId;
    /** 内容 */
    private String content;
    /** 是否已读：true已读 / false未读 */
    private Boolean isRead;
    /** 发送时间 */
    private LocalDateTime createdAt;
}
