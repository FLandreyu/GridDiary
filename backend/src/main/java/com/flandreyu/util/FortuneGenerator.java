package com.flandreyu.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.flandreyu.entity.Checkin;

/**
 * 今日运势生成器（演示级「伪随机」）：
 * 以 userId + 日期 作为随机种子，保证同一用户同一天生成结果稳定、可复现。
 */
public final class FortuneGenerator {

    private FortuneGenerator() {
    }

    /** 运势等级：名称 / 星级 / 权重 */
    private record Level(String name, int stars, int weight) {
    }

    /** 幸运色：名称 / 色值 */
    private record Color(String name, String hex) {
    }

    private static final Level[] LEVELS = {
            new Level("大吉", 5, 16),
            new Level("中吉", 4, 30),
            new Level("小吉", 3, 34),
            new Level("平", 2, 20)
    };

    private static final Color[] COLORS = {
            new Color("樱花粉", "#ff7abd"),
            new Color("星空紫", "#7c6cff"),
            new Color("天青蓝", "#3fd8ff"),
            new Color("抹茶绿", "#6ee7b7"),
            new Color("蜜桃橙", "#ffb066"),
            new Color("柠檬黄", "#ffe066"),
            new Color("月光银", "#dfe3ff"),
            new Color("珊瑚红", "#ff8f9c")
    };

    /** 宜 */
    private static final String[] SUITS = {
            "写日记", "晒太阳", "听一首喜欢的歌", "和朋友聊聊天", "早点休息",
            "随手拍张照", "散散步", "整理房间", "读几页书", "喝杯热奶茶",
            "给家人打个电话", "尝试一件小事", "记录灵感", "夸夸自己", "做点运动"
    };

    /** 忌 */
    private static final String[] AVOIDS = {
            "熬夜", "emo 到深夜", "纠结太久", "拖延", "暴饮暴食",
            "冲动消费", "和人争吵", "赖床到中午", "反复内耗", "说反话",
            "把情绪都憋着", "刷手机到半夜"
    };

    private static final String[] TEXTS = {
            "把小事记下来，它们会变成未来的糖。",
            "慢一点也没关系，你正在走自己的路。",
            "运气藏在细节里，留意身边的小惊喜。",
            "今天的天空，很适合装进九宫格里。",
            "对自己温柔一点，你已经做得很好了。",
            "想去的地方，先写下来，再慢慢靠近它。",
            "被记录下来的日子，都会闪闪发光。",
            "会有好事情发生的，只要你别先放弃期待。"
    };

    /** 生成某用户某天的打卡运势（不含 id / createdAt，由数据库生成） */
    public static Checkin generate(long userId, LocalDate date) {
        Random random = new Random(userId * 1000003L + date.toEpochDay());

        Level level = pickLevel(random);
        Color color = COLORS[random.nextInt(COLORS.length)];

        Checkin checkin = new Checkin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(date);
        checkin.setFortuneLevel(level.name());
        checkin.setFortuneStars(level.stars());
        checkin.setLuckyColor(color.name());
        checkin.setLuckyColorHex(color.hex());
        checkin.setLuckyNumber(random.nextInt(9) + 1);
        checkin.setSuit(String.join(",", pickDistinct(SUITS, 3, random)));
        checkin.setAvoid(String.join(",", pickDistinct(AVOIDS, 2, random)));
        checkin.setFortuneText(TEXTS[random.nextInt(TEXTS.length)]);
        return checkin;
    }

    /** 按权重抽取运势等级（大吉稀有、小吉常见） */
    private static Level pickLevel(Random random) {
        int total = 0;
        for (Level level : LEVELS) {
            total += level.weight();
        }
        int hit = random.nextInt(total);
        for (Level level : LEVELS) {
            hit -= level.weight();
            if (hit < 0) {
                return level;
            }
        }
        return LEVELS[LEVELS.length - 1];
    }

    /** 从候选池中不重复抽取 count 项 */
    private static List<String> pickDistinct(String[] pool, int count, Random random) {
        List<String> copy = new ArrayList<>(List.of(pool));
        Collections.shuffle(copy, random);
        return copy.subList(0, Math.min(count, copy.size()));
    }
}
