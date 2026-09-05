package com.flandreyu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论 / 回复请求
 */
@Data
public class CommentSaveRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过 500 字")
    private String content;

    /**
     * 被回复的评论ID：为空表示一级评论；
     * 若回复的是“回复”，后端会自动挂到其所属的一级评论下（保证两级结构）
     */
    private Long parentId;
}
