package com.flandreyu.service.impl;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flandreyu.common.BusinessException;
import com.flandreyu.common.SessionKeys;
import com.flandreyu.dto.ChangePasswordRequest;
import com.flandreyu.dto.ForgotPasswordRequest;
import com.flandreyu.dto.LoginRequest;
import com.flandreyu.dto.RegisterRequest;
import com.flandreyu.dto.ResetPasswordRequest;
import com.flandreyu.dto.UpdateProfileRequest;
import com.flandreyu.entity.User;
import com.flandreyu.mapper.UserMapper;
import com.flandreyu.service.UserService;
import com.flandreyu.vo.UserVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户模块业务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 找回密码验证码缓存：email -> 验证码记录（10 分钟有效）。
     * 说明：课程演示用内存存储；生产环境应通过邮件发送并配合 Redis 等持久化。
     */
    private final ConcurrentHashMap<String, ResetEntry> resetStore = new ConcurrentHashMap<>();

    @Override
    public UserVO register(RegisterRequest req) {
        if (userMapper.findByUsername(req.getUsername()) != null) {
            throw new BusinessException("用户名已被注册");
        }
        if (userMapper.findByEmail(req.getEmail()) != null) {
            throw new BusinessException("该邮箱已被注册");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword())); // BCrypt 加盐散列
        user.setEmail(req.getEmail());
        user.setNickname(req.getNickname());
        userMapper.insert(user);
        return toVO(user);
    }

    @Override
    public UserVO login(LoginRequest req, HttpSession session) {
        User user = userMapper.findByUsername(req.getUsername());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        UserVO vo = toVO(user);
        session.setAttribute(SessionKeys.LOGIN_USER, vo);
        return vo;
    }

    @Override
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @Override
    public UserVO getCurrent(HttpSession session) {
        Object obj = session.getAttribute(SessionKeys.LOGIN_USER);
        if (obj == null) {
            throw new BusinessException(401, "未登录");
        }
        return (UserVO) obj;
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest req) {
        if (userMapper.findByEmail(req.getEmail()) == null) {
            throw new BusinessException("该邮箱尚未注册");
        }
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        resetStore.put(req.getEmail(), new ResetEntry(code, LocalDateTime.now().plusMinutes(10)));
        log.warn("[演示] 邮箱 {} 的找回密码验证码：{}（真实项目应通过邮件发送）", req.getEmail(), code);
        return code;
    }

    @Override
    public void resetPassword(ResetPasswordRequest req) {
        ResetEntry entry = resetStore.get(req.getEmail());
        if (entry == null || !entry.code.equals(req.getCode())) {
            throw new BusinessException("验证码错误或已失效");
        }
        if (entry.expireAt.isBefore(LocalDateTime.now())) {
            resetStore.remove(req.getEmail());
            throw new BusinessException("验证码已过期，请重新获取");
        }
        User user = userMapper.findByEmail(req.getEmail());
        if (user == null) {
            throw new BusinessException("该邮箱尚未注册");
        }
        User update = new User();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userMapper.update(update);
        resetStore.remove(req.getEmail());
    }

    /** 公开资料（个人主页） */
    @Override
    public UserVO profile(long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return toVO(user);
    }

    /** 修改个人资料（昵称/头像） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(long userId, UpdateProfileRequest req) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        User update = new User();
        update.setId(userId);
        String nick = req.getNickname().trim();
        if (!nick.isEmpty()) {
            update.setNickname(nick);
        }
        if (req.getAvatar() != null && !req.getAvatar().isBlank()) {
            update.setAvatar(req.getAvatar().trim());
        }
        userMapper.update(update);
        return toVO(userMapper.findById(userId));
    }

    /** 修改密码（需校验原密码） */
    @Override
    public void changePassword(long userId, ChangePasswordRequest req) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userMapper.update(update);
    }

    /** 实体 -> VO（屏蔽密码等敏感字段） */
    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    /** 验证码记录 */
    private record ResetEntry(String code, LocalDateTime expireAt) {
    }
}
