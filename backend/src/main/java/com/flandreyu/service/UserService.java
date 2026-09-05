package com.flandreyu.service;

import com.flandreyu.dto.ChangePasswordRequest;
import com.flandreyu.dto.ForgotPasswordRequest;
import com.flandreyu.dto.LoginRequest;
import com.flandreyu.dto.RegisterRequest;
import com.flandreyu.dto.ResetPasswordRequest;
import com.flandreyu.dto.UpdateProfileRequest;
import com.flandreyu.vo.UserVO;

import jakarta.servlet.http.HttpSession;

/**
 * 用户模块业务接口：注册 / 登录 / 退出 / 当前用户 / 找回密码
 */
public interface UserService {

    /** 注册 */
    UserVO register(RegisterRequest req);

    /** 登录（写入 Session） */
    UserVO login(LoginRequest req, HttpSession session);

    /** 退出登录 */
    void logout(HttpSession session);

    /** 当前登录用户（从 Session 读取） */
    UserVO getCurrent(HttpSession session);

    /** 公开资料（个人主页，按用户ID查询） */
    UserVO profile(long id);

    /** 修改个人资料（昵称/头像），返回最新用户信息 */
    UserVO updateProfile(long userId, UpdateProfileRequest req);

    /** 修改密码（需校验原密码） */
    void changePassword(long userId, ChangePasswordRequest req);

    /** 找回密码：提交邮箱获取验证码 */
    String forgotPassword(ForgotPasswordRequest req);

    /** 重置密码 */
    void resetPassword(ResetPasswordRequest req);
}
