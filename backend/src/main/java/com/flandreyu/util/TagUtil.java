package com.flandreyu.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 标签工具：数据库用逗号分隔的字符串存标签，这里负责与 List 互转。
 */
public final class TagUtil {

    /** 单个标签最大长度 */
    private static final int MAX_LEN = 20;
    /** 最多标签数 */
    private static final int MAX_COUNT = 5;
    private static final String SEP = ",";

    private TagUtil() {
    }

    /**
     * 归一化标签：trim、去空、去重（保持顺序）、截断长度、最多 5 个。
     *
     * @return 逗号分隔字符串；无有效标签返回 null
     */
    public static String join(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag == null) {
                continue;
            }
            String t = tag.trim().replace(SEP, "");
            if (t.isEmpty()) {
                continue;
            }
            if (t.length() > MAX_LEN) {
                t = t.substring(0, MAX_LEN);
            }
            set.add(t);
            if (set.size() >= MAX_COUNT) {
                break;
            }
        }
        return set.isEmpty() ? null : String.join(SEP, set);
    }

    /** 拆分标签字符串为列表（空或 null 返回空列表） */
    public static List<String> split(String tags) {
        List<String> list = new ArrayList<>();
        if (tags == null || tags.isBlank()) {
            return list;
        }
        for (String t : tags.split(SEP)) {
            String v = t.trim();
            if (!v.isEmpty()) {
                list.add(v);
            }
        }
        return list;
    }
}
