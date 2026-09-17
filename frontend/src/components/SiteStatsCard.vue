<script setup>
/**
 * 侧边栏「站点统计」卡（对齐 Mizuki 的 Site Statistics）
 * 数据由父级请求后通过 props 传入，避免组件各自重复请求
 */
import { computed } from "vue";
import { timeAgo } from "../utils/format";

const props = defineProps({
  stats: { type: Object, default: null },
});

const rows = computed(() => {
  const s = props.stats;
  if (!s) return [];
  return [
    { k: "📝 公开日记", v: s.postCount ?? 0 },
    { k: "👥 注册用户", v: s.userCount ?? 0 },
    { k: "✍️ 总字数", v: (s.wordCount ?? 0).toLocaleString() },
    { k: "♥ 累计获赞", v: s.likeCount ?? 0 },
    { k: "💬 评论总数", v: s.commentCount ?? 0 },
    { k: "📂 分类数", v: s.categoryCount ?? 0 },
    { k: "🏷️ 标签数", v: s.tagCount ?? 0 },
    { k: "⏳ 运行天数", v: `${s.runDays ?? 0} 天` },
    { k: "🕒 最近更新", v: timeAgo(s.lastActiveAt) || "—" },
  ];
});
</script>

<template>
  <div class="gd-panel">
    <div class="gd-panel-title">📊 站点统计</div>
    <div class="gd-stats">
      <template v-if="stats">
        <div v-for="r in rows" :key="r.k" class="stat-row">
          <span class="k">{{ r.k }}</span>
          <span class="v">{{ r.v }}</span>
        </div>
      </template>
      <div v-else class="stat-row">
        <span class="k">加载中…</span>
        <span class="v">—</span>
      </div>
    </div>
  </div>
</template>
