package com.flandreyu.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.flandreyu.entity.Checkin;

/**
 * 每日打卡表 Mapper
 */
public interface CheckinMapper {

    /** 新增打卡记录（返回自增ID） */
    int insert(Checkin checkin);

    /** 查询某用户某天的打卡记录，没有返回 null */
    Checkin findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /** 查询某用户最近的打卡日期（倒序，用于计算连续天数） */
    List<LocalDate> findRecentDates(@Param("userId") Long userId, @Param("limit") int limit);

    /** 某用户累计打卡天数 */
    long countByUser(Long userId);
}
