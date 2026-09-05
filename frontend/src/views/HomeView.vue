<script setup>
import { onMounted, ref } from "vue";
import { listDiaries } from "../api/diary";
import DiaryCard from "../components/DiaryCard.vue";

const loading = ref(false);
const records = ref([]);
const total = ref(0);
const page = ref(1);
const size = 12;
const keyword = ref("");

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
