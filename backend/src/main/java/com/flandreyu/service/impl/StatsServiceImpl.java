package com.flandreyu.service.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.flandreyu.common.BusinessException;
import com.flandreyu.mapper.CheckinMapper;
import com.flandreyu.mapper.DiaryMapper;
import com.flandreyu.service.StatsService;
import com.flandreyu.util.TagUtil;
import com.flandreyu.vo.CalendarMarkVO;
import com.flandreyu.vo.SiteStatsVO;
import com.flandreyu.vo.TagCountVO;

import lombok.RequiredArgsConstructor;

/**
 * 站点统计 / 日历打点实现
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final DiaryMapper diaryMapper;
    private final CheckinMapper checkinMapper;

    @Override
    public SiteStatsVO siteStats() {
        SiteStatsVO vo = diaryMapper.selectSiteStats();
        if (vo != null) {
            // 标签存在逗号串里，不重复标签数在 Java 侧聚合
            vo.setTagCount(countDistinctTags(diaryMapper.selectPublicTags()));
        }
        return vo;
    }

    @Override
    public List<TagCountVO> topTags(int limit) {
        Map<String, Long> counter = new LinkedHashMap<>();
        for (String tags : diaryMapper.selectPublicTags()) {
            for (String tag : TagUtil.split(tags)) {
                counter.merge(tag, 1L, Long::sum);
            }
        }
        return counter.entrySet().stream()
                .map(e -> {
                    TagCountVO vo = new TagCountVO();
                    vo.setName(e.getKey());
                    vo.setCount(e.getValue());
                    return vo;
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(Math.max(1, limit))
                .toList();
    }

    private long countDistinctTags(List<String> tagRows) {
        Map<String, Boolean> set = new LinkedHashMap<>();
        for (String tags : tagRows) {
            for (String tag : TagUtil.split(tags)) {
                set.put(tag, Boolean.TRUE);
            }
        }
        return set.size();
    }

    @Override
    public List<CalendarMarkVO> calendar(long userId, String month) {
        YearMonth ym = parseMonth(month);
        LocalDate first = ym.atDay(1);
        LocalDate next = ym.plusMonths(1).atDay(1);

        // 用 LinkedHashMap 合并两种标记，保证同一天只有一条记录
        Map<String, CalendarMarkVO> marks = new LinkedHashMap<>();
        for (String date : diaryMapper.selectDiaryDates(userId, first.toString(), next.toString())) {
            mark(marks, date).setDiary(true);
        }
        for (LocalDate date : checkinMapper.findDatesBetween(userId, first, next)) {
            mark(marks, date.toString()).setCheckin(true);
        }
        return new ArrayList<>(marks.values());
    }

    private CalendarMarkVO mark(Map<String, CalendarMarkVO> marks, String date) {
        return marks.computeIfAbsent(date, key -> {
            CalendarMarkVO vo = new CalendarMarkVO();
            vo.setDate(key);
            return vo;
        });
    }

    /** month 为空取当月；格式非法返回 400 */
    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (RuntimeException e) {
            throw new BusinessException(400, "月份格式应为 yyyy-MM");
        }
    }
}
