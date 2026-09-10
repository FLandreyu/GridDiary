<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { register } from "../api/user";

const router = useRouter();
const formRef = ref();
const form = ref({
  username: "",
  password: "",
  confirm: "",
  email: "",
  nickname: "",
});
const loading = ref(false);
const imgOk = ref(true);
const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    {
      pattern: /^[a-zA-Z0-9_]{3,50}$/,
      message: "3~50 位字母/数字/下划线",
      trigger: "blur",
    },
  ],
  password: [
    {
      required: true,
      min: 6,
      max: 50,
      message: "密码长度 6~50",
      trigger: "blur",
    },
  ],
  confirm: [
    { required: true, message: "请再次输入密码", trigger: "blur" },
    {
      validator: (_r, v, cb) =>
        v === form.value.password ? cb() : cb(new Error("两次密码不一致")),
      trigger: "blur",
    },
  ],
  email: [
    {
      required: true,
      type: "email",
      message: "邮箱格式不正确",
      trigger: "blur",
    },
  ],
  nickname: [{ required: true, message: "请输入昵称", trigger: "blur" }],
};

async function submit() {
  await formRef.value.validate();
  loading.value = true;
  try {
    const { confirm: _confirm, ...req } = form.value;
    await register(req);
    ElMessage.success("注册成功，请登录");
    router.push("/login");
  } catch (_) {
    /* 已提示 */
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <el-card class="auth-card">
    <div class="auth-illus">
      <img
        v-if="imgOk"
        :src="'/images/auth.png'"
        alt=""
        @error="imgOk = false"
      />
      <div v-else class="fallback">🌸</div>
    </div>
    <h2 class="auth-title">注册账号</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="3~50 位字母数字下划线" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          placeholder="6~50 位"
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirm">
        <el-input v-model="form.confirm" type="password" show-password />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="用于登录与找回密码" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          style="width: 100%"
          :loading="loading"
          @click="submit"
        >
          注册
        </el-button>
      </el-form-item>
    </el-form>
    <div class="auth-footer">
      已有账号？<el-link type="primary" @click="router.push('/login')"
        >去登录</el-link
      >
    </div>
  </el-card>
</template>
