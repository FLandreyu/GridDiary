import request from "./request";

const payload = (p) => p.data;

/**
 * 上传多张图片（FormData，字段名 files）。
 * @param {File[]} files 图片文件（jpg/png，单张≤5MB）
 * @returns {Promise<Array<{originalUrl:string, thumbUrl:string}>>}
 */
export async function uploadImages(files) {
  const fd = new FormData();
  for (const f of files) fd.append("files", f);
  // axios 遇到 FormData 会自动设置 multipart 边界
  return request.post("/api/upload/image", fd).then(payload);
}
