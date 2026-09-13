<script setup>
/**
 * 侧边栏日历卡：标记「当天有日记 / 当天已打卡」
 * 数据自取（按月请求），暴露 reload() 供父级在打卡后刷新
 */
import { onMounted, ref, watch } from "vue";
import { getCalendarMarks } from "../api/stats";

const cur = ref(new Date());
/** { 'yyyy-MM-dd': 'diary' | 'checkin' | 'both' } */
const marks = ref({});
/** 月份缓存：yyyy-MM -> marks，翻回看过的月份不再重复请求 */
const cache = new Map();

const pad = (n) => String(n).padStart(2, "0");
const monthKey = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}`;

function toMarks(list) {
  const map = {};
  for (const m of list || []) {
    map[m.date] = m.diary && m.checkin ? "both" : m.diary ? "diary" : "checkin";
  }
  return map;
}

async function load(force = false) {
  const key = monthKey(cur.value);
  if (!force && cache.has(key)) {
    marks.value = cache.get(key);
    return;
  }
  try {
    const map = toMarks(await getCalendarMarks(key));
    cache.set(key, map);
    marks.value = map;
  } catch (_) {
    marks.value = {};
  }
}

const mark = (day) => marks.value[day] || "";

watch(cur, () => load());
onMounted(() => load());

// 打卡 / 发日记后：强制重新拉当月并更新缓存
defineExpose({ reload: () => load(true) });
</script>

<template>
  <div class="gd-panel gd-calendar">
    <div class="gd-panel-title">🗓️ 我的日历</div>
    <el-calendar v-model="cur">
      <template #date-cell="{ data }">
        <div class="cal-cell" :class="{ marked: !!mark(data.day) }">
          <span>{{ data.day.split("-")[2] }}</span>
          <i v-if="mark(data.day)" class="cal-dot" :class="mark(data.day)"></i>
        </div>
      </template>
    </el-calendar>
    <div class="cal-legend">
      <span><i class="cal-dot diary"></i>写日记</span>
      <span><i class="cal-dot checkin"></i>已打卡</span>
    </div>
  </div>
</template>
