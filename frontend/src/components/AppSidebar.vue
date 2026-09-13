<script setup>
/**
 * 页面通用右侧栏：站点统计 + 标签云 + 我的日历
 * - 统计自取，首屏由本组件统一请求，页面无需重复实现
 * - 默认插槽可插入页面专属卡片（如首页的「每日打卡」），会排在统计卡之前
 * - 暴露 reload()：刷新统计、标签云与日历（打卡/发日记后调用）
 */
import { onMounted, ref } from "vue";
import { getSiteStats } from "../api/stats";
import DiaryCalendar from "./DiaryCalendar.vue";
import SiteStatsCard from "./SiteStatsCard.vue";
import TagCloudCard from "./TagCloudCard.vue";

const stats = ref(null);
const calendarRef = ref(null);
const tagRef = ref(null);

async function load() {
  try {
    stats.value = await getSiteStats();
  } catch (_) {
    /* 统计失败不影响主流程 */
  }
}

function reload() {
  load();
  tagRef.value?.reload();
  calendarRef.value?.reload();
}

onMounted(load);
defineExpose({ reload });
</script>

<template>
  <slot />
  <SiteStatsCard :stats="stats" />
  <TagCloudCard ref="tagRef" />
  <DiaryCalendar ref="calendarRef" />
</template>
