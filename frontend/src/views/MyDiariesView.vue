<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { listMyDiaries, deleteDiary } from "../api/diary";
import DiaryCard from "../components/DiaryCard.vue";
import { useUserStore } from "../store/user";

const store = useUserStore();
const router = useRouter();
const loading = ref(false);
const records = ref([]);
const total = ref(0);
const page = ref(1);
const size = 8;

async function load(p = 1) {
  loading.value = true;
  try {
    const res = await listMyDiaries({ page: p, size });
    records.value = res.records || [];
    total.value = res.total || 0;
    page.value = p;
  } catch (_) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false;
  }
}

function onPage(p) {
  load(p);
}

function goNew() {
  router.push("/diary/new");
}

function goProfile() {
  if (store.user) router.push(`/user/${store.user.id}`);
}

function goEdit(d) {
  router.push({ path: "/diary/edit", query: { id: d.id } });
}

async function onDelete(d) {
  try {
    await ElMessageBox.confirm(
      `确定删除《${d.title}》吗？删除后不可恢复。`,
      "删除确认",
      {
        type: "warning",
        confirmButtonText: "删除",
        cancelButtonText: "取消",
      },
    );
    await deleteDiary(d.id);
    ElMessage.success("已删除");
    load(page.value);
  } catch (_) {
    /* 取消 */
  }
}

onMounted(() => load(1));
</script>

<template>
  <div class="my-wrap">
    <div class="toolbar">
      <h2 class="section-title">🗂️ 我的日记</h2>
      <div>
        <el-button @click="goProfile">我的主页</el-button>
        <el-button type="primary" @click="goNew">＋ 写日记</el-button>
      </div>
    </div>

    <div v-loading="loading">
      <div v-if="records.length" class="grid">
        <div v-for="d in records" :key="d.id" class="cell">
          <DiaryCard :diary="d" />
          <div class="ops">
            <el-tag
              :type="d.isPublic ? 'success' : 'info'"
              size="small"
              class="pub"
            >
              {{ d.isPublic ? "公开" : "仅自己" }}
            </el-tag>
            <div>
              <el-button
                size="small"
                text
                type="primary"
                @click="router.push(`/diary/${d.id}`)"
                >查看</el-button
              >
              <el-button size="small" text type="primary" @click="goEdit(d)"
                >编辑</el-button
              >
              <el-button size="small" text type="danger" @click="onDelete(d)"
                >删除</el-button
              >
            </div>
          </div>
        </div>
      </div>
      <el-empty
        v-else-if="!loading"
        description="还没有日记，点右上角写第一篇吧 🖊️"
      />
    </div>

    <div v-if="total > size" class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<style scoped>
.my-wrap {
  max-width: 1200px;
  margin: 0 auto;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
.section-title {
  margin: 0;
  font-size: 20px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}
.ops {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 6px 0;
}
.pub {
  margin-right: 6px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-bottom: 20px;
}
</style>
