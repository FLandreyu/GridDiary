<script setup>
/**
 * 全站页脚：品牌 + 快捷入口 + 技术栈 + 版权
 * 视觉上延续「无框」风格，只在顶部用一条渐变细线分隔
 */
const year = new Date().getFullYear();

const links = [
  { label: "首页", path: "/" },
  { label: "写日记", path: "/diary/new" },
  { label: "我的日记", path: "/my" },
  { label: "排行榜", path: "/hot" },
  { label: "相册", path: "/gallery" },
  { label: "私信", path: "/message" },
];

const stack = [
  "Vue 3",
  "Vite",
  "Element Plus",
  "Spring Boot",
  "MyBatis",
  "MySQL",
];
</script>

<template>
  <footer class="app-footer">
    <div class="ft-inner">
      <div class="ft-brand">
        <div class="ft-logo">
          <img :src="'/images/logo.svg'" alt="logo" />
          <span>九宫格记忆网</span>
        </div>
        <p>把每一天，装进属于你的九宫格 ✦</p>
      </div>

      <div class="ft-col">
        <div class="ft-title">快捷入口</div>
        <div class="ft-links">
          <span
            v-for="l in links"
            :key="l.path"
            class="ft-link"
            @click="$router.push(l.path)"
            >{{ l.label }}</span
          >
        </div>
      </div>

      <div class="ft-col">
        <div class="ft-title">技术栈</div>
        <div class="ft-tags">
          <span v-for="s in stack" :key="s">{{ s }}</span>
        </div>
      </div>
    </div>

    <div class="ft-bottom">
      <span>© {{ year }} 九宫格记忆网</span>
      <span>Made with ♥ and Vue</span>
    </div>
  </footer>
</template>
