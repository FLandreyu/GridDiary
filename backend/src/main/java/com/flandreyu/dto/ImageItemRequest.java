package com.flandreyu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 一条日记图片（原图/缩略图地址），由上传接口先生成
 */
@Data
public class ImageItemRequest {

    @NotBlank(message = "原图地址不能为空")
    private String originalUrl;

    @NotBlank(message = "缩略图地址不能为空")
    private String thumbUrl;
}
