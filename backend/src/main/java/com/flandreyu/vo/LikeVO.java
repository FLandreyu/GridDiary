package com.flandreyu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞状态返回：当前是否已赞 + 实时点赞数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeVO {

    /** 当前用户是否已赞 */
    private boolean liked;
    /** 日记点赞总数 */
    private int likeCount;
}
