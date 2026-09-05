package com.flandreyu.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户实体，对应表 user
 */
@Data
public class User {
    private Long id;
    /** 用户名（唯一） */
    private String username;
    /** BCrypt 散列后的密码 */
    private String password;
    /** 邮箱（唯一） */
    private String email;
    /** 昵称 */
    private String nickname;
    /** 头像地址 */
    private String avatar;
    /** 注册时间 */
    private LocalDateTime createdAt;
}
