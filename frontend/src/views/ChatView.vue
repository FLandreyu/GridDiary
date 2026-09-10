<script setup>
import { nextTick, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { chatWith, sendMessage } from "../api/message";
import { useUserStore } from "../store/user";
import { formatTime } from "../utils/format";

const route = useRoute();
const router = useRouter();
const store = useUserStore();

const peerId = Number(route.params.peerId);
const meId = store.user?.id;

const loading = ref(false);
const sending = ref(false);
const peer = ref(null); // {peerNickname, peerAvatar}
const messages = ref([]);
const text = ref("");
const scrollBox = ref(null);

async function load() {
  loading.value = true;
  try {
    const d = await chatWith(peerId, 200);
    peer.value = d;
    messages.value = d.messages || [];
    scrollToBottom();
  } finally {
    loading.value = false;
  }
}

function scrollToBottom() {
  nextTick(() => {
    const el = scrollBox.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
}

async function send() {
  const content = text.value.trim();
  if (!content) return;
  sending.value = true;
  try {
    await sendMessage(peerId, content);
    text.value = "";
    await load();
  } catch (_) {
    /* 拦截器已提示 */
  } finally {
    sending.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="chat-wrap">
    <el-card v-loading="loading" class="chat-card">
      <template #header>
        <div class="hd">
          <el-button link @click="router.push('/message')"
            >← 返回会话</el-button
          >
          <span class="nick">{{ peer?.peerNickname }}</span>
        </div>
      </template>

      <div ref="scrollBox" class="chat-body">
        <el-empty
          v-if="!loading && !messages.length"
          description="还没有聊天记录，说点什么吧"
          :image-size="50"
        />
        <div
          v-for="m in messages"
          :key="m.id"
          class="msg-row"
          :class="{ mine: m.fromUserId === meId }"
        >
          <div class="bubble">
            <div class="msg-text">{{ m.content }}</div>
            <div class="msg-time">{{ formatTime(m.createdAt) }}</div>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="text"
          type="textarea"
          :rows="2"
          maxlength="1000"
          show-word-limit
          placeholder="输入消息，Enter 发送"
          @keydown.enter.exact.prevent="send"
        />
        <el-button
          type="primary"
          :loading="sending"
          @click="send"
          style="margin-top: 8px"
        >
          发送
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.chat-wrap {
  max-width: 640px;
  margin: 0 auto;
}
.chat-card {
  display: flex;
  flex-direction: column;
}
.hd {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hd .nick {
  font-weight: 700;
}
.chat-body {
  height: 52vh;
  overflow-y: auto;
  padding: 4px 6px;
  background: var(--gd-surface-soft);
  border-radius: var(--gd-radius-sm);
  margin-bottom: 12px;
}
.msg-row {
  display: flex;
  margin-bottom: 10px;
}
.msg-row.mine {
  justify-content: flex-end;
}
.bubble {
  max-width: 78%;
  padding: 8px 12px;
  border-radius: 14px;
  background: var(--gd-surface-solid);
  border: 1px solid var(--gd-border);
  box-shadow: 0 4px 14px rgba(20, 16, 60, 0.14);
}
.mine .bubble {
  background: linear-gradient(
    120deg,
    rgba(124, 108, 255, 0.92),
    rgba(255, 122, 189, 0.88)
  );
  border: none;
  color: #fff;
}
.msg-text {
  word-break: break-word;
  white-space: pre-wrap;
  font-size: 14px;
}
.msg-time {
  font-size: 11px;
  color: var(--gd-text-sub);
  margin-top: 4px;
  text-align: right;
}
.chat-input {
  display: flex;
  flex-direction: column;
}
</style>
