package com.flandreyu.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flandreyu.common.BusinessException;
import com.flandreyu.dto.CommentSaveRequest;
import com.flandreyu.entity.Comment;
import com.flandreyu.entity.Diary;
import com.flandreyu.entity.User;
import com.flandreyu.mapper.CommentMapper;
import com.flandreyu.mapper.DiaryMapper;
import com.flandreyu.mapper.UserMapper;
import com.flandreyu.service.CommentService;
import com.flandreyu.vo.CommentVO;

import lombok.RequiredArgsConstructor;

/**
 * 评论模块业务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final DiaryMapper diaryMapper;
    private final UserMapper userMapper;

    @Override
    public List<CommentVO> listByDiary(long diaryId) {
        List<CommentVO> all = commentMapper.selectVOByDiaryId(diaryId);

        // 组装两级树：一级评论 -> replies
        List<CommentVO> roots = new ArrayList<>();
        Map<Long, CommentVO> rootIndex = new LinkedHashMap<>();
        for (CommentVO c : all) {
            if (c.getParentId() == null) {
                roots.add(c);
                rootIndex.put(c.getId(), c);
            }
        }
        for (CommentVO c : all) {
            if (c.getParentId() != null) {
                CommentVO root = rootIndex.get(c.getParentId());
                if (root != null) {
                    if (root.getReplies() == null) {
                        root.setReplies(new ArrayList<>());
                    }
                    root.getReplies().add(c);
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO add(long diaryId, long userId, CommentSaveRequest req) {
        if (diaryMapper.findById(diaryId) == null) {
            throw new BusinessException(404, "日记不存在");
        }

        Comment comment = new Comment();
        comment.setDiaryId(diaryId);
        comment.setUserId(userId);
        comment.setContent(req.getContent().trim());

        if (req.getParentId() != null) {
            Comment parent = commentMapper.selectById(req.getParentId());
            if (parent == null || !parent.getDiaryId().equals(diaryId)) {
                throw new BusinessException("回复的评论不存在");
            }
            // 回复“回复”时挂到其所属的一级评论下，保证只有两级
            comment.setParentId(parent.getParentId() != null ? parent.getParentId() : parent.getId());
        }

        commentMapper.insert(comment);

        User author = userMapper.findById(userId);
        return toVO(comment, author);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(long diaryId, long commentId, long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !comment.getDiaryId().equals(diaryId)) {
            throw new BusinessException(404, "评论不存在");
        }
        Diary diary = diaryMapper.findById(diaryId);
        boolean isCommentAuthor = comment.getUserId().equals(userId);
        boolean isDiaryAuthor = diary != null && diary.getUserId().equals(userId);
        if (!isCommentAuthor && !isDiaryAuthor) {
            throw new BusinessException(403, "无权删除该评论");
        }
        if (comment.getParentId() == null) {
            // 一级评论被删，级联删其回复
            commentMapper.deleteByParentId(commentId);
        }
        commentMapper.deleteById(commentId);
    }

    private CommentVO toVO(Comment comment, User author) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setDiaryId(comment.getDiaryId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        if (author != null) {
            vo.setAuthorNickname(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }
        return vo;
    }
}
