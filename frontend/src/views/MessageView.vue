<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { conversations } from "../api/message";
import { timeAgo } from "../utils/format";

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
  <div class="msg-wrap">
    <el-card>
      <template #header>
        <div class="hd">
          <b>💬 私信会话</b>
          <el-button size="small" @click="load">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading">
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
        <el-empty
          v-if="!loading && !list.length"
          description="还没有任何私信，去别人的主页打个招呼吧～"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.msg-wrap {
  max-width: 640px;
  margin: 0 auto;
}
.hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
