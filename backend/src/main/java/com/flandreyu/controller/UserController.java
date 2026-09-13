package com.flandreyu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flandreyu.common.PublicApi;
import com.flandreyu.common.Result;
import com.flandreyu.common.SessionKeys;
import com.flandreyu.dto.ChangePasswordRequest;
import com.flandreyu.dto.ForgotPasswordRequest;
import com.flandreyu.dto.LoginRequest;
import com.flandreyu.dto.RegisterRequest;
import com.flandreyu.dto.ResetPasswordRequest;
import com.flandreyu.dto.UpdateProfileRequest;
import com.flandreyu.service.UserService;
import com.flandreyu.vo.UserStatsVO;
import com.flandreyu.vo.UserVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 用户模块接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 注册 */
    @PostMapping("/register")
    @PublicApi
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(userService.register(req));
    }

    /** 登录（写入 Session） */
    @PostMapping("/login")
    @PublicApi
    public Result<UserVO> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        return Result.ok(userService.login(req, session));
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        userService.logout(session);
        return Result.ok();
    }

    /** 当前登录用户 */
    @GetMapping("/me")
    public Result<UserVO> me(HttpSession session) {
        return Result.ok(userService.getCurrent(session));
    }

    /** 修改个人资料（昵称/头像），需登录 */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest req,
            HttpSession session) {
        long userId = userService.getCurrent(session).getId();
        UserVO vo = userService.updateProfile(userId, req);
        // 同步 Session，使顶栏等即时显示最新资料
        session.setAttribute(SessionKeys.LOGIN_USER, vo);
        return Result.ok(vo);
    }

    /** 修改密码，需登录 */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req,
            HttpSession session) {
        userService.changePassword(userService.getCurrent(session).getId(), req);
        return Result.ok();
    }

    /** 某用户公开资料（个人主页，匿名可访问） */
    @GetMapping("/{id}")
    @PublicApi
    public Result<UserVO> profile(@PathVariable Long id) {
        return Result.ok(userService.profile(id));
    }

    /** 某用户的作品统计（详情页作者卡 / 个人主页，匿名可见） */
    @GetMapping("/{id}/stats")
    @PublicApi
    public Result<UserStatsVO> userStats(@PathVariable Long id) {
        return Result.ok(userService.stats(id));
    }

    /** 找回密码：提交邮箱获取验证码 */
    @PostMapping("/forgot-password")
    @PublicApi
    public Result<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return Result.ok(userService.forgotPassword(req));
    }

    /** 重置密码 */
    @PostMapping("/reset-password")
    @PublicApi
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        userService.resetPassword(req);
        return Result.ok();
    }
}
