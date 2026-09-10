import { defineStore } from "pinia";

const KEY = "gd-theme-mode";

/** 应用主题到 <html>：dark 类供 Element Plus 暗色变量生效 */
export function applyTheme(mode) {
  const el = document.documentElement;
  el.classList.toggle("dark", mode === "dark");
  el.dataset.theme = mode;
}

export const useThemeStore = defineStore("theme", {
  state: () => ({
    mode: localStorage.getItem(KEY) || "light",
  }),
  getters: {
    isDark: (s) => s.mode === "dark",
  },
  actions: {
    set(mode) {
      this.mode = mode;
      localStorage.setItem(KEY, mode);
      applyTheme(mode);
    },
    toggle() {
      this.set(this.mode === "dark" ? "light" : "dark");
    },
    init() {
      applyTheme(this.mode);
    },
  },
});
