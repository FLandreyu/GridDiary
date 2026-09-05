package com.flandreyu.mapper;

import java.util.List;

import com.flandreyu.entity.Comment;
import com.flandreyu.vo.CommentVO;

/**
 * 评论表 Mapper（两级：一级评论 + 回复）
 */
public interface CommentMapper {

    /** 新增评论/回复，返回受影响行数 */
    int insert(Comment comment);

    /** 按主键查询（用于校验回复目标 / 删除鉴权） */
    Comment selectById(Long id);

    /** 查询某日记的全部评论（含作者昵称/头像，按时间升序，由 service 组装树） */
    List<CommentVO> selectVOByDiaryId(Long diaryId);

    /** 删除某条评论 */
    int deleteById(Long id);

    /** 删除某一级评论下的全部回复 */
    int deleteByParentId(Long parentId);

    /** 删除某日记下全部评论 */
    int deleteByDiaryId(Long diaryId);

    /** 某日记评论总数 */
    long countByDiaryId(Long diaryId);
}
