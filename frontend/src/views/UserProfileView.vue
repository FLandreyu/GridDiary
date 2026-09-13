<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { listDiaries } from "../api/diary";
import { getProfile, getUserStats } from "../api/user";
import { useUserStore } from "../store/user";
import AppSidebar from "../components/AppSidebar.vue";
import DiaryCard from "../components/DiaryCard.vue";
import TwoColLayout from "../components/TwoColLayout.vue";

const route = useRoute();
const router = useRouter();
const store = useUserStore();

const userId = computed(() => Number(route.params.id));
const user = ref(null);
const stats = ref(null);
const records = ref([]);
const total = ref(0);
const page = ref(1);
const size = 8;
const loading = ref(false);
const notFound = ref(false);

const isSelf = computed(() => store.user && store.user.id === userId.value);

async function load() {
  loading.value = true;
  notFound.value = false;
  try {
    const [u, s, res] = await Promise.all([
      getProfile(userId.value),
      getUserStats(userId.value),
      listDiaries({ userId: userId.value, page: 1, size }),
    ]);
    user.value = u;
    stats.value = s;
    records.value = res.records || [];
    total.value = res.total || 0;
  } catch (e) {
    user.value = null;
    records.value = [];
    notFound.value = true;
  } finally {
    loading.value = false;
  }
}

function goSend() {
  router.push(`/message/chat/${userId.value}`);
}

watch(userId, () => load(), { immediate: true });
onMounted(() => {});
</script>

<template>
  <TwoColLayout>
    <el-card v-loading="loading">
      <template v-if="notFound">
        <el-empty description="该用户不存在" />
      </template>
      <template v-else-if="user">
        <div class="head">
          <el-avatar :size="72" :src="user.avatar || undefined">
            {{ (user.nickname || "?").charAt(0).toUpperCase() }}
          </el-avatar>
          <div class="uinfo">
            <div class="nick">{{ user.nickname }}</div>
            <div class="uname">@{{ user.username }}</div>
            <div class="ustats">
              <span
                ><b>{{ stats?.postCount ?? 0 }}</b> 公开日记</span
              >
              <span
                ><b>{{ stats?.likeCount ?? 0 }}</b> 获赞</span
              >
              <span
                ><b>{{ stats?.commentCount ?? 0 }}</b> 收到评论</span
              >
              <span
                >加入 <b>{{ stats?.joinDays ?? 1 }}</b> 天</span
              >
            </div>
          </div>
          <el-button v-if="isSelf" round @click="router.push('/profile/edit')">
            ⚙️ 编辑资料
          </el-button>
          <el-button
            v-if="store.isLogin && !isSelf"
            type="primary"
            round
            @click="goSend"
          >
            ✉️ 发私信
          </el-button>
        </div>
      </template>
    </el-card>

    <h3 class="sub">TA 的公开日记（{{ total }}）</h3>
    <div v-loading="loading">
      <div v-if="records.length" class="grid">
        <DiaryCard v-for="d in records" :key="d.id" :diary="d" />
      </div>
      <el-empty
        v-else-if="!loading"
        description="TA 还没有公开日记"
        :image-size="60"
      />
    </div>

    <template #aside>
      <AppSidebar />
    </template>
  </TwoColLayout>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  gap: 18px;
}
.uinfo {
  flex: 1;
}
.nick {
  font-size: 20px;
  font-weight: 700;
}
.uname {
  color: var(--gd-text-sub);
  font-size: 13px;
  margin-top: 4px;
}
.sub {
  margin: 18px 0 14px;
  font-size: 17px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}
</style>
