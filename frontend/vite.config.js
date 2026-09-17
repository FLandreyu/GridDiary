import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";

// 开发期把 /api 与 /upload 代理到后端，保持同源，Session Cookie 才能正常工作
export default defineConfig({
  plugins: [
    vue(),
    // Element Plus 按需引入：模板里用到哪个组件/指令，才打包哪个（含对应样式）
    Components({
      resolvers: [
        ElementPlusResolver({ importStyle: "css", directives: true }),
      ],
      dts: false, // JS 项目不生成 d.ts
    }),
  ],
  server: {
    port: 5173,
    open: true,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/upload": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
  build: {
    // 字体分片（woff2）永远不内联成 base64：中文字体按 unicode-range 切成上百个小片，
    // 若被内联进 CSS，首屏会把所有分片一次性下载（实测 CSS 81KB → 375KB）；
    // 保持独立文件后，浏览器只按 unicode-range 取用到的 1~3 个分片
    assetsInlineLimit: (filePath) =>
      filePath.endsWith(".woff2") || filePath.endsWith(".woff")
        ? false
        : undefined,
  },
});
