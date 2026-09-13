<script setup>
/**
 * 侧边栏「标签云」卡（对齐 Mizuki 的 Tags）
 * - 字号随使用次数放大，形成云朵层次
 * - 点击标签 → 跳首页并按该标签筛选；再点一次取消筛选
 */
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getTopTags } from "../api/stats";

const route = useRoute();
const router = useRouter();
const tags = ref([]);

async function load() {
  try {
    tags.value = await getTopTags(20);
  } catch (_) {
    tags.value = [];
  }
}

const sizeClass = (c) => (c >= 3 ? "lg" : c === 2 ? "md" : "sm");

function pick(name) {
  const isActive = route.query.tag === name;
  router
    .push({ path: "/", query: isActive ? {} : { tag: name } })
    .catch(() => {});
}

onMounted(load);
defineExpose({ reload: load });
</script>

<template>
  <div class="gd-panel">
    <div class="gd-panel-title">🏷️ 标签云</div>
    <div v-if="tags.length" class="gd-tagcloud">
      <span
        v-for="t in tags"
        :key="t.name"
        class="tag-cloud-item"
        :class="[sizeClass(t.count), { active: route.query.tag === t.name }]"
        :title="`${t.count} 篇日记`"
        @click="pick(t.name)"
      >
        {{ t.name }}<i>{{ t.count }}</i>
      </span>
    </div>
    <div v-else class="tag-cloud-empty">还没有标签，写日记时加几个试试～</div>
  </div>
</template>
