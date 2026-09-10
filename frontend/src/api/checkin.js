import request from "./request";

const payload = (p) => p.data;

/** 今日打卡状态 + 今日运势（未打卡时 checked=false） */
export const todayCheckin = () =>
  request.get("/api/checkin/today").then(payload);

/** 打卡：生成今日运势（同一天幂等） */
export const doCheckin = () => request.post("/api/checkin").then(payload);
