<script setup>
import { timeAgo } from "../utils/format";

defineProps({
  diary: { type: Object, required: true },
});

function firstChar(nick) {
  return (nick || "?").charAt(0).toUpperCase();
}
</script>

<template>
  <div class="diary-card" @click="$router.push(`/diary/${diary.id}`)">
    <!-- 封面缩略图 -->
    <div class="cover">
      <el-image
        v-if="diary.cover"
        :src="diary.cover"
        fit="cover"
        lazy
        class="cover-img"
      />
      <div v-else class="cover-placeholder">📔</div>
    </div>
    <!-- 标题 + 作者/时间 -->
    <div class="info">
      <div class="title" :title="diary.title">{{ diary.title }}</div>
      <div class="meta">
        <span
          class="author"
          title="查看个人主页"
          @click.stop="$router.push(`/user/${diary.userId}`)"
        >
          <el-avatar
            :size="20"
            :src="diary.authorAvatar || undefined"
            style="flex-shrink: 0"
          >
            {{ firstChar(diary.authorNickname) }}
          </el-avatar>
          <span class="nick">{{ diary.authorNickname || "匿名" }}</span>
        </span>
        <span class="time">{{ timeAgo(diary.createdAt) }}</span>
      </div>
      <div class="like-row">
        <span class="heart" :class="{ active: false }">♥</span>
        <span>{{ diary.likeCount ?? 0 }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.diary-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
  border: 1px solid #ebeef5;
}
.diary-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
}
.cover {
  height: 160px;
  background: #f0f2f5;
  position: relative;
}
.cover-img {
  width: 100%;
  height: 100%;
  display: block;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  background: linear-gradient(135deg, #eef3fb, #e6f7ff);
}
.info {
  padding: 10px 12px 12px;
}
.title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 8px;
}
.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #909399;
  font-size: 12px;
}
.author {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  cursor: pointer;
}
.author:hover .nick {
  color: #409eff;
}
.nick {
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.time {
  flex-shrink: 0;
}
.like-row {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 3px;
}
.heart {
  color: #f56c6c;
}
</style>
