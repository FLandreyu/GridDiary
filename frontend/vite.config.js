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
});
