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

    /** 图片列表（按顺序展示），可为空 */
    @Valid
    private List<ImageItemRequest> images;
}
