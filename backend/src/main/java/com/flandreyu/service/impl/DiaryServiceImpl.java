package com.flandreyu.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flandreyu.common.BusinessException;
import com.flandreyu.common.PageResult;
import com.flandreyu.dto.DiarySaveRequest;
import com.flandreyu.dto.ImageItemRequest;
import com.flandreyu.entity.Diary;
import com.flandreyu.entity.DiaryImage;
import com.flandreyu.mapper.DiaryImageMapper;
import com.flandreyu.mapper.DiaryMapper;
import com.flandreyu.service.DiaryService;
import com.flandreyu.util.TagUtil;
import com.flandreyu.vo.DiaryVO;
import com.flandreyu.vo.GalleryImageVO;

import lombok.RequiredArgsConstructor;

/**
 * 日记模块业务实现
 */
@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private final DiaryMapper diaryMapper;
    private final DiaryImageMapper diaryImageMapper;

    @Override
    public PageResult<DiaryVO> pagePublic(int page, int size, Long userId, String keyword, String tag) {
        long total = diaryMapper.countPage(userId, keyword, tag);
        if (total == 0) {
            return new PageResult<>(0, List.of());
        }
        int offset = (page - 1) * size;
        return new PageResult<>(total, diaryMapper.selectPage(offset, size, userId, keyword, tag));
    }

    @Override
    public PageResult<DiaryVO> pageMine(long userId, int page, int size) {
        long total = diaryMapper.countByUserId(userId);
        if (total == 0) {
            return new PageResult<>(0, List.of());
        }
        int offset = (page - 1) * size;
        return new PageResult<>(total, diaryMapper.selectByUserId(userId, offset, size));
    }

    @Override
    public List<DiaryVO> hot(int limit) {
        return diaryMapper.selectHot(limit);
    }

    @Override
    public PageResult<GalleryImageVO> gallery(int page, int size) {
        long total = diaryImageMapper.countGallery();
        if (total == 0) {
            return new PageResult<>(0, List.of());
        }
        int offset = (page - 1) * size;
        return new PageResult<>(total, diaryImageMapper.selectGalleryPage(offset, size));
    }

    @Override
    public DiaryVO detail(long id, Long viewerId) {
        DiaryVO vo = diaryMapper.selectVOById(id);
        if (vo == null) {
            throw new BusinessException(404, "日记不存在");
        }
        // 私密日记：仅作者本人可查看
        if (!Boolean.TRUE.equals(vo.getIsPublic())) {
            if (viewerId == null || !viewerId.equals(vo.getUserId())) {
                throw new BusinessException(403, "该日记未公开，无权查看");
            }
        }
        vo.setImages(diaryImageMapper.selectByDiaryId(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(long userId, DiarySaveRequest req) {
        List<ImageItemRequest> images = normalizeImages(req.getImages());

        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setTitle(req.getTitle());
        diary.setContent(req.getContent());
        diary.setIsPublic(req.getIsPublic() == null || req.getIsPublic());
        diary.setCategory(normalizeCategory(req.getCategory()));
        diary.setTags(TagUtil.join(req.getTags()));
        diary.setCover(firstCover(images));
        diaryMapper.insert(diary);

        saveImages(diary.getId(), images);
        return diary.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(long userId, long id, DiarySaveRequest req) {
        Diary exist = diaryMapper.findById(id);
        if (exist == null) {
            throw new BusinessException(404, "日记不存在");
        }
        if (!exist.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能修改自己的日记");
        }
        List<ImageItemRequest> images = normalizeImages(req.getImages());

        Diary diary = new Diary();
        diary.setId(id);
        diary.setTitle(req.getTitle());
        diary.setContent(req.getContent());
        // 未传 isPublic 时保持原状
        diary.setIsPublic(req.getIsPublic() != null ? req.getIsPublic() : exist.getIsPublic());
        diary.setCategory(normalizeCategory(req.getCategory()));
        diary.setTags(TagUtil.join(req.getTags()));
        diary.setCover(firstCover(images));
        diaryMapper.updateFull(diary);

        // 图片整组替换：先清旧图再写新图（同一事务）
        diaryImageMapper.deleteByDiaryId(id);
        saveImages(id, images);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(long userId, long id) {
        Diary exist = diaryMapper.findById(id);
        if (exist == null) {
            throw new BusinessException(404, "日记不存在");
        }
        if (!exist.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的日记");
        }
        // 图片/评论/点赞由外键级联删除
        diaryMapper.deleteById(id);
    }

    /** 分类：trim 后为空则存 null，最长 50 字 */
    private String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        String c = category.trim();
        if (c.isEmpty()) {
            return null;
        }
        return c.length() > 50 ? c.substring(0, 50) : c;
    }

    /** 空列表 -> 空集合，避免 null */
    private List<ImageItemRequest> normalizeImages(List<ImageItemRequest> images) {
        return images == null ? List.of() : images;
    }

    /** 取第一张图的缩略图作为封面，无图则 null */
    private String firstCover(List<ImageItemRequest> images) {
        if (images.isEmpty()) {
            return null;
        }
        return images.get(0).getThumbUrl();
    }

    /** 批量写入图片记录 */
    private void saveImages(Long diaryId, List<ImageItemRequest> images) {
        for (int i = 0; i < images.size(); i++) {
            ImageItemRequest item = images.get(i);
            DiaryImage image = new DiaryImage();
            image.setDiaryId(diaryId);
            image.setOriginalUrl(item.getOriginalUrl());
            image.setThumbUrl(item.getThumbUrl());
            image.setSortOrder(i);
            diaryImageMapper.insert(image);
        }
    }
}
