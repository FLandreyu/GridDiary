<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { hotDiaries } from "../api/diary";

const router = useRouter();
const loading = ref(false);
const list = ref([]);

const rankClass = (i) => (i < 3 ? `rank top${i + 1}` : "rank");

onMounted(async () => {
  loading.value = true;
  try {
    list.value = await hotDiaries(10);
  } catch (_) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="hot-wrap">
    <h2 class="title">🔥 热门日记排行榜</h2>

    <el-card v-loading="loading">
      <el-empty
        v-if="!loading && !list.length"
        description="暂时还没有足够多的日记上榜"
      />
      <div
        v-for="(d, i) in list"
        :key="d.id"
        class="row"
        @click="router.push(`/diary/${d.id}`)"
      >
        <span :class="rankClass(i)">{{ i + 1 }}</span>
        <el-image
          v-if="d.cover"
          :src="d.cover"
          fit="cover"
          class="cover"
          lazy
        />
        <div v-else class="cover placeholder">📔</div>
        <div class="info">
          <div class="d-title">{{ d.title }}</div>
          <div class="d-meta">by {{ d.authorNickname || "匿名" }}</div>
        </div>
        <span class="likes">♥ {{ d.likeCount ?? 0 }}</span>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.hot-wrap {
  max-width: 720px;
  margin: 0 auto;
}
.title {
  font-size: 20px;
  margin-bottom: 16px;
}
.row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 8px;
  cursor: pointer;
  border-bottom: 1px solid #f0f2f5;
}
.row:hover {
  background: #f5f7fa;
}
.row:last-child {
  border-bottom: none;
}
.rank {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #909399;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}
.rank.top1 {
  background: #f56c6c;
  color: #fff;
}
.rank.top2 {
  background: #e6a23c;
  color: #fff;
}
.rank.top3 {
  background: #909399;
  color: #fff;
}
.cover {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  flex-shrink: 0;
}
.cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef3fb, #e6f7ff);
  font-size: 26px;
}
.info {
  flex: 1;
  min-width: 0;
}
.d-title {
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.d-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.likes {
  color: #f56c6c;
  font-size: 13px;
  flex-shrink: 0;
}
</style>
