<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { me, updateProfile, changePassword } from "../api/user";
import { uploadImages } from "../api/upload";
import { useUserStore } from "../store/user";

const router = useRouter();
const store = useUserStore();

// 基本资料
const nickname = ref("");
const avatar = ref(""); // 已上传的头像 URL
const avatarPreview = ref("");
const avatarUploading = ref(false);
const profileSaving = ref(false);
const fileInput = ref();

// 修改密码
const oldPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const pwdSaving = ref(false);

const firstChar = () => (nickname.value || "?").charAt(0).toUpperCase();

onMounted(async () => {
  try {
    const u = await me();
    store.setUser(u);
    nickname.value = u.nickname || "";
    avatar.value = u.avatar || "";
    avatarPreview.value = u.avatar || "";
  } catch (_) {
    /* 已提示 */
  }
});

function pickAvatar() {
  fileInput.value?.click();
}

async function onAvatarPick(e) {
  const file = e.target.files?.[0];
  e.target.value = "";
  if (!file) return;
  if (!["image/jpeg", "image/png"].includes(file.type)) {
    return ElMessage.warning("仅支持 jpg / png");
  }
  if (file.size > 5 * 1024 * 1024) {
    return ElMessage.warning("头像不能超过 5MB");
  }
  avatarUploading.value = true;
  try {
    const r = await uploadImages([file]);
    avatar.value = r[0].originalUrl;
    avatarPreview.value = r[0].originalUrl;
    ElMessage.success("头像已上传，点「保存资料」生效");
  } catch (_) {
    /* 已提示 */
  } finally {
    avatarUploading.value = false;
  }
}

async function saveProfile() {
  if (!nickname.value.trim()) return ElMessage.warning("昵称不能为空");
  profileSaving.value = true;
  try {
    const u = await updateProfile({
      nickname: nickname.value.trim(),
      avatar: avatar.value,
    });
    store.setUser(u); // 同步顶栏
    ElMessage.success("资料已保存");
  } catch (_) {
    /* 已提示 */
  } finally {
    profileSaving.value = false;
  }
}

async function savePassword() {
  if (!oldPassword.value) return ElMessage.warning("请输入原密码");
  if (newPassword.value.length < 6) return ElMessage.warning("新密码至少 6 位");
  if (newPassword.value !== confirmPassword.value)
    return ElMessage.warning("两次输入的新密码不一致");
  pwdSaving.value = true;
  try {
    await changePassword({
      oldPassword: oldPassword.value,
      newPassword: newPassword.value,
    });
    ElMessage.success("密码已修改，下次登录请使用新密码");
    oldPassword.value = "";
    newPassword.value = "";
    confirmPassword.value = "";
  } catch (_) {
    /* 已提示 */
  } finally {
    pwdSaving.value = false;
  }
}
</script>

<template>
  <div class="edit-wrap">
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; gap: 12px">
          <el-button link @click="router.back()">← 返回</el-button>
          <b>个人设置</b>
        </div>
      </template>

      <!-- 基本资料 -->
      <h3 class="sec">👤 基本资料</h3>
      <el-form label-width="80px" style="max-width: 460px">
        <el-form-item label="头像">
          <div class="avatar-row">
            <el-avatar :size="72" :src="avatarPreview || undefined">
              {{ firstChar() }}
            </el-avatar>
            <div>
              <el-button
                size="small"
                :loading="avatarUploading"
                @click="pickAvatar"
              >
                更换头像
              </el-button>
              <div class="tip">jpg/png，≤5MB</div>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept="image/jpeg,image/png"
              hidden
              @change="onAvatarPick"
            />
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input
            v-model="nickname"
            maxlength="64"
            show-word-limit
            placeholder="给自己起个好听的昵称"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="profileSaving"
            @click="saveProfile"
          >
            保存资料
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <!-- 修改密码 -->
      <h3 class="sec">🔒 修改密码</h3>
      <el-form label-width="80px" style="max-width: 460px">
        <el-form-item label="原密码">
          <el-input
            v-model="oldPassword"
            type="password"
            show-password
            placeholder="当前登录密码"
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="newPassword"
            type="password"
            show-password
            placeholder="6~50 位新密码"
          />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input
            v-model="confirmPassword"
            type="password"
            show-password
            placeholder="再次输入新密码"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="pwdSaving" @click="savePassword">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.edit-wrap {
  max-width: 640px;
  margin: 0 auto;
}
.sec {
  margin: 0 0 16px;
  font-size: 16px;
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
