import request from "./request";

// 把后端 Result{code,message,data} 解包成 data
const payload = (p) => p.data;

export const register = (data) =>
  request.post("/api/user/register", data).then(payload);
export const login = (data) =>
  request.post("/api/user/login", data).then(payload);
export const logout = () => request.post("/api/user/logout");
export const me = () => request.get("/api/user/me").then(payload);
export const forgotPassword = (email) =>
  request.post("/api/user/forgot-password", { email }).then(payload);
export const resetPassword = (data) =>
  request.post("/api/user/reset-password", data).then(payload);
export const getProfile = (id) => request.get(`/api/user/${id}`).then(payload);
/** 某用户的作品统计：{ postCount, likeCount, commentCount, joinDays, createdAt } */
export const getUserStats = (id) =>
  request.get(`/api/user/${id}/stats`).then(payload);
export const updateProfile = (data) =>
  request.put("/api/user/profile", data).then(payload);
export const changePassword = (data) =>
  request.put("/api/user/password", data).then(payload);
