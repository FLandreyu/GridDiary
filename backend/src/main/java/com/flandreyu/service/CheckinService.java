package com.flandreyu.service;

import com.flandreyu.vo.CheckinVO;

/**
 * 每日打卡 + 今日运势 业务接口
 */
public interface CheckinService {

    /** 查询今日打卡状态与今日运势（未打卡时只返回连续/累计天数） */
    CheckinVO today(long userId);

    /** 打卡（幂等：同一天重复调用不会重复生成运势） */
    CheckinVO checkin(long userId);
}
