package com.flandreyu.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.flandreyu.entity.Diary;
import com.flandreyu.vo.DiaryVO;
import com.flandreyu.vo.SiteStatsVO;

/**
 * 日记表 Mapper
 */
public interface DiaryMapper {

        /** 新增日记，返回受影响行数（自增主键回填到实体 id） */
        int insert(Diary diary);

        /** 按主键查询（实体，内部校验用） */
        Diary findById(Long id);

        /** 按主键查询（含作者昵称/头像，详情用） */
        DiaryVO selectVOById(Long id);

        /**
         * 分页查询公开日记（首页九宫格）；userId 非空则只看该作者的公开日记；
         * keyword 非空则按标题/正文模糊搜索；tag 非空则只查带该标签的日记。
         */
        List<DiaryVO> selectPage(@Param("offset") int offset,
                        @Param("size") int size,
                        @Param("userId") Long userId,
                        @Param("keyword") String keyword,
                        @Param("tag") String tag);

        /** 与 selectPage 条件一致的总数（分页用） */
        long countPage(@Param("userId") Long userId,
                        @Param("keyword") String keyword,
                        @Param("tag") String tag);

        /** 分页查询某用户自己的全部日记（含私密） */
        List<DiaryVO> selectByUserId(@Param("userId") Long userId,
                        @Param("offset") int offset,
                        @Param("size") int size);

        /** 某用户日记总数 */
        long countByUserId(@Param("userId") Long userId);

        /** 热门排行：按点赞数倒序取前 N（仅公开） */
        List<DiaryVO> selectHot(@Param("limit") int limit);

        /** 动态更新日记（标题/正文/封面/是否公开等按需更新） */
        int update(Diary diary);

        /** 整行更新日记（编辑用，允许把 cover 置空） */
        int updateFull(Diary diary);

        /** 删除日记（关联图片/评论/点赞由外键级联删除） */
        int deleteById(Long id);

        /** 点赞数增减：delta 为 +1 或 -1 */
        int changeLikeCount(@Param("id") Long id, @Param("delta") int delta);

        /** 站点统计（单行聚合，首页侧边栏用） */
        SiteStatsVO selectSiteStats();

        /** 所有公开日记的标签原值（逗号分隔），供标签云/统计在 Java 侧聚合 */
        List<String> selectPublicTags();

        /** 某用户指定区间内有日记的日期（yyyy-MM-dd，含私密；from 含、to 不含） */
        List<String> selectDiaryDates(@Param("userId") Long userId,
                        @Param("from") String from,
                        @Param("to") String to);
}
