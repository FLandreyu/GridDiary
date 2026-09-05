import request from "./request";

const payload = (p) => p.data;

/** 公开日记分页：{ page, size, userId, keyword } */
export const listDiaries = (params) =>
  request.get("/api/diary", { params }).then(payload);

/** 日记详情 */
export const getDiary = (id) => request.get(`/api/diary/${id}`).then(payload);

/** 我的日记分页 */
export const listMyDiaries = (params) =>
  request.get("/api/diary/mine", { params }).then(payload);

/** 热门排行榜 */
export const hotDiaries = (limit = 10) =>
  request.get("/api/diary/hot", { params: { limit } }).then(payload);
/** 新建日记，返回日记ID */
export const createDiary = (data) =>
  request.post("/api/diary", data).then(payload);

/** 修改日记 */
export const updateDiary = (id, data) =>
  request.put(`/api/diary/${id}`, data).then(payload);

/** 删除日记 */
export const deleteDiary = (id) =>
  request.delete(`/api/diary/${id}`).then(payload);
