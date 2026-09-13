<script setup>
/**
 * 相册页：把所有「公开日记」里的图片按发布时间铺成瀑布流
 * 点击任意一张 → 进入它所属的日记详情
 */
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { galleryImages } from "../api/diary";
import AppSidebar from "../components/AppSidebar.vue";
import SkeletonCards from "../components/SkeletonCards.vue";
import TwoColLayout from "../components/TwoColLayout.vue";
import { timeAgo } from "../utils/format";

const router = useRouter();
const loading = ref(false);
const list = ref([]);
const total = ref(0);
const page = ref(1);
const size = 24;

async function load(p = 1) {
  loading.value = true;
  try {
    const res = await galleryImages({ page: p, size });
    list.value = res.records || [];
    total.value = res.total || 0;
    page.value = p;
  } catch (_) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false;
  }
}

const firstChar = (nick) => (nick || "?").charAt(0).toUpperCase();

onMounted(() => load(1));
</script>

<template>
  <TwoColLayout>
    <div class="toolbar">
      <h2 class="section-title">🖼️ 相册</h2>
      <span v-if="total" class="gallery-count">共 {{ total }} 张公开图片</span>
    </div>

    <div v-loading="loading && list.length > 0">
      <SkeletonCards v-if="loading && !list.length" type="grid" :count="6" />

      <div v-else-if="list.length" class="gallery-grid">
        <figure
          v-for="img in list"
          :key="img.imageId"
          class="gallery-item"
          @click="router.push(`/diary/${img.diaryId}`)"
        >
          <img
            :src="img.thumbUrl || img.originalUrl"
            :alt="img.diaryTitle"
            loading="lazy"
            decoding="async"
          />
          <figcaption class="gallery-cap">
            <div class="gc-title" :title="img.diaryTitle">
              {{ img.diaryTitle }}
            </div>
            <div class="gc-meta">
              <el-avatar :size="18" :src="img.authorAvatar || undefined">
                {{ firstChar(img.authorNickname) }}
              </el-avatar>
              <span>{{ img.authorNickname || "匿名" }}</span>
              <i>·</i>
              <span>{{ timeAgo(img.createdAt) }}</span>
            </div>
          </figcaption>
        </figure>
      </div>

      <el-empty
        v-else
        description="还没有公开的图片，去写一篇带图的日记吧 📷"
      />
    </div>

    <div v-if="total > size" class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="load"
      />
    </div>

    <template #aside>
      <AppSidebar />
    </template>
  </TwoColLayout>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 18px;
}
.gallery-count {
  font-size: 13px;
  color: var(--gd-text-sub);
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
