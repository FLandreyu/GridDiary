package com.flandreyu.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建 / 修改日记请求
 */
@Data
public class DiarySaveRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    /** 正文，可为空 */
    private String content;

    /** 是否公开，默认 true */
    private Boolean isPublic;

    /** 分类（单选，可为空，最多 50 字） */
    @Size(max = 50, message = "分类长度不能超过 50")
    private String category;

    /** 标签（最多 5 个，单个不超过 20 字；服务层会 trim / 去重 / 去空） */
    @Size(max = 5, message = "标签最多 5 个")
    private List<String> tags;

    /** 图片列表（按顺序展示），可为空 */
    @Valid
    private List<ImageItemRequest> images;
}
