<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const formRef = ref()
const form = ref({ username: '', password: '' })
const loading = ref(false)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await store.login(form.value)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch (_) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-card class="auth-card">
    <h2 class="auth-title">登录九宫格记忆网</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @submit.prevent>
      <el-form-item prop="username">
        <el-input v-model="form.username" placeholder="用户名" clearable />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="密码"
          show-password
          @keyup.enter="submit"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form-item>
    </el-form>
    <div class="auth-footer">
      还没有账号？<el-link type="primary" @click="router.push('/register')">去注册</el-link>
      &nbsp;|&nbsp;<el-link type="primary" @click="router.push('/forgot')">忘记密码</el-link>
    </div>
  </el-card>
</template>
