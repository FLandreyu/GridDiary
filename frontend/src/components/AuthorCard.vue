<script setup>
/**
 * 详情页右侧栏「关于作者」卡
 * - 资料与作品统计各自请求（都是 @PublicApi，匿名也能看）
 * - 是本人 → 「编辑资料」；否则 → 「发私信」
 */
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { getProfile, getUserStats } from "../api/user";
import { useUserStore } from "../store/user";
import { formatTime } from "../utils/format";

const props = defineProps({
  userId: { type: [Number, String], required: true },
  nickname: { type: String, default: "" },
  avatar: { type: String, default: "" },
});

const router = useRouter();
const store = useUserStore();
const user = ref(null);
const stats = ref(null);

const isSelf = computed(
  () => store.user && Number(store.user.id) === Number(props.userId),
);
const nick = computed(() => user.value?.nickname || props.nickname || "匿名");
const firstChar = computed(() => (nick.value || "?").charAt(0).toUpperCase());
const joinDate = computed(() =>
  stats.value?.createdAt ? formatTime(stats.value.createdAt).slice(0, 10) : "",
);

async function load() {
  const id = props.userId;
  if (!id) return;
  try {
    const [u, s] = await Promise.all([getProfile(id), getUserStats(id)]);
    user.value = u;
    stats.value = s;
  } catch (_) {
    /* 作者信息拿不到不影响看日记 */
  }
}

watch(() => props.userId, load);
onMounted(load);
</script>

<template>
  <div class="gd-panel author-card">
    <div class="gd-panel-title">✍️ 关于作者</div>

    <div class="ac-head">
      <el-avatar :size="56" :src="user?.avatar || props.avatar || undefined">
        {{ firstChar }}
      </el-avatar>
      <div class="ac-info">
        <div class="ac-nick" :title="nick">{{ nick }}</div>
        <div class="ac-uname">@{{ user?.username || "..." }}</div>
      </div>
    </div>

    <div class="ac-stats">
      <div>
        <b>{{ stats?.postCount ?? 0 }}</b>
        <span>公开日记</span>
      </div>
      <div>
        <b>{{ stats?.likeCount ?? 0 }}</b>
        <span>获赞</span>
      </div>
      <div>
        <b>{{ stats?.commentCount ?? 0 }}</b>
        <span>收到评论</span>
      </div>
    </div>

    <div class="ac-actions">
      <el-button round size="small" @click="router.push(`/user/${userId}`)">
        👤 看主页
      </el-button>
      <el-button
        v-if="store.isLogin && !isSelf"
        round
        size="small"
        type="primary"
        @click="router.push(`/message/chat/${userId}`)"
      >
        ✉️ 发私信
      </el-button>
      <el-button
        v-else-if="isSelf"
        round
        size="small"
        @click="router.push('/profile/edit')"
      >
        ⚙️ 编辑资料
      </el-button>
    </div>

    <div v-if="joinDate" class="ac-join">
      加入于 {{ joinDate }} · 第 {{ stats?.joinDays ?? 1 }} 天
    </div>
  </div>
</template>
