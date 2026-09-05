package com.flandreyu.vo;

import java.util.List;

import com.flandreyu.entity.Message;

import lombok.Data;

/**
 * 与某人的聊天详情：对方信息 + 历史消息（时间正序）
 */
@Data
public class MessageDetailVO {

    private Long peerId;
    private String peerNickname;
    private String peerAvatar;
    private List<Message> messages;
}
