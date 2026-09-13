<script setup>
/**
 * 横向列表卡（Mizuki 文章列表风格）
 * 左缩略图 + 右标题/作者/时间/摘要/数据胶囊，用于首页「列表」视图
 */
import { computed, ref } from "vue";
import { timeAgo, toTagList, wordCountText } from "../utils/format";

const props = defineProps({
  diary: { type: Object, required: true },
});

const placeholderOk = ref(true);

const tagList = computed(() => toTagList(props.diary.tags));

function firstChar(nick) {
  return (nick || "?").charAt(0).toUpperCase();
}
</script>

<template>
  <article class="gd-item" @click="$router.push(`/diary/${diary.id}`)">
    <!-- 缩略图 -->
    <div class="item-thumb">
      <el-image
        v-if="diary.cover"
        :src="diary.cover"
        fit="cover"
        lazy
        class="thumb-img"
      >
        <template #error>
          <div class="thumb-ph">
            <img
              v-if="placeholderOk"
              :src="'/images/cover-placeholder.png'"
              class="thumb-img"
              alt=""
              @error="placeholderOk = false"
            />
            <span v-else>📔</span>
          </div>
        </template>
      </el-image>
      <div v-else class="thumb-ph">📔</div>
    </div>

    <!-- 正文信息 -->
    <div class="item-body">
      <h3 class="item-title" :title="diary.title">{{ diary.title }}</h3>

      <div class="item-meta">
        <span
          class="author"
          title="查看个人主页"
          @click.stop="$router.push(`/user/${diary.userId}`)"
        >
          <el-avatar
            :size="18"
            :src="diary.authorAvatar || undefined"
            style="flex-shrink: 0"
          >
            {{ firstChar(diary.authorNickname) }}
          </el-avatar>
          <span>{{ diary.authorNickname || "匿名" }}</span>
        </span>
        <i class="dot">·</i>
        <span>{{ timeAgo(diary.createdAt) }}</span>
        <i v-if="diary.category" class="dot">·</i>
        <span v-if="diary.category">📂 {{ diary.category }}</span>
        <i class="dot">·</i>
        <span>{{ wordCountText(diary.wordCount) }}</span>
      </div>

      <p v-if="diary.content" class="item-excerpt">
        {{ diary.content.replace(/\s+/g, " ").slice(0, 70) }}
      </p>

      <div v-if="tagList.length" class="gd-tags">
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
  </article>
</template>
