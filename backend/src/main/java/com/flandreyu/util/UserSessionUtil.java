package com.flandreyu.util;

import com.flandreyu.common.BusinessException;
import com.flandreyu.common.SessionKeys;
import com.flandreyu.vo.UserVO;

import jakarta.servlet.http.HttpSession;

/**
 * Session 中当前登录用户的读取工具
 */
public final class UserSessionUtil {

    private UserSessionUtil() {
    }

    /** 从 Session 取当前用户，未登录返回 null */
    public static UserVO getUser(HttpSession session) {
        Object obj = session.getAttribute(SessionKeys.LOGIN_USER);
        return obj instanceof UserVO user ? user : null;
    }

    /** 需要登录的接口：未登录抛 401 */
    public static Long requireId(HttpSession session) {
        UserVO user = getUser(session);
        if (user == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return user.getId();
    }

    /** 可选的当前用户ID：匿名返回 null */
    public static Long optionalId(HttpSession session) {
        UserVO user = getUser(session);
        return user == null ? null : user.getId();
    }
}
