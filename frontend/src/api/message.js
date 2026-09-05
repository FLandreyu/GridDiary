import request from "./request";

const payload = (p) => p.data;

/** 会话列表 */
export const conversations = () =>
  request.get("/api/message/conversations").then(payload);

/** 我的未读私信总数 */
export const unreadCount = () =>
  request.get("/api/message/unread-count").then(payload);

/** 与某人的聊天记录（打开即标记已读） */
export const chatWith = (peerId, limit = 100) =>
  request
    .get(`/api/message/with/${peerId}`, { params: { limit } })
    .then(payload);

/** 发送私信 */
export const sendMessage = (toUserId, content) =>
  request.post("/api/message", { toUserId, content }).then(payload);
