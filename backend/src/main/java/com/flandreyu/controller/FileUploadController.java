package com.flandreyu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.flandreyu.common.BusinessException;
import com.flandreyu.common.Result;
import com.flandreyu.util.ImageStorage;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 文件上传接口（需登录）。上传成功后返回每张图片的 {originalUrl, thumbUrl}，
 * 前端在写日记/编辑日记时把地址随表单一起提交。
 */
@Tag(name = "文件上传", description = "日记配图上传（原图 + 缩略图）")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final ImageStorage imageStorage;

    /** 上传多张图片（表单字段名 files） */
    @PostMapping("/image")
    public Result<List<ImageStorage.ImageUrls>> upload(
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        List<ImageStorage.ImageUrls> result = files.stream().map(imageStorage::save).toList();
        return Result.ok(result);
    }
}
