package com.flandreyu.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 返回给前端的用户信息（不含密码）
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private LocalDateTime createdAt;
}
