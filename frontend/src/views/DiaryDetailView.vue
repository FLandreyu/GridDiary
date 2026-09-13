<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { getDiary, deleteDiary } from "../api/diary";
import {
  listComments,
  addComment,
  deleteComment,
  likeDiary,
  unlikeDiary,
  likeStatus,
} from "../api/social";
import { useUserStore } from "../store/user";
import { formatTime, timeAgo, toTagList, wordCountText } from "../utils/format";
import AppSidebar from "../components/AppSidebar.vue";
import AuthorCard from "../components/AuthorCard.vue";
import TwoColLayout from "../components/TwoColLayout.vue";

const route = useRoute();
const router = useRouter();
const store = useUserStore();
const diaryId = Number(route.params.id);

const loading = ref(true);
const diary = ref(null);

// 点赞
const liked = ref(false);
const likeCount = ref(0);
const liking = ref(false);

// 评论
const comments = ref([]);
const commentLoading = ref(false);
const commentText = ref("");
const replyingTo = ref(null);
const submitting = ref(false);

// 图片灯箱
const dialogVisible = ref(false);
const previewIndex = ref(0);
const rotate = ref(0);

const meId = computed(() => store.user?.id);
const isOwner = computed(
  () => diary.value && diary.value.userId === meId.value,
);

const images = computed(() => diary.value?.images || []);
/** 标签列表（后端存逗号串，这里拆成数组） */
const tagList = computed(() => toTagList(diary.value?.tags));
const currentImage = computed(() =>
  previewIndex.value >= 0 && previewIndex.value < images.value.length
    ? images.value[previewIndex.value]
    : null,
);

/** 评论总数（一级 + 回复） */
const commentCount = computed(() =>
  comments.value.reduce(
    (n, c) => n + 1 + (c.replies ? c.replies.length : 0),
    0,
  ),
);

const firstChar = (nick) => (nick || "?").charAt(0).toUpperCase();

onMounted(loadAll);

async function loadAll() {
  loading.value = true;
  try {
    const d = await getDiary(diaryId);
    diary.value = d;
    likeCount.value = d.likeCount || 0;
    loadComments();
    initLike();
  } catch (_) {
    /* 404/403 已由拦截器提示 */
  } finally {
    loading.value = false;
  }
}

async function initLike() {
  if (!meId.value) {
    liked.value = false;
    return;
  }
  try {
    const s = await likeStatus(diaryId);
    liked.value = s.liked;
    likeCount.value = s.likeCount;
  } catch (_) {
    /* ignore */
  }
}

async function toggleLike() {
  if (!meId.value) return router.push("/login");
  liking.value = true;
  try {
    const r = liked.value
      ? await unlikeDiary(diaryId)
      : await likeDiary(diaryId);
    liked.value = r.liked;
    likeCount.value = r.likeCount;
  } finally {
    liking.value = false;
  }
}

// ---------- 评论 ----------
async function loadComments() {
  commentLoading.value = true;
  try {
    comments.value = await listComments(diaryId);
  } finally {
    commentLoading.value = false;
  }
}

function startReply(c) {
  replyingTo.value = c;
}

function cancelReply() {
  replyingTo.value = null;
}

async function submitComment() {
  const content = commentText.value.trim();
  if (!content) return ElMessage.warning("评论内容不能为空");
  submitting.value = true;
  try {
    await addComment(diaryId, {
      content,
      parentId: replyingTo.value ? replyingTo.value.id : null,
    });
    ElMessage.success("评论成功");
    commentText.value = "";
    replyingTo.value = null;
    loadComments();
  } catch (_) {
    /* 已提示 */
  } finally {
    submitting.value = false;
  }
}

function canDelete(c) {
  return c.userId === meId.value || isOwner.value;
}

async function onDeleteComment(c) {
  try {
    await ElMessageBox.confirm("确定删除这条评论吗？", "提示", {
      type: "warning",
    });
    await deleteComment(diaryId, c.id);
    ElMessage.success("已删除");
    loadComments();
  } catch (_) {
    /* 取消 */
  }
}

// ---------- 图片灯箱 ----------
function openPreview(i) {
  previewIndex.value = i;
  rotate.value = 0;
  dialogVisible.value = true;
}

function rot(deg) {
  rotate.value = (rotate.value + deg + 360) % 360;
}

function nextImage(step) {
  previewIndex.value =
    (previewIndex.value + step + images.value.length) % images.value.length;
  rotate.value = 0;
}

function viewOriginal() {
  if (currentImage.value) window.open(currentImage.value.originalUrl, "_blank");
}

// ---------- 日记 编辑/删除（作者） ----------
function goEdit() {
  router.push({ path: "/diary/edit", query: { id: diaryId } });
}

async function onDeleteDiary() {
  try {
    await ElMessageBox.confirm(
      "确定删除这篇日记吗？删除后不可恢复。",
      "删除确认",
      {
        type: "warning",
        confirmButtonText: "删除",
        cancelButtonText: "取消",
      },
    );
    await deleteDiary(diaryId);
    ElMessage.success("已删除");
    router.replace("/my");
  } catch (_) {
    /* 取消 */
  }
}
</script>

<template>
  <TwoColLayout>
    <el-card v-loading="loading">
      <template #header>
        <el-button link @click="router.back()">← 返回</el-button>
        <el-link
          v-if="diary"
          type="primary"
          style="margin-left: 12px"
          @click="router.push('/')"
        >
          首页
        </el-link>
      </template>

      <template v-if="diary">
        <!-- 标题 / 作者 / 时间 -->
        <h1 class="title">{{ diary.title }}</h1>
        <div class="meta">
          <span
            title="查看个人主页"
            @click="router.push(`/user/${diary.userId}`)"
            style="
              display: inline-flex;
              align-items: center;
              gap: 10px;
              cursor: pointer;
            "
          >
            <el-avatar :size="26" :src="diary.authorAvatar || undefined">
              {{ firstChar(diary.authorNickname) }}
            </el-avatar>
            <span class="nick">{{ diary.authorNickname || "匿名" }}</span>
          </span>
          <el-tag v-if="!diary.isPublic" type="info" size="small"
            >仅自己可见</el-tag
          >
          <span class="time">{{ formatTime(diary.createdAt) }}</span>
          <span v-if="isOwner" class="owner-ops">
            <el-button size="small" text type="primary" @click="goEdit"
              >编辑</el-button
            >
            <el-button size="small" text type="danger" @click="onDeleteDiary"
              >删除</el-button
            >
          </span>
        </div>

        <!-- 信息位 -->
        <div class="gd-chips">
          <span class="gd-chip hot">♥ {{ likeCount }}</span>
          <span class="gd-chip">🖼 {{ images.length }} 张图</span>
          <span class="gd-chip">💬 {{ commentCount }} 条评论</span>
          <span v-if="diary.category" class="gd-chip">
            📂 {{ diary.category }}
          </span>
          <span class="gd-chip">✍️ {{ wordCountText(diary.wordCount) }}</span>
          <span class="gd-chip">
            {{ diary.isPublic ? "🌐 公开" : "🔒 仅自己可见" }}
          </span>
        </div>

        <!-- 标签 -->
        <div v-if="tagList.length" class="gd-tags">
          <span
            v-for="t in tagList"
            :key="t"
            class="gd-tag"
            @click="$router.push({ path: '/', query: { tag: t } })"
            >#{{ t }}</span
          >
        </div>

        <!-- 点赞 -->
        <div class="action-bar">
          <el-button
            round
            :type="liked ? 'danger' : 'default'"
            :loading="liking"
            :aria-pressed="liked"
            :aria-label="
              liked
                ? `取消点赞，当前 ${likeCount} 个赞`
                : `点赞，当前 ${likeCount} 个赞`
            "
            @click="toggleLike"
          >
            <template #icon>
              <span :class="['heart', { on: liked }]">{{
                liked ? "♥" : "♡"
              }}</span>
            </template>
            {{ liked ? "已赞" : "点赞" }} · {{ likeCount }}
          </el-button>
        </div>

        <!-- 正文 -->
        <div class="content">{{ diary.content || "（本篇暂无正文）" }}</div>

        <!-- 图片九宫格：点击展开 -->
        <div v-if="images.length" class="gallery">
          <div
            v-for="(im, i) in images"
            :key="i"
            class="g-item"
            @click="openPreview(i)"
          >
            <el-image :src="im.thumbUrl" fit="cover" lazy class="g-img" />
            <div class="g-mask">👁 展开</div>
          </div>
        </div>

        <el-divider />

        <!-- 评论 -->
        <div class="comments">
          <h3 class="c-title">💬 评论（{{ commentCount }}）</h3>

          <div class="c-input">
            <el-input
              v-model="commentText"
              type="textarea"
              :rows="2"
              maxlength="500"
              show-word-limit
              :placeholder="
                replyingTo
                  ? '回复 @' + (replyingTo.authorNickname || '') + '：'
                  : '写下你的评论……'
              "
            />
            <div v-if="replyingTo" class="c-replying">
              回复 @{{ replyingTo.authorNickname }}
              <el-link type="primary" @click="cancelReply">取消</el-link>
            </div>
            <div class="c-submit">
              <el-button
                type="primary"
                size="small"
                :loading="submitting"
                @click="submitComment"
              >
                发表评论
              </el-button>
            </div>
          </div>

          <div v-loading="commentLoading">
            <template v-if="comments.length">
              <!-- 一级评论 -->
              <div v-for="root in comments" :key="root.id" class="c-root">
                <div class="c-item">
                  <el-avatar :size="30" :src="root.authorAvatar || undefined">
                    {{ firstChar(root.authorNickname) }}
                  </el-avatar>
                  <div class="c-main">
                    <div class="c-head">
                      <b>{{ root.authorNickname }}</b>
                      <span class="c-time">{{ timeAgo(root.createdAt) }}</span>
                    </div>
                    <div class="c-text">{{ root.content }}</div>
                    <div class="c-ops">
                      <el-link
                        type="primary"
                        underline="never"
                        @click="startReply(root)"
                      >
                        回复
                      </el-link>
                      <el-link
                        v-if="canDelete(root)"
                        type="danger"
                        underline="never"
                        style="margin-left: 12px"
                        @click="onDeleteComment(root)"
                      >
                        删除
                      </el-link>
                    </div>

                    <!-- 回复列表 -->
                    <div
                      v-if="root.replies && root.replies.length"
                      class="c-replies"
                    >
                      <div
                        v-for="rep in root.replies"
                        :key="rep.id"
                        class="c-reply"
                      >
                        <el-avatar
                          :size="24"
                          :src="rep.authorAvatar || undefined"
                        >
                          {{ firstChar(rep.authorNickname) }}
                        </el-avatar>
                        <div class="c-main">
                          <div class="c-head">
                            <b>{{ rep.authorNickname }}</b>
                            <span class="c-time">{{
                              timeAgo(rep.createdAt)
                            }}</span>
                          </div>
                          <div class="c-text">{{ rep.content }}</div>
                          <div class="c-ops">
                            <el-link
                              type="primary"
                              underline="never"
                              @click="startReply(rep)"
                            >
                              回复
                            </el-link>
                            <el-link
                              v-if="canDelete(rep)"
                              type="danger"
                              underline="never"
                              style="margin-left: 12px"
                              @click="onDeleteComment(rep)"
                            >
                              删除
                            </el-link>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <el-empty
              v-else-if="!commentLoading"
              description="还没有评论，来抢沙发～"
              :image-size="60"
            />
          </div>
        </div>
      </template>
    </el-card>

    <!-- 图片灯箱：展开 / 旋转 / 原图 -->
    <el-dialog
      v-model="dialogVisible"
      width="720px"
      append-to-body
      :show-close="true"
    >
      <template #header>
        <span v-if="currentImage">
          图片 {{ previewIndex + 1 }} / {{ images.length }}
        </span>
      </template>
      <div class="lightbox">
        <img
          v-if="currentImage"
          :src="currentImage.originalUrl"
          class="lightbox-img"
          :style="{ transform: `rotate(${rotate}deg)` }"
          alt="日记图片"
        />
      </div>
      <template #footer>
        <div class="lightbox-tools">
          <el-button v-if="images.length > 1" @click="nextImage(-1)"
            >◀ 上一张</el-button
          >
          <el-button @click="rot(-90)">↺ 左转</el-button>
          <el-button @click="rot(90)">↻ 右转</el-button>
          <el-button type="primary" @click="viewOriginal"
            >🔍 查看原图</el-button
          >
          <el-button v-if="images.length > 1" @click="nextImage(1)"
            >下一张 ▶</el-button
          >
        </div>
      </template>
    </el-dialog>

    <template #aside>
      <!-- 页面专属卡：作者卡放在统计/标签云/日历之前 -->
      <AppSidebar>
        <AuthorCard
          v-if="diary"
          :user-id="diary.userId"
          :nickname="diary.authorNickname"
          :avatar="diary.authorAvatar"
        />
      </AppSidebar>
    </template>
  </TwoColLayout>
</template>

<style scoped>
.title {
  margin: 0 0 12px;
  font-size: 24px;
}
.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--gd-text-sub);
  font-size: 13px;
  margin-bottom: 14px;
}
.nick {
  font-weight: 600;
  color: var(--gd-text);
}
.time {
  color: var(--gd-text-sub);
}
.owner-ops {
  margin-left: auto;
}
.action-bar {
  margin-bottom: 14px;
}
.heart {
  font-size: 16px;
  margin-right: 4px;
}
.heart.on {
  color: #fff;
}

.content {
  white-space: pre-wrap;
  line-height: 1.8;
  font-size: 15px;
  color: var(--gd-text);
  margin-bottom: 18px;
}

.gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.g-item {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: zoom-in;
  border: 1px solid var(--gd-border);
}
.g-img {
  width: 100%;
  height: 100%;
  display: block;
}
.g-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.15s;
}
.g-item:hover .g-mask {
  opacity: 1;
}

.comments {
  margin-top: 4px;
}
.c-title {
  margin: 0 0 12px;
  font-size: 17px;
}
.c-input {
  margin-bottom: 18px;
}
.c-replying {
  color: var(--gd-primary);
  font-size: 13px;
  margin-top: 6px;
}
.c-submit {
  text-align: right;
  margin-top: 8px;
}
.c-root {
  margin-bottom: 16px;
}
.c-item {
  display: flex;
  gap: 10px;
}
.c-replies {
  margin-top: 12px;
  padding-left: 40px;
  border-left: 2px solid var(--gd-border);
}
.c-reply {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
.c-main {
  flex: 1;
  min-width: 0;
}
.c-head {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.c-time {
  color: var(--gd-text-sub);
  font-size: 12px;
}
.c-text {
  color: var(--gd-text);
  font-size: 14px;
  margin: 4px 0 2px;
  line-height: 1.6;
}
.c-ops {
  font-size: 12px;
}

.lightbox {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 320px;
  background: var(--gd-surface-soft);
  border-radius: var(--gd-radius-sm);
  overflow: hidden;
}
.lightbox-img {
  max-width: 100%;
  max-height: 60vh;
  transition: transform 0.25s ease;
}
.lightbox-tools {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
