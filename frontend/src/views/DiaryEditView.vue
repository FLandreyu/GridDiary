<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getDiary, createDiary, updateDiary } from "../api/diary";
import { uploadImages } from "../api/upload";

const route = useRoute();
const router = useRouter();
const diaryId = route.query.id ? Number(route.query.id) : null;
const isEdit = !!diaryId;

const loading = ref(false); // 编辑时拉取详情
const saving = ref(false);
const title = ref("");
const content = ref("");
const isPublic = ref(true);

/**
 * 图片列表：
 *  - 已存在于服务器的：{ originalUrl, thumbUrl }
 *  - 本地待上传的：    { preview(blob), raw(File) }
 */
const images = ref([]);
const fileInput = ref();

const MAX_IMAGES = 9;

onMounted(async () => {
  if (!isEdit) return;
  loading.value = true;
  try {
    const d = await getDiary(diaryId);
    title.value = d.title;
    content.value = d.content || "";
    isPublic.value = d.isPublic !== false;
    images.value = (d.images || []).map((im) => ({
      originalUrl: im.originalUrl,
      thumbUrl: im.thumbUrl,
    }));
  } catch (_) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false;
  }
});

function pick() {
  fileInput.value?.click();
}

/** 选择本地图片：仅本地预览，确认提交时才真正上传 */
function onPick(e) {
  const files = Array.from(e.target.files || []);
  e.target.value = "";
  for (const file of files) {
    if (images.value.length >= MAX_IMAGES) {
      ElMessage.warning(`最多上传 ${MAX_IMAGES} 张图片`);
      break;
    }
    if (!["image/jpeg", "image/png"].includes(file.type)) {
      ElMessage.warning(`${file.name} 仅支持 jpg / png`);
      continue;
    }
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.warning(`${file.name} 超过 5MB`);
      continue;
    }
    images.value.push({ preview: URL.createObjectURL(file), raw: file });
  }
}

function removeImage(i) {
  const img = images.value[i];
  if (img.preview) URL.revokeObjectURL(img.preview);
  images.value.splice(i, 1);
}

function hasText(s) {
  return !!(s && s.trim());
}

async function submit() {
  if (!hasText(title.value)) return ElMessage.warning("请填写标题");
  saving.value = true;
  try {
    // 1) 先上传本地新图（一次请求批量上传）
    const newRaws = images.value.filter((i) => i.raw).map((i) => i.raw);
    const uploaded = newRaws.length ? await uploadImages(newRaws) : [];
    let k = 0;
    // 2) 组装最终图片顺序
    const finalImages = [];
    for (const img of images.value) {
      if (img.raw) {
        finalImages.push({
          originalUrl: uploaded[k].originalUrl,
          thumbUrl: uploaded[k].thumbUrl,
        });
        k++;
      } else {
        finalImages.push({
          originalUrl: img.originalUrl,
          thumbUrl: img.thumbUrl,
        });
      }
    }
    const payload = {
      title: title.value.trim(),
      content: content.value,
      isPublic: isPublic.value,
      images: finalImages,
    };
    // 3) 保存
    if (isEdit) {
      await updateDiary(diaryId, payload);
      ElMessage.success("修改成功");
    } else {
      await createDiary(payload);
      ElMessage.success("发布成功");
    }
    router.push("/my");
  } catch (_) {
    /* 拦截器已提示 */
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div class="edit-wrap">
    <el-card v-loading="loading">
      <template #header>
        <b>{{ isEdit ? "✏️ 修改日记" : "🖊️ 写日记" }}</b>
      </template>

      <el-form label-width="70px">
        <el-form-item label="标题" required>
          <el-input
            v-model="title"
            maxlength="200"
            show-word-limit
            placeholder="给今天起个标题"
          />
        </el-form-item>

        <el-form-item label="正文">
          <el-input
            v-model="content"
            type="textarea"
            :rows="7"
            placeholder="记录今天的心情 / 见闻 / 想法……"
          />
        </el-form-item>

        <el-form-item label="图片">
          <div class="img-grid">
            <div v-for="(img, i) in images" :key="i" class="img-item">
              <el-image
                :src="img.preview || img.thumbUrl"
                fit="cover"
                class="img"
              />
              <div class="remove" @click="removeImage(i)">✕</div>
            </div>
            <div
              v-if="images.length < MAX_IMAGES"
              class="img-add"
              @click="pick"
            >
              ＋
              <span>添加图片</span>
            </div>
          </div>
          <div class="tip">
            支持 jpg / png，单张 ≤ 5MB，最多 9
            张（先本地预览，发布时自动上传并生成缩略图）
          </div>
          <input
            ref="fileInput"
            type="file"
            accept="image/jpeg,image/png"
            multiple
            hidden
            @change="onPick"
          />
        </el-form-item>

        <el-form-item label="公开">
          <el-switch
            v-model="isPublic"
            active-text="公开"
            inactive-text="仅自己可见"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">
            {{ isEdit ? "保存修改" : "发布" }}
          </el-button>
          <el-button @click="router.push('/my')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.edit-wrap {
  max-width: 780px;
  margin: 0 auto;
}
.img-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  width: 100%;
}
.img-item,
.img-add {
  width: 110px;
  height: 110px;
  border-radius: 14px;
  overflow: hidden;
  position: relative;
  border: 1px solid var(--gd-border);
}
.img {
  width: 100%;
  height: 100%;
}
.img-item .remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  line-height: 18px;
  text-align: center;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
  cursor: pointer;
}
.img-add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px dashed var(--gd-border);
  color: var(--gd-text-sub);
  cursor: pointer;
  gap: 2px;
}
.img-add:hover {
  color: var(--gd-primary);
  border-color: var(--gd-primary);
}
.tip {
  width: 100%;
  color: var(--gd-text-sub);
  font-size: 12px;
  margin-top: 8px;
}
</style>
