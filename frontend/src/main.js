import { createApp } from "vue";
import { createPinia } from "pinia";
// 组件与指令样式由 unplugin-vue-components 按需注入（见 vite.config.js），
// 这里只手动引入基础变量、暗色变量，以及「函数式调用」组件的样式
// （ElMessage / ElMessageBox / v-loading 不是模板标签，插件无法自动带样式）
import "element-plus/theme-chalk/base.css";
import "element-plus/theme-chalk/dark/css-vars.css";
import "element-plus/theme-chalk/el-overlay.css";
import "element-plus/theme-chalk/el-loading.css";
import "element-plus/theme-chalk/el-message.css";
import "element-plus/theme-chalk/el-message-box.css";
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
app.mount("#app");
