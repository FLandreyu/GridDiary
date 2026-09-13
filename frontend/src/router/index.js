import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "../store/user";

const routes = [
  { path: "/", name: "home", component: () => import("../views/HomeView.vue") },
  {
    path: "/diary/new",
    name: "diary-new",
    component: () => import("../views/DiaryEditView.vue"),
  },
  {
    path: "/diary/edit",
    name: "diary-edit",
    component: () => import("../views/DiaryEditView.vue"),
  },
  {
    path: "/my",
    name: "my-diaries",
    component: () => import("../views/MyDiariesView.vue"),
  },
  {
    path: "/login",
    name: "login",
    component: () => import("../views/LoginView.vue"),
    meta: { public: true },
  },
  {
    path: "/register",
    name: "register",
    component: () => import("../views/RegisterView.vue"),
    meta: { public: true },
  },
  {
    path: "/forgot",
    name: "forgot",
    component: () => import("../views/ForgotView.vue"),
    meta: { public: true },
  },
  {
    path: "/diary/:id",
    name: "diary-detail",
    component: () => import("../views/DiaryDetailView.vue"),
  },
  {
    path: "/hot",
    name: "hot",
    component: () => import("../views/HotView.vue"),
  },
  {
    path: "/gallery",
    name: "gallery",
    component: () => import("../views/GalleryView.vue"),
  },
  {
    path: "/user/:id",
    name: "user-profile",
    component: () => import("../views/UserProfileView.vue"),
  },
  {
    path: "/profile/edit",
    name: "profile-edit",
    component: () => import("../views/ProfileEditView.vue"),
  },
  {
    path: "/message",
    name: "message",
    component: () => import("../views/MessageView.vue"),
  },
  {
    path: "/message/chat/:peerId",
    name: "message-chat",
    component: () => import("../views/ChatView.vue"),
  },
  { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 登录守卫：非 public 页面需要登录；已登录再访问登录/注册页则回首页
router.beforeEach(async (to) => {
  const store = useUserStore();
  if (!store.initialized) await store.init(); // 通过 /api/user/me 初始化登录态
  if (!to.meta.public && !store.isLogin) {
    return { name: "login", query: { redirect: to.fullPath } };
  }
  if (to.meta.public && store.isLogin) {
    return { name: "home" };
  }
  return true;
});

export default router;
