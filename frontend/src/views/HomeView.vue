<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { listDiaries } from "../api/diary";
import { doCheckin, todayCheckin } from "../api/checkin";
import { useUserStore } from "../store/user";
import DiaryCard from "../components/DiaryCard.vue";

const store = useUserStore();
const loading = ref(false);
const records = ref([]);
const total = ref(0);
const page = ref(1);
const size = 12;
const keyword = ref("");
const heroOk = ref(true);

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

/** 打卡：成功后弹出今日运势 */
async function onCheckin() {
  checkinLoading.value = true;
  try {
    checkin.value = await doCheckin();
    fortuneVisible.value = true;
    ElMessage.success(`打卡成功 ✧ 今日运势：${checkin.value.level}`);
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

async function load(p = 1) {
  loading.value = true;
  try {
    const res = await listDiaries({
      page: p,
      size,
      keyword: keyword.value ? keyword.value.trim() : undefined,
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

function onSearch() {
  load(1);
}

function onPageChange(p) {
  load(p);
}

onMounted(() => load(1));
</script>

<template>
  <div class="home">
    <!-- Hero 横幅：右侧为每日打卡（未登录时显示 hero.png 插画） -->
    <section class="gd-hero">
      <div class="gd-hero-text">
        <h1>记录今天的小美好 ✦</h1>
        <p>上传照片、写下心情，把回忆装进属于你的九宫格。</p>
      </div>

      <div
        v-if="store.isLogin"
        class="gd-checkin"
        :class="{ done: checkin.checked }"
      >
        <div class="ck-top">
          <span class="ck-icon">{{ checkin.checked ? levelEmoji : "🗓️" }}</span>
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

      <img
        v-else-if="heroOk"
        class="gd-hero-img"
        :src="'/images/hero.png'"
        alt=""
        @error="heroOk = false"
      />
    </section>

    <!-- 顶部：标题 + 搜索 -->
    <div class="toolbar">
      <h2 class="section-title">📷 最新公开日记</h2>
      <el-input
        v-model="keyword"
        class="search"
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

    <!-- 九宫格列表 -->
    <div v-loading="loading">
      <el-row v-if="records.length" :gutter="16">
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

      <el-empty
        v-else-if="!loading"
        description="还没有公开日记，快去写第一篇吧 🖊️"
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
              <i
                class="ft-dot"
                :style="{ background: checkin.luckyColorHex }"
              ></i
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
  </div>
</template>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 18px;
}
.section-title {
  margin: 0;
  font-size: 20px;
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
  .search {
    width: 100%;
  }
}
</style>
