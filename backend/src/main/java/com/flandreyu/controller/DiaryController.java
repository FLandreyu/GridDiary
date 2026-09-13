package com.flandreyu.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flandreyu.common.BusinessException;
import com.flandreyu.common.PageResult;
import com.flandreyu.common.PublicApi;
import com.flandreyu.common.Result;
import com.flandreyu.common.SessionKeys;
import com.flandreyu.dto.DiarySaveRequest;
import com.flandreyu.service.DiaryService;
import com.flandreyu.vo.DiaryVO;
import com.flandreyu.vo.UserVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 日记模块接口
 */
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /** 首页/搜索/他人公开日记（分页，支持按标签筛选） */
    @GetMapping
    @PublicApi
    public Result<PageResult<DiaryVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String tg = (tag == null || tag.isBlank()) ? null : tag.trim();
        return Result.ok(diaryService.pagePublic(safePage, safeSize, userId, kw, tg));
    }

    /** 我的日记（含私密，需登录） */
    @GetMapping("/mine")
    public Result<PageResult<DiaryVO>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpSession session) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        return Result.ok(diaryService.pageMine(currentUserId(session), safePage, safeSize));
    }

    /** 热门排行榜 Top N */
    @GetMapping("/hot")
    @PublicApi
    public Result<java.util.List<DiaryVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(diaryService.hot(Math.min(Math.max(1, limit), 50)));
    }

    /** 相册：公开日记的图片流（分页，含日记标题与作者） */
    @GetMapping("/gallery")
    @PublicApi
    public Result<PageResult<com.flandreyu.vo.GalleryImageVO>> gallery(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "24") int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 60);
        return Result.ok(diaryService.gallery(safePage, safeSize));
    }

    /** 日记详情（公开可看，私密仅作者） */
    @GetMapping("/{id}")
    @PublicApi
    public Result<DiaryVO> detail(@PathVariable Long id, HttpSession session) {
        return Result.ok(diaryService.detail(id, currentUserIdOrNull(session)));
    }

    /** 写日记（需登录） */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody DiarySaveRequest req, HttpSession session) {
        return Result.ok(diaryService.create(currentUserId(session), req));
    }

    /** 修改日记（需登录，仅作者） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
            @Valid @RequestBody DiarySaveRequest req,
            HttpSession session) {
        diaryService.update(currentUserId(session), id, req);
        return Result.ok();
    }

    /** 删除日记（需登录，仅作者） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        diaryService.delete(currentUserId(session), id);
        return Result.ok();
    }

    /** 当前登录用户ID（不存在则抛 401，用于需登录接口） */
    private Long currentUserId(HttpSession session) {
        UserVO user = sessionUser(session);
        if (user == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return user.getId();
    }

    /** 当前登录用户ID，匿名返回 null（用于公开接口的可选鉴权） */
    private Long currentUserIdOrNull(HttpSession session) {
        UserVO user = sessionUser(session);
        return user == null ? null : user.getId();
    }

    private UserVO sessionUser(HttpSession session) {
        Object obj = session.getAttribute(SessionKeys.LOGIN_USER);
        return obj instanceof UserVO user ? user : null;
    }
}
