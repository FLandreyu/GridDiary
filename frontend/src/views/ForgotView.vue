<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { forgotPassword, resetPassword } from '../api/user'

const router = useRouter()
const step = ref('email') // email -> reset
const loading = ref(false)
const email = ref('')
const code = ref('')
const newPassword = ref('')
const confirm = ref('')

async function sendCode() {
  if (!email.value) return ElMessage.warning('请输入注册邮箱')
  loading.value = true
  try {
    const demoCode = await forgotPassword(email.value)
    // 演示实现：后端直接返回验证码（真实项目会发送到邮箱）
    ElMessage.info('演示环境：验证码为 ' + demoCode + '（真实项目将发送至邮箱）')
    step.value = 'reset'
  } finally {
    loading.value = false
  }
}

async function submitReset() {
  if (!code.value) return ElMessage.warning('请输入验证码')
  if (newPassword.value.length < 6) return ElMessage.warning('新密码至少 6 位')
  if (newPassword.value !== confirm.value) return ElMessage.warning('两次密码不一致')
  loading.value = true
  try {
    await resetPassword({ email: email.value, code: code.value, newPassword: newPassword.value })
    ElMessage.success('密码重置成功，请重新登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-card class="auth-card">
    <h2 class="auth-title">找回密码</h2>
    <template v-if="step === 'email'">
      <el-input v-model="email" placeholder="注册邮箱" clearable style="margin-bottom:16px" />
      <el-button type="primary" style="width:100%" :loading="loading" @click="sendCode">
        发送验证码
      </el-button>
    </template>
    <template v-else>
      <el-input v-model="code" placeholder="6 位验证码" style="margin-bottom:16px" />
      <el-input
        v-model="newPassword"
        type="password"
        placeholder="新密码(≥6位)"
        show-password
        style="margin-bottom:16px"
      />
      <el-input
        v-model="confirm"
        type="password"
        placeholder="确认新密码"
        show-password
        style="margin-bottom:16px"
      />
      <el-button type="primary" style="width:100%" :loading="loading" @click="submitReset">
        重置密码
      </el-button>
    </template>
    <div class="auth-footer">
      <el-link type="primary" @click="router.push('/login')">返回登录</el-link>
    </div>
  </el-card>
</template>
