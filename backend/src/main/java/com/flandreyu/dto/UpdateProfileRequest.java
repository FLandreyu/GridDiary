package com.flandreyu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人资料请求（昵称 / 头像）
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过 64")
    private String nickname;

    /** 头像地址，可为空字符串（保留原头像） */
    @Size(max = 255, message = "头像地址过长")
    private String avatar;
}
