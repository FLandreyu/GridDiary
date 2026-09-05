package com.flandreyu.mapper;

import java.util.List;

import com.flandreyu.entity.DiaryImage;

/**
 * 日记图片表 Mapper
 */
public interface DiaryImageMapper {

    /** 新增图片记录，返回受影响行数 */
    int insert(DiaryImage image);

    /** 按日记ID查询其全部图片（按 sort_order 升序） */
    List<DiaryImage> selectByDiaryId(Long diaryId);

    /** 删除某张图片记录 */
    int deleteById(Long id);

    /** 删除某日记下的全部图片记录 */
    int deleteByDiaryId(Long diaryId);
}
