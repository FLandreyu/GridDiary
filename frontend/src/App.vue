<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
// 按需引入后不再有 app.use(ElementPlus, { locale })，改用 ElConfigProvider 提供中文包
import zhCn from "element-plus/es/locale/lang/zh-cn";
import { useUserStore } from "./store/user";
import { useThemeStore } from "./store/theme";
import { unreadCount } from "./api/message";
import AppFooter from "./components/AppFooter.vue";

const store = useUserStore();
const router = useRouter();
const theme = useThemeStore();
const logoOk = ref(true);

/* 顶栏融入 Hero：首页未滚动时透明（无边框/阴影），一滚动就恢复毛玻璃 */
const scrolled = ref(false);
const onScroll = () => {
  scrolled.value = window.scrollY > 24;
};
onMounted(() => {
  window.addEventListener("scroll", onScroll, { passive: true });
  onScroll();
});
onBeforeUnmount(() => window.removeEventListener("scroll", onScroll));

/** 只有首页（有通栏 Hero）且在顶部时才启用透明模式 */
const heroMode = computed(
  () => router.currentRoute.value.path === "/" && !scrolled.value,
);

// 未读私信角标：登录后每 30s 拉取
const unread = ref(0);
let unreadTimer = null;

async function refreshUnread() {
  try {
    unread.value = await unreadCount();
  } catch (_) {
    unread.value = 0;
  }
}
function startUnreadPoll() {
  stopUnreadPoll();
  refreshUnread();
  unreadTimer = setInterval(refreshUnread, 30000);
}
function stopUnreadPoll() {
  if (unreadTimer) {
    clearInterval(unreadTimer);
    unreadTimer = null;
  }
}
watch(
  () => store.isLogin,
  (v) => {
    if (v) startUnreadPoll();
    else {
      stopUnreadPoll();
      unread.value = 0;
    }
  },
  { immediate: true },
);
watch(
  () => router.currentRoute.value.path,
  (p) => {
    if (p.startsWith("/message")) refreshUnread();
  },
);
onBeforeUnmount(stopUnreadPoll);

async function onLogout() {
  try {
    await ElMessageBox.confirm("确定退出登录吗？", "提示", { type: "warning" });
    await store.logout();
    ElMessage.success("已退出登录");
    router.push("/login");
  } catch (_) {
    /* 取消 */
  }
}

/* ---------------- 顶栏搜索（回车→回首页按关键词筛选） ---------------- */
const searchText = ref("");
watch(
  () => router.currentRoute.value.query.keyword,
  (k) => {
    searchText.value = k || "";
  },
  { immediate: true },
);

function onSearch() {
  const kw = searchText.value.trim();
  router.push({ path: "/", query: kw ? { keyword: kw } : {} }).catch(() => {});
}

/* ---------------- 头像下拉菜单（把文字按钮收进菜单，顶栏只留图标） ----------------
   自己用绝对定位实现，不引入 el-dropdown：后者会把 popper 基建拉进主包（+65KB） */
const avatarChar = computed(() => {
  const n = store.user?.nickname || store.user?.username || "?";
  return n.charAt(0).toUpperCase();
});
const userMenu = ref(false);

function onUserCommand(cmd) {
  userMenu.value = false;
  if (cmd === "home") router.push(`/user/${store.user?.id}`);
  else if (cmd === "edit") router.push("/profile/edit");
  else if (cmd === "logout") onLogout();
}

/** 点击菜单外部 / 路由变化时自动收起 */
function onDocClick(e) {
  if (!e.target?.closest?.(".gd-usermenu-wrap")) userMenu.value = false;
}
onMounted(() => document.addEventListener("click", onDocClick));
onBeforeUnmount(() => document.removeEventListener("click", onDocClick));
watch(
  () => router.currentRoute.value.fullPath,
  () => (userMenu.value = false),
);

/* ---------------- 窄屏抽屉菜单（顶栏导航在 <900px 会被隐藏） ---------------- */
const menuVisible = ref(false);
const NAVS = [
  { label: "首页", path: "/", name: "home" },
  { label: "写日记", path: "/diary/new", name: "diary-new" },
  { label: "我的日记", path: "/my", name: "my-diaries" },
  { label: "排行榜", path: "/hot", name: "hot" },
  { label: "相册", path: "/gallery", name: "gallery" },
  { label: "私信", path: "/message", name: "message" },
];

function isActive(n) {
  const cur = router.currentRoute.value;
  return n.name === "message"
    ? cur.path.startsWith("/message")
    : cur.name === n.name;
}

function go(path) {
  menuVisible.value = false;
  router.push(path);
}

function closeAndLogout() {
  menuVisible.value = false;
  onLogout();
}
</script>

<template>
  <el-container style="min-height: 100%">
    <el-header
      class="app-header"
      :class="{ 'hero-mode': heroMode }"
      height="56px"
    >
      <div class="left">
        <div class="brand" @click="router.push('/')">
          <img
            v-if="logoOk"
            :src="'/images/logo.svg'"
            alt="logo"
            @error="logoOk = false"
          />
          <span>九宫格记忆网</span>
        </div>
        <nav v-if="store.isLogin" class="nav">
          <span
            v-for="n in NAVS"
            :key="n.path"
            class="nav-item"
            :class="[n.name, { active: isActive(n) }]"
            @click="go(n.path)"
          >
            <el-badge
              v-if="n.name === 'message'"
              :value="unread"
              :max="99"
              :hidden="unread <= 0"
            >
              {{ n.label }}
            </el-badge>
            <template v-else>{{ n.label }}</template>
          </span>
        </nav>
      </div>
      <div class="right">
        <form class="hd-search" role="search" @submit.prevent="onSearch">
          <span class="hd-search-ico" aria-hidden="true">🔍</span>
          <input
            v-model="searchText"
            class="hd-search-input"
            type="search"
            placeholder="搜索日记…"
            aria-label="搜索日记"
            maxlength="50"
          />
        </form>

        <template v-if="store.isLogin">
          <div class="gd-usermenu-wrap">
            <button
              class="gd-icon-btn gd-avatar-btn"
              :title="store.user?.nickname"
              :aria-label="`账号菜单：${store.user?.nickname || ''}`"
              :aria-expanded="userMenu"
              aria-haspopup="menu"
              @click="userMenu = !userMenu"
            >
              <img
                v-if="store.user?.avatar"
                class="hd-avatar-img"
                :src="store.user.avatar"
                alt=""
              />
              <span v-else class="hd-avatar-char">{{ avatarChar }}</span>
            </button>
            <div v-if="userMenu" class="gd-usermenu" role="menu">
              <button
                class="um-item"
                role="menuitem"
                @click="onUserCommand('home')"
              >
                👤 我的主页
              </button>
              <button
                class="um-item"
                role="menuitem"
                @click="onUserCommand('edit')"
              >
                ⚙️ 编辑资料
              </button>
              <button
                class="um-item danger"
                role="menuitem"
                @click="onUserCommand('logout')"
              >
                🚪 退出登录
              </button>
            </div>
          </div>
        </template>
        <template v-else>
          <button class="gd-mini-btn" @click="router.push('/login')">
            登录
          </button>
          <button class="gd-mini-btn primary" @click="router.push('/register')">
            注册
          </button>
        </template>

        <button
          class="gd-theme-btn"
          :title="theme.isDark ? '切换白天模式' : '切换夜间模式'"
          :aria-label="theme.isDark ? '切换白天模式' : '切换夜间模式'"
          :aria-pressed="theme.isDark"
          @click="theme.toggle()"
        >
          {{ theme.isDark ? "🌙" : "☀️" }}
        </button>
        <button
          v-if="store.isLogin"
          class="gd-menu-btn"
          title="菜单"
          aria-label="打开导航菜单"
          @click="menuVisible = true"
        >
          ☰
        </button>
      </div>
    </el-header>
    <el-main>
      <!-- 语言包：影响分页“共 x 条”、日历星期/月份等文案（ConfigProvider 不产生额外 DOM） -->
      <el-config-provider :locale="zhCn">
        <router-view v-slot="{ Component }">
          <transition name="gd-page" mode="out-in">
            <div :key="$route.fullPath" class="gd-page-box">
              <component :is="Component" />
            </div>
          </transition>
        </router-view>
      </el-config-provider>
    </el-main>
    <AppFooter />
  </el-container>

  <!-- 宽屏隐藏，窄屏（<900px）由 ☰ 打开 -->
  <el-drawer
    v-model="menuVisible"
    direction="rtl"
    size="240px"
    :with-header="false"
    class="gd-drawer"
  >
    <div class="drawer-nav">
      <span
        v-for="n in NAVS"
        :key="n.path"
        class="drawer-item"
        :class="{ active: isActive(n) }"
        @click="go(n.path)"
      >
        <span>{{ n.label }}</span>
        <el-badge
          v-if="n.name === 'message' && unread > 0"
          :value="unread"
          :max="99"
        />
      </span>
    </div>

    <div class="drawer-foot">
      <template v-if="store.isLogin">
        <el-button round @click="go(`/user/${store.user?.id}`)"
          >⚙️ 我的主页</el-button
        >
        <el-button round @click="closeAndLogout">退出登录</el-button>
      </template>
      <template v-else>
        <el-button round @click="go('/login')">登录</el-button>
        <el-button round type="primary" @click="go('/register')"
          >注册</el-button
        >
      </template>
    </div>
  </el-drawer>

  <!-- 回到顶部（与右下角运势挂件竖排成一列） -->
  <el-backtop :right="26" :bottom="88" />
</template>

<style scoped>
.left {
  display: flex;
  align-items: center;
  gap: 26px;
  min-width: 0;
  /* 占满「品牌 → 右侧工具」之间的全部空间 */
  flex: 1;
}
.right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.nav {
  display: flex;
  /* 项间只留 2px：hover 胶囊不会粘在一起 */
  gap: 2px;
  /* 导航接管中间空间，六项均匀分布 → 顶栏中间不再留空 */
  flex: 1;
  justify-content: space-between;
  margin-right: 18px;
}
/* 注意：颜色由 styles/theme.css 统一控制（胶囊 + 渐变），这里只留布局 */
.nav-item {
  cursor: pointer;
  position: relative;
  white-space: nowrap;
}
.nav-item.message {
  display: inline-flex;
  align-items: center;
}

/* 抽屉内部 */
.drawer-nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.drawer-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-radius: 12px;
  cursor: pointer;
  color: var(--gd-text-sub);
  font-size: 14.5px;
  transition: all 0.2s ease;
}
.drawer-item:hover {
  background: var(--gd-surface-soft);
  color: var(--gd-primary);
}
.drawer-item.active {
  color: #fff;
  font-weight: 600;
  background: linear-gradient(120deg, var(--gd-primary), var(--gd-primary-2));
}
.drawer-foot {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--gd-border);
}
.drawer-foot .el-button {
  width: 100%;
  margin-left: 0 !important;
}
</style>
