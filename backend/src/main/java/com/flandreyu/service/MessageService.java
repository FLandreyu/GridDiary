package com.flandreyu.service;

import java.util.List;

import com.flandreyu.dto.MessageSendRequest;
import com.flandreyu.vo.ConversationVO;
import com.flandreyu.vo.MessageDetailVO;

/**
 * 私信模块业务接口
 */
public interface MessageService {

    /** 我的会话列表（含未读数） */
    List<ConversationVO> conversations(long me);

    /** 我的未读私信总数（导航红点） */
    long unreadCount(long me);

    /**
     * 打开与某人的聊天：返回历史消息（正序），并把对方发给我的消息标记已读
     */
    MessageDetailVO detail(long me, long peerId, int limit);

    /** 发送私信，返回消息ID */
    Long send(long fromUserId, MessageSendRequest req);
}
