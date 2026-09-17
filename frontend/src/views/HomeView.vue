<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { listDiaries } from "../api/diary";
import { doCheckin, todayCheckin } from "../api/checkin";
import { useUserStore } from "../store/user";
import AppSidebar from "../components/AppSidebar.vue";
import DiaryCard from "../components/DiaryCard.vue";
import DiaryListItem from "../components/DiaryListItem.vue";
import FortuneFab from "../components/FortuneFab.vue";
import SkeletonCards from "../components/SkeletonCards.vue";
import TwoColLayout from "../components/TwoColLayout.vue";

const store = useUserStore();
const router = useRouter();
const route = useRoute();
const loading = ref(false);
const records = ref([]);
const total = ref(0);
const page = ref(1);
const size = 12;
const keyword = ref(route.query.keyword || "");
const heroOk = ref(true);

/* ---------------- 侧边栏 / 视图 ---------------- */
const sidebarRef = ref(null);
// 视图偏好持久化：列表（Mizuki 风格）/ 网格（九宫格）
const view = ref(localStorage.getItem("gd-view") === "list" ? "list" : "grid");
watch(view, (v) => localStorage.setItem("gd-view", v));

/* ---------------- 每日打卡 + 今日运势 ---------------- */
const checkinLoading = ref(false);
const fortuneVisible = ref(false);
const checkin = ref({
  checked: false,
  date: "",
  streak: 0,
  total: 0,
  level: "",
  stars: 0,
  luckyColor: "",
  luckyColorHex: "#7c6cff",
  luckyNumber: 0,
  suit: [],
  avoid: [],
  text: "",
});
const LEVEL_EMOJI = { 大吉: "🌟", 中吉: "✨", 小吉: "🌈", 平: "☁️" };
const levelEmoji = computed(() => LEVEL_EMOJI[checkin.value.level] || "🔮");

/** 拉取今日打卡状态（已打卡时一并拿到今日运势） */
async function loadCheckin() {
  try {
    checkin.value = await todayCheckin();
  } catch (_) {
    /* 拦截器已提示 */
  }
}

/** 打卡：成功后弹出今日运势，并刷新侧栏（统计 + 日历） */
async function onCheckin() {
  checkinLoading.value = true;
  try {
    checkin.value = await doCheckin();
    fortuneVisible.value = true;
    ElMessage.success(`打卡成功 ✧ 今日运势：${checkin.value.level}`);
    sidebarRef.value?.reload();
  } catch (_) {
    /* 拦截器已提示 */
  } finally {
    checkinLoading.value = false;
  }
}

// 登录态就绪后再拉打卡（刷新时 /me 可能还没返回）
watch(
  () => store.isLogin,
  (v) => v && loadCheckin(),
  { immediate: true },
);

/* ---------------- Hero 入场编排：图片 → 大字淡入 → 小字逐字打出 ----------------
   时序与 theme.css 里的动画对齐：
   0s 图片淡入(0.8s) → 0.7s 大字淡入(0.9s) → 1.55s 小字逐字(85ms/字) → 打完后光标淡出 */
const HERO_SUB = "把每一天，装进属于你的九宫格 ✦";
const typed = ref(0);
const typingDone = ref(false);
let startTimer = null;
let typeTimer = null;
let doneTimer = null;

const TYPE_DELAY = 1550; // 等图片与大字入场完
const TYPE_SPEED = 85; // 每个字的间隔（ms）

function stopHeroIntro() {
  [startTimer, typeTimer, doneTimer].forEach(
    (t) => t && window.clearTimeout(t),
  );
  if (typeTimer) window.clearInterval(typeTimer);
  startTimer = typeTimer = doneTimer = null;
}

function playHeroIntro() {
  stopHeroIntro();
  typed.value = 0;
  typingDone.value = false;
  startTimer = window.setTimeout(() => {
    typeTimer = window.setInterval(() => {
      if (typed.value >= HERO_SUB.length) {
        window.clearInterval(typeTimer);
        typeTimer = null;
        // 打完停一会儿再让光标淡出
        doneTimer = window.setTimeout(() => (typingDone.value = true), 1200);
        return;
      }
      typed.value += 1;
    }, TYPE_SPEED);
  }, TYPE_DELAY);
}

onMounted(() => {
  // 尊重系统「减弱动态效果」：直接显示完整文案，不做逐字动画
  const reduce = window.matchMedia?.(
    "(prefers-reduced-motion: reduce)",
  ).matches;
  if (reduce) {
    typed.value = HERO_SUB.length;
    typingDone.value = true;
  } else {
    playHeroIntro();
  }
});
onBeforeUnmount(stopHeroIntro);

/** 侧边栏/标签云点进来的标签筛选（?tag=xxx） */
const activeTag = computed(() => route.query.tag || "");

function clearTag() {
  router.push({ path: "/", query: {} }).catch(() => {});
}

async function load(p = 1) {
  loading.value = true;
  try {
    const res = await listDiaries({
      page: p,
      size,
      keyword: keyword.value ? keyword.value.trim() : undefined,
      tag: activeTag.value || undefined,
    });
    records.value = res.records || [];
    total.value = res.total || 0;
    page.value = p;
  } catch (_) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false;
  }
}

// 顶栏搜索 / 标签云 / 卡片点击导致的 URL 变化（含浏览器前进后退）：
// 同步本地关键词并重拉第一页
watch(
  () => `${route.query.keyword || ""}|${route.query.tag || ""}`,
  () => {
    keyword.value = route.query.keyword || "";
    load(1);
  },
);

/** 搜索：同步到 URL（与顶栏搜索框共用 ?keyword=）再拉第一页 */
function onSearch() {
  const kw = keyword.value.trim();
  if (kw === (route.query.keyword || "")) {
    load(1);
    return;
  }
  router
    .push({ path: "/", query: { ...route.query, keyword: kw || undefined } })
    .catch(() => {});
}

function onPageChange(p) {
  load(p);
}

onMounted(() => load(1));
</script>

<template>
  <!-- 通栏 Hero：必须放在两栏骨架之外、作为 .el-main 的直接子元素，
       才能靠负 margin 撑满左右并与顶栏/页面背景融合 -->
  <section class="gd-hero-full">
    <div class="hero-bg">
      <img
        v-if="heroOk"
        class="hero-img"
        :src="'/images/hero.png'"
        alt=""
        fetchpriority="high"
        decoding="async"
        @error="heroOk = false"
      />
      <div class="hero-veil"></div>
    </div>
    <div class="hero-inner">
      <h1>九宫格记忆网</h1>
      <!-- 小字：打完的字符 + 闪烁光标；完整文案给读屏器，动画部分对读屏器隐藏 -->
      <p class="hero-sub">
        <span class="sr-only">{{ HERO_SUB }}</span>
        <span aria-hidden="true"
          >{{ HERO_SUB.slice(0, typed)
          }}<i class="hero-caret" :class="{ done: typingDone }"></i
        ></span>
      </p>
    </div>

    <!-- 与下方日记列表交汇处的波浪：
         填充用的是和 body 同一套「fixed 背景」，所以下沿与页面背景无缝；
         形状靠 SVG data-URI 做 mask，可横向无缝平铺 + 缓慢漂移 -->
    <div class="hero-wave" aria-hidden="true">
      <i class="wv wv-back"></i>
      <i class="wv wv-front"></i>
    </div>
  </section>

  <TwoColLayout>
    <!-- 顶部：标题 + 搜索 + 视图切换 -->
    <div class="toolbar">
      <h2 class="section-title">
        {{ activeTag ? `🏷️ 标签：${activeTag}` : "📷 最新公开日记" }}
        <button v-if="activeTag" class="tag-clear" @click="clearTag">
          清除筛选 ✕
        </button>
      </h2>
      <div class="toolbar-right">
        <el-radio-group v-model="view" size="small" class="view-switch">
          <el-radio-button value="list">列表</el-radio-button>
          <el-radio-button value="grid">网格</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="keyword"
          class="search gd-search-narrow"
          placeholder="搜索标题 / 正文关键词"
          clearable
          @keyup.enter="onSearch"
          @clear="onSearch"
        >
          <template #append>
            <el-button @click="onSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 日记列表：列表视图（Mizuki 风格）/ 网格视图（九宫格） -->
    <div v-loading="loading && records.length > 0">
      <!-- 首屏骨架屏，避免白屏跳动 -->
      <SkeletonCards
        v-if="loading && !records.length"
        :type="view === 'list' ? 'list' : 'grid'"
        :count="view === 'list' ? 4 : 6"
      />

      <template v-else-if="records.length">
        <div v-if="view === 'list'">
          <DiaryListItem v-for="d in records" :key="d.id" :diary="d" />
        </div>

        <el-row v-else :gutter="16">
          <el-col
            v-for="d in records"
            :key="d.id"
            :xs="12"
            :sm="8"
            :md="8"
            :lg="6"
            style="margin-bottom: 16px"
          >
            <DiaryCard :diary="d" />
          </el-col>
        </el-row>
      </template>

      <el-empty
        v-else
        :description="
          activeTag
            ? `没有带 #${activeTag} 标签的公开日记`
            : '还没有公开日记，快去写第一篇吧 🖊️'
        "
      />
    </div>

    <!-- 分页 -->
    <div v-if="total > size" class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
      />
    </div>

    <!-- 右：侧边栏（打卡 / 站点统计 / 日历） -->
    <template #aside>
      <AppSidebar ref="sidebarRef">
        <div
          v-if="store.isLogin"
          class="gd-checkin"
          :class="{ done: checkin.checked }"
        >
          <div class="ck-top">
            <span class="ck-icon">{{
              checkin.checked ? levelEmoji : "🗓️"
            }}</span>
            <div class="ck-info">
              <div class="ck-title">每日打卡</div>
              <div class="ck-sub">
                {{
                  checkin.checked
                    ? `今日运势 · ${checkin.level}`
                    : "今天还没有打卡哦"
                }}
              </div>
            </div>
          </div>
          <div class="ck-streak">
            🔥 连续 <b>{{ checkin.streak }}</b> 天 · 累计
            <b>{{ checkin.total }}</b> 天
          </div>
          <el-button
            v-if="!checkin.checked"
            type="primary"
            round
            class="ck-btn"
            :loading="checkinLoading"
            @click="onCheckin"
            >✧ 立即打卡 ✧</el-button
          >
          <el-button
            v-else
            round
            plain
            class="ck-btn"
            @click="fortuneVisible = true"
            >🔮 查看今日运势</el-button
          >
        </div>
      </AppSidebar>
    </template>
  </TwoColLayout>

  <!-- 右下角浮动挂件：查看今日运势 -->
  <FortuneFab
    v-if="store.isLogin && checkin.checked"
    :emoji="levelEmoji"
    :text="`今日运势 · ${checkin.level}`"
    @click="fortuneVisible = true"
  />

  <!-- 今日运势 -->
  <el-dialog
    v-model="fortuneVisible"
    width="400px"
    align-center
    class="gd-fortune-dialog"
    :show-close="false"
  >
    <div class="gd-fortune">
      <div class="ft-date">{{ checkin.date }} · 今日运势</div>
      <span class="ft-emoji">{{ levelEmoji }}</span>
      <div class="ft-level">{{ checkin.level }}</div>
      <div class="ft-stars">
        <span>{{ "★".repeat(checkin.stars) }}</span
        ><span class="dim">{{ "★".repeat(5 - checkin.stars) }}</span>
      </div>

      <div class="ft-grid">
        <div class="ft-cell">
          <div class="ft-label">幸运色</div>
          <div class="ft-value">
            <i class="ft-dot" :style="{ background: checkin.luckyColorHex }"></i
            >{{ checkin.luckyColor }}
          </div>
        </div>
        <div class="ft-cell">
          <div class="ft-label">幸运数字</div>
          <div class="ft-value">{{ checkin.luckyNumber }}</div>
        </div>
        <div class="ft-cell">
          <div class="ft-label">连续打卡</div>
          <div class="ft-value">{{ checkin.streak }} 天</div>
        </div>
      </div>

      <div class="ft-lists">
        <div class="ft-list suit">
          <div class="ft-label">宜</div>
          <div class="ft-tags">
            <span v-for="s in checkin.suit" :key="s">{{ s }}</span>
          </div>
        </div>
        <div class="ft-list avoid">
          <div class="ft-label">忌</div>
          <div class="ft-tags">
            <span v-for="a in checkin.avoid" :key="a">{{ a }}</span>
          </div>
        </div>
      </div>

      <p class="ft-text">「{{ checkin.text }}」</p>
      <el-button
        type="primary"
        round
        class="ft-ok"
        @click="fortuneVisible = false"
        >收下这份好运 ✧</el-button
      >
    </div>
  </el-dialog>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 18px;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.section-title {
  margin: 0;
  font-size: 20px;
}
.tag-clear {
  margin-left: 10px;
  padding: 3px 12px;
  font-size: 12.5px;
  font-weight: 400;
  font-family: inherit;
  border-radius: 999px;
  cursor: pointer;
  color: var(--gd-text-sub);
  background: var(--gd-surface-soft);
  border: 1px solid var(--gd-border);
  transition: all 0.2s ease;
}
.tag-clear:hover {
  color: var(--gd-primary);
  border-color: var(--gd-primary);
}
.search {
  width: 320px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-bottom: 20px;
}
@media (max-width: 768px) {
  .toolbar-right {
    width: 100%;
    flex-wrap: wrap;
  }
  .search {
    width: 100%;
  }
}
</style>
