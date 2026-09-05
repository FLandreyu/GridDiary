package com.flandreyu.mapper;

import org.apache.ibatis.annotations.Param;

import com.flandreyu.entity.DiaryLike;

/**
 * 点赞表 Mapper
 */
public interface DiaryLikeMapper {

    /** 新增点赞，返回受影响行数 */
    int insert(DiaryLike like);

    /** 取消点赞（按 日记+用户 删除） */
    int delete(@Param("diaryId") Long diaryId, @Param("userId") Long userId);

    /** 查询某用户是否已赞某日记 */
    DiaryLike find(@Param("diaryId") Long diaryId, @Param("userId") Long userId);

    /** 某日记点赞总数 */
    long countByDiaryId(Long diaryId);
}
