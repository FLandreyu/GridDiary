import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 统一 axios 实例：withCredentials 携带 Session Cookie
const request = axios.create({
  baseURL: '/',
  timeout: 15000,
  withCredentials: true
})

// 响应拦截：统一处理 {code, message, data} 与 401
request.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) return body // 业务成功，返回整个 Result
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || 'error'))
    }
    return body
  },
  (err) => {
    const status = err.response?.status
    const msg = err.response?.data?.message || err.message || '网络错误'
    if (status === 401) {
      // 初始化登录态的 /me 401 属正常，静默处理
      const url = err.config?.url || ''
      if (!url.endsWith('/me')) {
        ElMessage.error('登录已过期，请重新登录')
        if (router.currentRoute.value.name !== 'login') router.push('/login')
      }
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(err)
  }
)

export default request
