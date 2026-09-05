package com.flandreyu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送私信请求
 */
@Data
public class MessageSendRequest {

    /** 接收人ID */
    @NotNull(message = "接收人不能为空")
    private Long toUserId;

    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "私信内容不能超过 1000 字")
    private String content;
}
