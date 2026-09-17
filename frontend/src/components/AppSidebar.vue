<script setup>
/**
 * 页面通用右侧栏：站点统计 + 写作热力图 + 标签云 + 我的日历
 * - 统计自取，首屏由本组件统一请求，页面无需重复实现
 * - 默认插槽可插入页面专属卡片（如首页的「每日打卡」），会排在统计卡之前
 * - 暴露 reload()：刷新统计、热力图、标签云与日历（打卡/发日记后调用）
 * - 主栏已经放了完整热力图的页面，传 :heatmap="false" 避免重复
 */
import { onMounted, ref } from "vue";
import { getSiteStats } from "../api/stats";
import { useUserStore } from "../store/user";
import DiaryCalendar from "./DiaryCalendar.vue";
import SiteStatsCard from "./SiteStatsCard.vue";
import TagCloudCard from "./TagCloudCard.vue";
import WriteHeatmap from "./WriteHeatmap.vue";

const props = defineProps({
  /** 是否显示侧栏紧凑版写作热力图 */
  heatmap: { type: Boolean, default: true },
});

const store = useUserStore();
const stats = ref(null);
const calendarRef = ref(null);
const tagRef = ref(null);
const heatmapRef = ref(null);

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
  heatmapRef.value?.reload();
}

onMounted(load);
defineExpose({ reload });
</script>

<template>
  <slot />
  <SiteStatsCard :stats="stats" />
  <WriteHeatmap
    v-if="store.isLogin && heatmap"
    ref="heatmapRef"
    compact
    :days="182"
    title="写作热力图"
  />
  <TagCloudCard ref="tagRef" />
  <DiaryCalendar ref="calendarRef" />
</template>
