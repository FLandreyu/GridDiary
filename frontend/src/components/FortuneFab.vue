<script setup>
/**
 * 右下角浮动挂件（对齐 Mizuki 的悬浮小挂件）
 * 点击后由父级弹出今日运势弹窗
 */
defineProps({
  emoji: { type: String, default: "🔮" },
  text: { type: String, default: "" },
});

defineEmits(["click"]);
</script>

<template>
  <button
    class="gd-fab"
    title="查看今日运势"
    aria-label="查看今日运势"
    @click="$emit('click')"
  >
    <span class="fab-face">{{ emoji }}</span>
    <span class="fab-text">{{ text }}</span>
  </button>
</template>
