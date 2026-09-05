package com.flandreyu.service;

import com.flandreyu.vo.LikeVO;

/**
 * 点赞模块业务接口
 */
public interface LikeService {

    /** 点赞（幂等），返回最新点赞状态 */
    LikeVO like(long diaryId, long userId);

    /** 取消点赞，返回最新点赞状态 */
    LikeVO unlike(long diaryId, long userId);

    /** 查询某用户对某日记的点赞状态（详情页初始化用） */
    LikeVO status(long diaryId, long userId);
}
