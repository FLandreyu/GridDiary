import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import "element-plus/theme-chalk/dark/css-vars.css";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import App from "./App.vue";
import router from "./router";
import "./styles.css";
import "./styles/theme.css";
import { useThemeStore } from "./store/theme";

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);
useThemeStore(pinia).init(); // 先应用主题，避免刷新闪白
app.use(router);
app.use(ElementPlus, { locale: zhCn });
app.mount("#app");
