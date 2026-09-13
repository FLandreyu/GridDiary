package com.flandreyu.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.flandreyu.entity.DiaryImage;
import com.flandreyu.vo.GalleryImageVO;

/**
 * 日记图片表 Mapper
 */
public interface DiaryImageMapper {

    /** 新增图片记录，返回受影响行数 */
    int insert(DiaryImage image);

    /** 按日记ID查询其全部图片（按 sort_order 升序） */
    List<DiaryImage> selectByDiaryId(Long diaryId);

    /** 相册：分页查询公开日记的图片（含日记标题与作者，按发布时间倒序） */
    List<GalleryImageVO> selectGalleryPage(@Param("offset") int offset, @Param("size") int size);

    /** 相册总数（公开日记的图片数） */
    long countGallery();

    /** 删除某张图片记录 */
    int deleteById(Long id);

    /** 删除某日记下的全部图片记录 */
    int deleteByDiaryId(Long diaryId);
}
