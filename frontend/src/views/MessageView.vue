<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { conversations } from "../api/message";
import { timeAgo } from "../utils/format";
import AppSidebar from "../components/AppSidebar.vue";
import SkeletonCards from "../components/SkeletonCards.vue";
import TwoColLayout from "../components/TwoColLayout.vue";

const router = useRouter();
const loading = ref(false);
const list = ref([]);

async function load() {
  loading.value = true;
  try {
    list.value = await conversations();
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <TwoColLayout>
    <div class="gd-panel">
      <div class="hd">
        <div class="gd-panel-title">💬 私信会话</div>
        <el-button size="small" @click="load">刷新</el-button>
      </div>

      <div v-loading="loading && list.length > 0">
        <SkeletonCards v-if="loading && !list.length" type="row" :count="4" />

        <template v-else-if="list.length">
          <div
            v-for="c in list"
            :key="c.peerId"
            class="row"
            @click="router.push(`/message/chat/${c.peerId}`)"
          >
            <el-avatar :size="44" :src="c.peerAvatar || undefined">
              {{ (c.peerNickname || "?").charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="info">
              <div class="top">
                <span class="nick">{{ c.peerNickname }}</span>
                <span class="time">{{ timeAgo(c.lastTime) }}</span>
              </div>
              <div class="last">{{ c.lastContent || "（无内容）" }}</div>
            </div>
            <el-badge v-if="c.unread > 0" :value="c.unread" :max="99" />
          </div>
        </template>

        <el-empty
          v-else
          description="还没有任何私信，去别人的主页打个招呼吧～"
        />
      </div>
    </div>

    <template #aside>
      <AppSidebar />
    </template>
  </TwoColLayout>
</template>

<style scoped>
.hd .gd-panel-title {
  flex: 1;
  margin-bottom: 0;
}
.hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 6px;
  cursor: pointer;
  border-bottom: 1px solid var(--gd-border);
}
.row:hover {
  background: var(--gd-surface-soft);
}
.row:last-child {
  border-bottom: none;
}
.info {
  flex: 1;
  min-width: 0;
}
.top {
  display: flex;
  justify-content: space-between;
}
.nick {
  font-weight: 600;
}
.time {
  color: var(--gd-text-sub);
  font-size: 12px;
}
.last {
  color: var(--gd-text-sub);
  font-size: 13px;
  margin-top: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
