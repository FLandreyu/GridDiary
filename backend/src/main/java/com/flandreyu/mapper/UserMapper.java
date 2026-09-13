package com.flandreyu.mapper;

import com.flandreyu.entity.User;
import com.flandreyu.vo.UserStatsVO;

/**
 * 用户表 Mapper
 */
public interface UserMapper {

    /** 新增用户，返回受影响行数 */
    int insert(User user);

    /** 按主键查询 */
    User findById(Long id);

    /** 作品统计（公开日记数 / 获赞 / 收到评论 / 注册天数），单行聚合 */
    UserStatsVO selectStats(Long id);

    /** 按用户名查询（用于唯一校验 / 登录） */
    User findByUsername(String username);

    /** 按邮箱查询（用于唯一校验 / 找回密码） */
    User findByEmail(String email);

    /** 动态更新（昵称/头像/邮箱/密码等按需更新） */
    int update(User user);

    /** 按主键删除 */
    int deleteById(Long id);
}
