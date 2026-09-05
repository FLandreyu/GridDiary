<script setup>
import { onBeforeUnmount, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "./store/user";
import { unreadCount } from "./api/message";

const store = useUserStore();
const router = useRouter();

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
</script>

<template>
  <el-container style="min-height: 100%">
    <el-header class="app-header" height="56px">
      <div class="left">
        <div class="brand" @click="router.push('/')">📔 九宫格记忆网</div>
        <nav v-if="store.isLogin" class="nav">
          <span
            class="nav-item"
            :class="{ active: $route.name === 'home' }"
            @click="router.push('/')"
            >首页</span
          >
          <span
            class="nav-item"
            :class="{ active: $route.name === 'diary-new' }"
            @click="router.push('/diary/new')"
            >写日记</span
          >
          <span
            class="nav-item"
            :class="{ active: $route.name === 'my-diaries' }"
            @click="router.push('/my')"
            >我的日记</span
          >
          <span
            class="nav-item"
            :class="{ active: $route.name === 'hot' }"
            @click="router.push('/hot')"
            >排行榜</span
          >
          <span
            class="nav-item msg-item"
            :class="{ active: $route.path.startsWith('/message') }"
            @click="router.push('/message')"
          >
            <el-badge :value="unread" :max="99" :hidden="unread <= 0">
              私信
            </el-badge>
          </span>
        </nav>
      </div>
      <div>
        <template v-if="store.isLogin">
          <el-tag
            type="success"
            title="查看我的主页"
            @click="router.push(`/user/${store.user?.id}`)"
            style="margin-right: 10px; cursor: pointer"
            >{{ store.user?.nickname }}</el-tag
          >
          <el-button size="small" @click="onLogout">退出登录</el-button>
        </template>
        <template v-else>
          <el-button size="small" @click="router.push('/login')"
            >登录</el-button
          >
          <el-button
            size="small"
            type="primary"
            @click="router.push('/register')"
            >注册</el-button
          >
        </template>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.left {
  display: flex;
  align-items: center;
  gap: 26px;
}
.nav {
  display: flex;
  gap: 18px;
}
.nav-item {
  cursor: pointer;
  color: #606266;
  font-size: 15px;
  position: relative;
  padding: 4px 0;
}
.nav-item:hover {
  color: #409eff;
}
.nav-item.active {
  color: #409eff;
  font-weight: 600;
}
.nav-item.active::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: -2px;
  height: 2px;
  border-radius: 2px;
  background: #409eff;
}
</style>
