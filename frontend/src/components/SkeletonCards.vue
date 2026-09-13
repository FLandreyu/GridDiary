<script setup>
/**
 * 列表骨架屏：首屏加载时占位，避免白屏跳动
 * type: grid（九宫格卡片）/ list（横向卡）/ row（左侧圆头像的行）
 */
defineProps({
  type: { type: String, default: "grid" },
  count: { type: Number, default: 6 },
});
</script>

<template>
  <!-- 九宫格卡片 -->
  <div v-if="type === 'grid'" class="sk-grid">
    <div v-for="i in count" :key="i" class="sk-card">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item
            variant="image"
            style="width: 100%; height: 150px; border-radius: 0"
          />
          <div class="sk-body">
            <el-skeleton-item variant="h3" style="width: 72%" />
            <el-skeleton-item
              variant="text"
              style="width: 46%; margin-top: 10px"
            />
            <el-skeleton-item
              variant="text"
              style="width: 30%; margin-top: 10px"
            />
          </div>
        </template>
      </el-skeleton>
    </div>
  </div>

  <!-- 横向列表卡 -->
  <div v-else-if="type === 'list'">
    <div v-for="i in count" :key="i" class="sk-item">
      <el-skeleton animated>
        <template #template>
          <div class="sk-item-inner">
            <el-skeleton-item
              variant="image"
              style="width: 176px; height: 118px; border-radius: 12px"
            />
            <div class="sk-item-body">
              <el-skeleton-item variant="h3" style="width: 55%" />
              <el-skeleton-item
                variant="text"
                style="width: 32%; margin-top: 12px"
              />
              <el-skeleton-item
                variant="text"
                style="width: 90%; margin-top: 14px"
              />
              <el-skeleton-item
                variant="text"
                style="width: 70%; margin-top: 8px"
              />
            </div>
          </div>
        </template>
      </el-skeleton>
    </div>
  </div>

  <!-- 行（头像 + 两行文字） -->
  <div v-else class="sk-rows">
    <div v-for="i in count" :key="i" class="sk-row">
      <el-skeleton animated>
        <template #template>
          <div class="sk-row-inner">
            <el-skeleton-item
              variant="circle"
              style="width: 44px; height: 44px; flex-shrink: 0"
            />
            <div class="sk-item-body">
              <el-skeleton-item variant="text" style="width: 34%" />
              <el-skeleton-item
                variant="text"
                style="width: 62%; margin-top: 10px"
              />
            </div>
          </div>
        </template>
      </el-skeleton>
    </div>
  </div>
</template>
