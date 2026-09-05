package com.flandreyu.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.flandreyu.entity.Message;
import com.flandreyu.vo.ConversationVO;

/**
 * 私信表 Mapper
 */
public interface MessageMapper {

    /** 发送私信，返回受影响行数 */
    int insert(Message message);

    /** 我收到的私信（按时间倒序） */
    List<Message> selectReceived(@Param("toUserId") Long toUserId);

    /** 我发出的私信（按时间倒序） */
    List<Message> selectSent(@Param("fromUserId") Long fromUserId);

    /** 与某人的历史会话记录（时间升序取最近 limit 条） */
    List<Message> selectConversation(@Param("userA") Long userA,
            @Param("userB") Long userB,
            @Param("limit") int limit);

    /** 会话列表：每个联系人一条（含对方信息 + 最后一条消息 + 未读数），按最近时间倒序 */
    List<ConversationVO> selectConversations(@Param("me") Long me);

    /** 将某人发给我的私信全部标记为已读 */
    int markRead(@Param("toUserId") Long toUserId, @Param("fromUserId") Long fromUserId);

    /** 我的未读私信数 */
    long countUnread(@Param("toUserId") Long toUserId);
}
