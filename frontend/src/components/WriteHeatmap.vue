<script setup>
/**
 * 写作热力图（GitHub 贡献图风格）
 * - userId 省略 → 自己的写作（含私密日记，需登录）
 * - userId 有值 → 该用户的公开写作（匿名可看，用于他人主页）
 * - compact 模式：省略月份/星期标签、方格宽度自适应，适合 300px 侧边栏
 * 数据自取；暴露 reload() 供父级在发日记后刷新
 */
import { computed, onMounted, ref, watch } from "vue";
import { getHeatmap } from "../api/stats";

const props = defineProps({
  /** 为空表示"我"（走 Session，含私密日记） */
  userId: { type: Number, default: null },
  /** 统计最近多少天（后端会夹在 30 ~ 730 之间） */
  days: { type: Number, default: 365 },
  /** 侧边栏紧凑模式 */
  compact: { type: Boolean, default: false },
  title: { type: String, default: "写作热力图" },
});

const loading = ref(true);
const data = ref(null);

async function load() {
  loading.value = true;
  try {
    data.value = await getHeatmap({
      userId: props.userId ?? undefined,
      days: props.days,
    });
  } catch (_) {
    data.value = null; // 未登录时静默隐藏
  } finally {
    loading.value = false;
  }
}

watch(() => [props.userId, props.days], load);
onMounted(load);
defineExpose({ reload: load });

/* ------------- 日期工具（一律用本地时间，避免 toISOString 的时区偏移） ------------- */
const pad = (n) => String(n).padStart(2, "0");
const keyOf = (d) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const parse = (s) => {
  const [y, m, d] = String(s).split("-").map(Number);
  return new Date(y, m - 1, d);
};
const addDays = (d, n) =>
  new Date(d.getFullYear(), d.getMonth(), d.getDate() + n);
/** 周一为一周第一天：0=周一 … 6=周日 */
const weekdayIndex = (d) => (d.getDay() + 6) % 7;

const WEEKDAYS = ["一", "二", "三", "四", "五", "六", "日"];

/** 稀疏点集 → 完整日历网格（列=周，行=周一~周日） */
const columns = computed(() => {
  if (!data.value?.from || !data.value?.to) return [];
  const map = new Map();
  for (const p of data.value.points || []) map.set(p.date, p);

  const start = parse(data.value.from);
  const end = parse(data.value.to);
  // 起点回退到所在周的周一，保证每列都是完整 7 天
  const gridStart = addDays(start, -weekdayIndex(start));
  const totalDays = Math.round((end - gridStart) / 86400000) + 1;
  const cols = Math.ceil(totalDays / 7);

  const out = [];
  for (let c = 0; c < cols; c++) {
    const days = [];
    const first = addDays(gridStart, c * 7);
    for (let r = 0; r < 7; r++) {
      const date = addDays(first, r);
      const k = keyOf(date);
      const hit = map.get(k);
      const future = date > end;
      days.push({
        key: k,
        future,
        count: hit?.count || 0,
        words: hit?.words || 0,
        tip: future
          ? ""
          : hit
            ? `${k} · ${hit.count} 篇 · ${hit.words} 字`
            : `${k} · 无记录`,
      });
    }
    out.push({ days, month: first.getMonth() });
  }
  return out;
});

/** 月份标签：某列所属月份与上一列不同时打标，且与上一个标签至少隔 3 列 */
const monthLabels = computed(() => {
  const cols = columns.value;
  const labels = new Map();
  let last = -99;
  cols.forEach((col, i) => {
    const changed = i === 0 || col.month !== cols[i - 1].month;
    if (changed && i - last >= 3) {
      labels.set(i, `${col.month + 1}月`);
      last = i;
    }
  });
  return labels;
});

/** 色阶：0 无 / 1 / 2 / 3 / 4（4 篇及以上） */
const level = (cell) => (cell.future ? 0 : Math.min(cell.count, 4));

const hasData = computed(() => (data.value?.totalCount ?? 0) > 0);
</script>

<template>
  <div class="gd-panel gd-heatmap" :class="{ 'is-compact': compact }">
    <div class="gd-panel-title">{{ compact ? "🖋️" : "🔥" }} {{ title }}</div>

    <div v-if="loading" class="hm-skeleton" />
    <template v-else-if="data">
      <div class="hm-summary">
        <span
          >共 <b>{{ data.totalCount }}</b> 篇</span
        >
        <span
          ><b>{{ data.totalWords }}</b> 字</span
        >
        <span
          ><b>{{ data.activeDays }}</b> 天有写作</span
        >
        <span v-if="!compact" class="hm-extra"
          >最长连续 <b>{{ data.maxStreak }}</b> 天</span
        >
        <span v-if="data.currentStreak > 0" class="hm-streak"
          >🔥 连续 {{ data.currentStreak }} 天</span
        >
      </div>

      <div class="hm-scroll">
        <div class="hm-grid">
          <!-- 星期标签列 -->
          <template v-if="!compact">
            <i class="hm-corner" />
            <i
              v-for="(w, i) in WEEKDAYS"
              :key="w"
              class="hm-wday"
              :class="{ hide: i % 2 === 1 }"
              >{{ w }}</i
            >
          </template>

          <!-- 每周一列：月份标签 + 7 天方格 -->
          <template v-for="(col, ci) in columns" :key="ci">
            <i v-if="!compact" class="hm-month">{{
              monthLabels.get(ci) || ""
            }}</i>
            <i
              v-for="cell in col.days"
              :key="cell.key"
              class="hm-cell"
              :class="[`lv${level(cell)}`, { future: cell.future }]"
              :title="cell.tip || undefined"
            />
          </template>
        </div>
      </div>

      <div class="hm-foot">
        <span class="hm-legend">
          少
          <i
            v-for="l in [0, 1, 2, 3, 4]"
            :key="l"
            class="hm-cell"
            :class="`lv${l}`"
          />
          多
        </span>
        <span v-if="!hasData" class="hm-empty"
          >还没有写作记录，今天开始吧 ✨</span
        >
      </div>
    </template>
  </div>
</template>

<style scoped>
.gd-heatmap {
  --hm-gap: 2px;
  --hm-radius: 2.5px;
}

/* ---------------- 顶部汇总 ---------------- */
.hm-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 14px;
  margin-bottom: 10px;
  font-size: 12.5px;
  color: var(--gd-text-sub);
}
.hm-summary b {
  color: var(--gd-text);
  font-size: 13.5px;
}
.hm-streak {
  color: var(--gd-primary-2);
  font-weight: 600;
}
.gd-heatmap.is-compact .hm-summary {
  gap: 4px 10px;
  font-size: 12px;
}

/* ---------------- 网格 ----------------
   列宽用 minmax(0,1fr) 自适应：永远刚好填满容器，不产生横向滚动条；
   配合 max-width 限制最宽处，避免超宽屏上格子过大 */
.hm-scroll {
  /* 右侧留一点余量：最后一个月份标签（如 12月）比格子宽，会往右溢出一两像素，
     不留余量就会出现横向滚动条 */
  padding-right: 6px;
  padding-bottom: 1px;
  overflow-x: auto;
}
/* 卡片内的滚动条收细，别用全局那根 10px 渐变粗条 */
.hm-scroll::-webkit-scrollbar {
  height: 6px;
}
.hm-scroll::-webkit-scrollbar-thumb {
  border: none;
  border-radius: 999px;
  background: var(--gd-border);
}
.hm-grid {
  display: grid;
  grid-auto-flow: column; /* 按列填充：每列 = 一行（周一到周日） */
  grid-template-columns: 15px; /* 星期标签列 */
  grid-template-rows: 14px repeat(7, auto); /* 首行放月份标签 */
  /* 等分剩余宽度自适应；设最小 8px 保证窄屏上格子仍然可读（不足时横向滚动） */
  grid-auto-columns: minmax(8px, 1fr);
  gap: var(--hm-gap);
  width: 100%;
  max-width: 860px;
}
.hm-cell {
  width: 100%;
  height: auto;
  aspect-ratio: 1;
  border-radius: var(--hm-radius);
  background: var(--gd-heat-0);
  transition:
    transform 0.12s ease,
    box-shadow 0.12s ease;
}
.hm-cell:hover {
  transform: scale(1.25);
  box-shadow: 0 0 0 1px var(--gd-text);
  position: relative;
  z-index: 2;
}
/* 窗口之外的将来日期：透明占位 */
.hm-cell.future {
  background: transparent;
}
.lv1 {
  background: var(--gd-heat-1);
}
.lv2 {
  background: var(--gd-heat-2);
}
.lv3 {
  background: var(--gd-heat-3);
}
.lv4 {
  background: var(--gd-heat-4);
}

.hm-wday {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 9px;
  font-style: normal;
  color: var(--gd-text-sub);
}
.hm-wday.hide {
  visibility: hidden;
}
.hm-month {
  font-size: 10px;
  line-height: 14px;
  font-style: normal;
  color: var(--gd-text-sub);
  white-space: nowrap;
}

/* ---------------- 底部图例 ---------------- */
.hm-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 9px;
  font-size: 11.5px;
  color: var(--gd-text-sub);
}
.hm-legend {
  display: flex;
  align-items: center;
  gap: 3px;
  flex-shrink: 0;
}
.hm-legend .hm-cell {
  width: 11px;
  height: 11px;
  aspect-ratio: auto;
  border-radius: 3px;
}
.hm-legend .hm-cell:hover {
  transform: none;
  box-shadow: none;
}
.hm-empty {
  text-align: right;
}

/* ---------------- 紧凑模式（侧边栏） ---------------- */
.gd-heatmap.is-compact {
  --hm-gap: 2px;
  --hm-radius: 2px;
}
.gd-heatmap.is-compact .hm-grid {
  grid-template-columns: none;
  grid-template-rows: repeat(7, auto); /* 无月份行 */
  max-width: none;
}
.gd-heatmap.is-compact .hm-cell:hover {
  transform: scale(1.15);
}

/* ---------------- 骨架 ---------------- */
.hm-skeleton {
  height: 96px;
  border-radius: var(--gd-radius-sm);
  background: linear-gradient(
    90deg,
    var(--gd-surface-soft),
    var(--gd-border),
    var(--gd-surface-soft)
  );
  background-size: 200% 100%;
  animation: hm-shimmer 1.2s infinite linear;
}
@keyframes hm-shimmer {
  from {
    background-position: 200% 0;
  }
  to {
    background-position: -200% 0;
  }
}

@media (max-width: 720px) {
  .hm-summary {
    font-size: 12px;
  }
  .hm-empty {
    display: none;
  }
}
</style>
