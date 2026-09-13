/**
 * 时间显示工具
 */

/** 相对时间：刚刚 / N 分钟前 / N 小时前 / 昨天 / MM-DD / YYYY-MM-DD */
export function timeAgo(time) {
  if (!time) return "";
  const date = new Date(time);
  if (Number.isNaN(date.getTime())) return "";
  const diff = Date.now() - date.getTime();
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;

  if (diff < minute) return "刚刚";
  if (diff < hour) return Math.floor(diff / minute) + " 分钟前";
  if (diff < day) return Math.floor(diff / hour) + " 小时前";
  if (diff < 2 * day) return "昨天";

  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  if (y === new Date().getFullYear()) return `${m}-${d}`;
  return `${y}-${m}-${d}`;
}

/** 完整时间：YYYY-MM-DD HH:mm */
export function formatTime(time) {
  if (!time) return "";
  const date = new Date(time);
  if (Number.isNaN(date.getTime())) return "";
  const p = (n) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${p(date.getMonth() + 1)}-${p(date.getDate())} ${p(date.getHours())}:${p(date.getMinutes())}`;
}

/** 标签：后端用逗号分隔字符串存，这里拆成数组供页面渲染 */
export function toTagList(tags) {
  if (!tags) return [];
  return String(tags)
    .split(",")
    .map((t) => t.trim())
    .filter(Boolean);
}

/** 字数：0 字显示“空”，否则显示 N 字 */
export function wordCountText(n) {
  return n ? `${n} 字` : "空";
}
