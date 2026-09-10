package com.flandreyu.service.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.flandreyu.entity.Checkin;
import com.flandreyu.mapper.CheckinMapper;
import com.flandreyu.service.CheckinService;
import com.flandreyu.util.FortuneGenerator;
import com.flandreyu.vo.CheckinVO;

import lombok.RequiredArgsConstructor;

/**
 * 每日打卡 + 今日运势 业务实现
 */
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final CheckinMapper checkinMapper;

    @Override
    public CheckinVO today(long userId) {
        LocalDate today = LocalDate.now();
        return build(userId, today, checkinMapper.findByUserAndDate(userId, today));
    }

    @Override
    public CheckinVO checkin(long userId) {
        LocalDate today = LocalDate.now();
        Checkin record = checkinMapper.findByUserAndDate(userId, today);
        if (record == null) {
            record = FortuneGenerator.generate(userId, today);
            try {
                checkinMapper.insert(record);
            } catch (DuplicateKeyException e) {
                // 极小概率的并发重复打卡：直接读回已存在的记录
                record = checkinMapper.findByUserAndDate(userId, today);
            }
        }
        return build(userId, today, record);
    }

    /** 组装返回：未打卡时只带连续/累计天数 */
    private CheckinVO build(long userId, LocalDate today, Checkin record) {
        CheckinVO vo = new CheckinVO();
        vo.setDate(today.toString());
        vo.setChecked(record != null);
        if (record != null) {
            vo.setLevel(record.getFortuneLevel());
            vo.setStars(record.getFortuneStars() == null ? 3 : record.getFortuneStars());
            vo.setLuckyColor(record.getLuckyColor());
            vo.setLuckyColorHex(record.getLuckyColorHex());
            vo.setLuckyNumber(record.getLuckyNumber() == null ? 0 : record.getLuckyNumber());
            vo.setSuit(split(record.getSuit()));
            vo.setAvoid(split(record.getAvoid()));
            vo.setText(record.getFortuneText());
        }
        vo.setStreak(calcStreak(userId, today, record != null));
        vo.setTotal(checkinMapper.countByUser(userId));
        return vo;
    }

    /**
     * 连续打卡天数：
     * 今天已打卡则从今天往前逐日回溯；今天未打卡则从昨天开始回溯（中断即停）。
     */
    private int calcStreak(long userId, LocalDate today, boolean checkedToday) {
        List<LocalDate> dates = checkinMapper.findRecentDates(userId, 400);
        if (dates.isEmpty()) {
            return 0;
        }
        Set<LocalDate> checkedDates = new HashSet<>(dates);
        LocalDate cursor = checkedToday ? today : today.minusDays(1);
        int streak = 0;
        while (checkedDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
