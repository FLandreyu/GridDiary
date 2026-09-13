<script setup>
/**
 * 两栏页面骨架（对齐 Mizuki 排版）
 * - 默认插槽：主内容（左列，最小宽度 0，防止长标题撑破栅格）
 * - #banner 插槽：通栏横幅，跨两列显示
 * - #aside  插槽：右侧边栏（sticky 吸顶）；不传 / 内部 v-if 为假时自动降级为单栏
 */
import { computed, useSlots, Comment } from "vue";

defineProps({
  maxWidth: { type: String, default: "1180px" },
});

const slots = useSlots();
// 侧边栏为空时插槽只会产出注释节点，据此判断是否真的需要右列
const hasAside = computed(() =>
  (slots.aside?.() ?? []).some((n) => n.type !== Comment),
);
</script>

<template>
  <div
    class="shell"
    :class="hasAside ? 'with-aside' : 'no-aside'"
    :style="{ maxWidth }"
  >
    <div v-if="$slots.banner" class="shell-banner">
      <slot name="banner" />
    </div>

    <div class="shell-main">
      <slot />
    </div>

    <aside v-if="hasAside" class="shell-aside">
      <slot name="aside" />
    </aside>
  </div>
</template>

<style scoped>
.shell {
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}
.shell.with-aside {
  grid-template-columns: minmax(0, 1fr) 300px;
}
.shell-banner {
  grid-column: 1 / -1;
}
.shell-main {
  min-width: 0;
}
.shell-aside {
  position: sticky;
  top: 76px; /* 顶栏 56px + 间距 */
  display: flex;
  flex-direction: column;
  gap: 16px;
}
@media (max-width: 960px) {
  .shell.with-aside {
    grid-template-columns: minmax(0, 1fr);
  }
  .shell-aside {
    position: static;
  }
}
</style>
