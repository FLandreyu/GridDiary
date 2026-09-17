import request from "./request";

const payload = (p) => p.data;

/** 站点统计：公开日记数/用户数/总字数/获赞/评论/运行天数 */
export const getSiteStats = () => request.get("/api/stats").then(payload);

/** 当前用户某月日历打点（month=yyyy-MM），返回 [{ date, diary, checkin }] */
export const getCalendarMarks = (month) =>
  request.get("/api/stats/calendar", { params: { month } }).then(payload);

/** 标签云：公开日记使用最多的标签 [{ name, count }] */
export const getTopTags = (limit = 20) =>
  request.get("/api/stats/tags", { params: { limit } }).then(payload);

/**
 * 写作热力图：近 days 天每日篇数/字数 + 活跃天数/连续天数
 * userId 省略 → 自己的（含私密，需登录）；有值 → 该用户的公开日记
 */
export const getHeatmap = ({ userId, days = 365 } = {}) =>
  request.get("/api/stats/heatmap", { params: { userId, days } }).then(payload);
