<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { hotDiaries } from "../api/diary";
import AppSidebar from "../components/AppSidebar.vue";
import SkeletonCards from "../components/SkeletonCards.vue";
import TwoColLayout from "../components/TwoColLayout.vue";

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
  <TwoColLayout>
    <h2 class="title">🔥 热门日记排行榜</h2>

    <el-card v-loading="loading && list.length > 0">
      <SkeletonCards v-if="loading && !list.length" type="row" :count="6" />

      <template v-else>
        <el-empty
          v-if="!list.length"
          description="暂时还没有足够多的日记上榜"
        />
        <div
          v-for="(d, i) in list"
          :key="d.id"
          class="row"
          @click="router.push(`/diary/${d.id}`)"
        >
          <span :class="rankClass(i)">{{
            i < 3 ? ["🥇", "🥈", "🥉"][i] : i + 1
          }}</span>
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
          <span class="gd-chip">💬 {{ d.commentCount ?? 0 }}</span>
          <span class="likes">♥ {{ d.likeCount ?? 0 }}</span>
        </div>
      </template>
    </el-card>

    <template #aside>
      <AppSidebar />
    </template>
  </TwoColLayout>
</template>

<style scoped>
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
  border-bottom: 1px solid var(--gd-border);
}
.row:hover {
  background: var(--gd-surface-soft);
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
  background: var(--gd-surface-soft);
  color: var(--gd-text-sub);
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
  background: linear-gradient(
    135deg,
    rgba(124, 108, 255, 0.18),
    rgba(63, 216, 255, 0.16)
  );
  font-size: 26px;
}
.info {
  flex: 1;
  min-width: 0;
}
.d-title {
  font-weight: 600;
  color: var(--gd-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.d-meta {
  font-size: 12px;
  color: var(--gd-text-sub);
  margin-top: 4px;
}
.likes {
  color: var(--gd-heart);
  font-size: 13px;
  flex-shrink: 0;
}
</style>
