package com.flandreyu.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 私信会话项：每个不同联系人一条，含对方信息 + 最后一条消息 + 未读数
 */
@Data
public class ConversationVO {

    /** 对方用户ID */
    private Long peerId;
    /** 对方昵称 */
    private String peerNickname;
    /** 对方头像 */
    private String peerAvatar;

    /** 最后一条消息ID */
    private Long lastMessageId;
    /** 最后一条消息的发送者（用于判断气泡方向） */
    private Long lastFromUserId;
    /** 最后一条消息内容 */
    private String lastContent;
    /** 最后一条消息时间 */
    private LocalDateTime lastTime;

    /** 对方发给我的未读数 */
    private int unread;
}
