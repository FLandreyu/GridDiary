package com.flandreyu.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flandreyu.common.BusinessException;
import com.flandreyu.dto.MessageSendRequest;
import com.flandreyu.entity.Message;
import com.flandreyu.entity.User;
import com.flandreyu.mapper.MessageMapper;
import com.flandreyu.mapper.UserMapper;
import com.flandreyu.service.MessageService;
import com.flandreyu.vo.ConversationVO;
import com.flandreyu.vo.MessageDetailVO;

import lombok.RequiredArgsConstructor;

/**
 * 私信模块业务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    public List<ConversationVO> conversations(long me) {
        return messageMapper.selectConversations(me);
    }

    @Override
    public long unreadCount(long me) {
        return messageMapper.countUnread(me);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageDetailVO detail(long me, long peerId, int limit) {
        User peer = requireUser(peerId);
        // 数据库按时间倒序取最近 limit 条，再反转成时间正序展示
        List<Message> messages = new ArrayList<>(
                messageMapper.selectConversation(me, peerId, limit));
        Collections.reverse(messages);
        // 打开会话：把对方发给我的全部标记为已读
        messageMapper.markRead(me, peerId);

        MessageDetailVO vo = new MessageDetailVO();
        vo.setPeerId(peer.getId());
        vo.setPeerNickname(peer.getNickname());
        vo.setPeerAvatar(peer.getAvatar());
        vo.setMessages(messages);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long send(long fromUserId, MessageSendRequest req) {
        if (fromUserId == req.getToUserId()) {
            throw new BusinessException("不能给自己发私信");
        }
        User toUser = requireUser(req.getToUserId());

        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUser.getId());
        message.setContent(req.getContent().trim());
        messageMapper.insert(message);
        return message.getId();
    }

    private User requireUser(long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }
}
