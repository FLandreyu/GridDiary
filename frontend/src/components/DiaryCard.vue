<script setup>
import { computed, ref } from "vue";
import { timeAgo, toTagList, wordCountText } from "../utils/format";

const props = defineProps({
  diary: { type: Object, required: true },
});

const placeholderOk = ref(true);

/** 卡片空间有限，只展示前 2 个标签 */
const tagList = computed(() => toTagList(props.diary.tags).slice(0, 2));

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
      >
        <template #error>
          <div class="cover-placeholder">
            <img
              v-if="placeholderOk"
              :src="'/images/cover-placeholder.png'"
              class="cover-img"
              alt=""
              @error="placeholderOk = false"
            />
            <span v-else>📔</span>
          </div>
        </template>
      </el-image>
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
        <span class="gd-words">{{ wordCountText(diary.wordCount) }}</span>
      </div>

      <!-- 分类 / 标签（Mizuki 卡片 meta 行） -->
      <div class="gd-tags">
        <span v-if="diary.category" class="gd-cat"
          >📂 {{ diary.category }}</span
        >
        <span
          v-for="t in tagList"
          :key="t"
          class="gd-tag"
          @click.stop="$router.push({ path: '/', query: { tag: t } })"
          >#{{ t }}</span
        >
      </div>

      <div class="gd-chips">
        <span class="gd-chip hot"><i>♥</i>{{ diary.likeCount ?? 0 }}</span>
        <span class="gd-chip"><i>💬</i>{{ diary.commentCount ?? 0 }}</span>
        <span v-if="(diary.imageCount ?? 0) > 0" class="gd-chip">
          <i>🖼</i>{{ diary.imageCount }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.diary-card {
  background: var(--gd-surface-solid);
  border-radius: var(--gd-radius);
  overflow: hidden;
  cursor: pointer;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
  border: 1px solid var(--gd-border);
}
.diary-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--gd-shadow-hover);
}
.cover {
  height: 160px;
  background: var(--gd-surface-soft);
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
  background: linear-gradient(
    135deg,
    rgba(124, 108, 255, 0.18),
    rgba(63, 216, 255, 0.16)
  );
}
.info {
  padding: 10px 12px 12px;
}
.title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gd-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 8px;
}
.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--gd-text-sub);
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
  color: var(--gd-primary);
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
  color: var(--gd-text-sub);
  display: flex;
  align-items: center;
  gap: 3px;
}
.heart {
  color: var(--gd-heart);
}
</style>
