package com.flandreyu.service.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.flandreyu.common.BusinessException;
import com.flandreyu.mapper.CheckinMapper;
import com.flandreyu.mapper.DiaryMapper;
import com.flandreyu.service.StatsService;
import com.flandreyu.util.TagUtil;
import com.flandreyu.vo.CalendarMarkVO;
import com.flandreyu.vo.HeatmapPointVO;
import com.flandreyu.vo.HeatmapVO;
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

    @Override
    public HeatmapVO heatmap(long userId, int days, boolean onlyPublic) {
        // 区间：含今天在内的最近 days 天（30 ~ 730 之间取值，避免一次拉太多）
        int span = Math.min(Math.max(days, 30), 730);
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(span - 1L);

        List<HeatmapPointVO> points = diaryMapper.selectHeatmap(
                userId, from.toString(), to.plusDays(1).toString(), onlyPublic);

        HeatmapVO vo = new HeatmapVO();
        vo.setFrom(from.toString());
        vo.setTo(to.toString());
        vo.setPoints(points);

        // 汇总 + 连续天数（只依赖「哪些天有写作」，用 Set 便于判断）
        Set<LocalDate> active = new HashSet<>();
        int totalCount = 0;
        int totalWords = 0;
        for (HeatmapPointVO p : points) {
            active.add(LocalDate.parse(p.getDate()));
            totalCount += p.getCount();
            totalWords += p.getWords();
        }
        vo.setTotalCount(totalCount);
        vo.setTotalWords(totalWords);
        vo.setActiveDays(active.size());
        vo.setCurrentStreak(currentStreak(active, to));
        vo.setMaxStreak(maxStreak(active, from, to));
        return vo;
    }

    /** 当前连续天数：今天没写则从昨天往前算（今天还没写不算断） */
    private int currentStreak(Set<LocalDate> active, LocalDate today) {
        LocalDate cursor = active.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (active.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /** 区间内最长连续天数 */
    private int maxStreak(Set<LocalDate> active, LocalDate from, LocalDate to) {
        int best = 0;
        int cur = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            if (active.contains(d)) {
                cur++;
                best = Math.max(best, cur);
            } else {
                cur = 0;
            }
        }
        return best;
    }
}
