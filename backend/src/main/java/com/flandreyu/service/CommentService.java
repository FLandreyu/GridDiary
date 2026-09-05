package com.flandreyu.service;

import java.util.List;

import com.flandreyu.dto.CommentSaveRequest;
import com.flandreyu.vo.CommentVO;

/**
 * 评论模块业务接口（两级：一级评论 + 回复）
 */
public interface CommentService {

    /** 某日记的评论树（一级评论 + 各自回复） */
    List<CommentVO> listByDiary(long diaryId);

    /** 发表评论 / 回复 */
    CommentVO add(long diaryId, long userId, CommentSaveRequest req);

    /** 删除评论（评论作者或日记作者可删；删一级评论会级联删其回复） */
    void delete(long diaryId, long commentId, long userId);
}
