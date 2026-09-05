package com.flandreyu.service;

import java.util.List;

import com.flandreyu.common.PageResult;
import com.flandreyu.dto.DiarySaveRequest;
import com.flandreyu.vo.DiaryVO;

/**
 * 日记模块业务接口
 */
public interface DiaryService {

    /**
     * 分页查询公开日记（首页/搜索/他人主页）。
     *
     * @param userId  非空则只看该用户的公开日记
     * @param keyword 非空则按标题/正文搜索
     */
    PageResult<DiaryVO> pagePublic(int page, int size, Long userId, String keyword);

    /** 分页查询某用户自己的全部日记（含私密） */
    PageResult<DiaryVO> pageMine(long userId, int page, int size);

    /** 热门日记排行榜（仅公开，按点赞数） */
    List<DiaryVO> hot(int limit);

    /**
     * 日记详情（含图片列表）。
     *
     * @param id       日记ID
     * @param viewerId 当前查看者ID，匿名传 null；私密日记仅作者可看
     */
    DiaryVO detail(long id, Long viewerId);

    /** 新建日记，返回日记ID */
    Long create(long userId, DiarySaveRequest req);

    /** 修改自己的日记 */
    void update(long userId, long id, DiarySaveRequest req);

    /** 删除自己的日记 */
    void delete(long userId, long id);
}
