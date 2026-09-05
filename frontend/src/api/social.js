import request from "./request";

const payload = (p) => p.data;

/** 某日记评论树（一级 + 回复） */
export const listComments = (diaryId) =>
  request.get(`/api/diary/${diaryId}/comments`).then(payload);

/** 发表评论 / 回复：{ content, parentId? } */
export const addComment = (diaryId, data) =>
  request.post(`/api/diary/${diaryId}/comments`, data).then(payload);

/** 删除评论 */
export const deleteComment = (diaryId, commentId) =>
  request.delete(`/api/diary/${diaryId}/comments/${commentId}`).then(payload);

/** 点赞（幂等），返回 { liked, likeCount } */
export const likeDiary = (diaryId) =>
  request.post(`/api/diary/${diaryId}/like`).then(payload);

/** 取消点赞 */
export const unlikeDiary = (diaryId) =>
  request.delete(`/api/diary/${diaryId}/like`).then(payload);

/** 点赞状态 */
export const likeStatus = (diaryId) =>
  request.get(`/api/diary/${diaryId}/like/status`).then(payload);
