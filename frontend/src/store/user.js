import { defineStore } from 'pinia'
import { me as fetchMe, login as apiLogin, logout as apiLogout } from '../api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    initialized: false
  }),
  getters: {
    isLogin: (s) => !!s.user
  },
  actions: {
    setUser(u) {
      this.user = u
    },
    /** 应用启动/路由守卫时调用：尝试恢复登录态 */
    async init() {
      try {
        this.user = await fetchMe()
      } catch (_) {
        this.user = null
      }
      this.initialized = true
    },
    async login(form) {
      this.user = await apiLogin(form)
    },
    async logout() {
      try {
        await apiLogout()
      } catch (_) {
        /* 忽略 */
      }
      this.user = null
    }
  }
})
