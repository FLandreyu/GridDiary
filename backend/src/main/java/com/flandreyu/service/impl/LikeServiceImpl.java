package com.flandreyu.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flandreyu.common.BusinessException;
import com.flandreyu.entity.Diary;
import com.flandreyu.entity.DiaryLike;
import com.flandreyu.mapper.DiaryLikeMapper;
import com.flandreyu.mapper.DiaryMapper;
import com.flandreyu.service.LikeService;
import com.flandreyu.vo.LikeVO;

import lombok.RequiredArgsConstructor;

/**
 * 点赞模块业务实现
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final DiaryLikeMapper diaryLikeMapper;
    private final DiaryMapper diaryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeVO like(long diaryId, long userId) {
        Diary diary = requireDiary(diaryId);
        if (diaryLikeMapper.find(diaryId, userId) == null) {
            DiaryLike like = new DiaryLike();
            like.setDiaryId(diaryId);
            like.setUserId(userId);
            diaryLikeMapper.insert(like);
            diaryMapper.changeLikeCount(diaryId, 1); // 冗余计数 +1
        }
        return build(diaryId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeVO unlike(long diaryId, long userId) {
        requireDiary(diaryId);
        if (diaryLikeMapper.find(diaryId, userId) != null) {
            diaryLikeMapper.delete(diaryId, userId);
            diaryMapper.changeLikeCount(diaryId, -1); // 冗余计数 -1
        }
        return build(diaryId, userId);
    }

    @Override
    public LikeVO status(long diaryId, long userId) {
        requireDiary(diaryId);
        return build(diaryId, userId);
    }

    private Diary requireDiary(long diaryId) {
        Diary diary = diaryMapper.findById(diaryId);
        if (diary == null) {
            throw new BusinessException(404, "日记不存在");
        }
        return diary;
    }

    /** 组装当前点赞状态：是否已赞 + 日记上的实时计数 */
    private LikeVO build(long diaryId, long userId) {
        boolean liked = diaryLikeMapper.find(diaryId, userId) != null;
        int count = 0;
        Diary diary = diaryMapper.findById(diaryId);
        if (diary != null && diary.getLikeCount() != null) {
            count = diary.getLikeCount();
        }
        return new LikeVO(liked, count);
    }
}
