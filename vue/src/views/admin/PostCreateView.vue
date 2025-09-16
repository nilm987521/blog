<template>
  <v-container style="padding-top: 100px;margin-bottom: 200px">
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-6">創建新文章</h1>

        <v-alert
          v-if="error"
          type="error"
          class="mb-4"
          closable
          @click:close="error = null"
        >
          {{ error }}
        </v-alert>

        <div v-if="loading" class="text-center py-8">
          <v-progress-circular
            indeterminate
            color="primary"
            size="64"
          ></v-progress-circular>
          <p class="mt-4">正在創建草稿...</p>
        </div>

        <div v-else-if="error" class="text-center py-8">
          <v-alert type="error" class="mb-4">
            {{ error }}
          </v-alert>
          <v-btn @click="createDraftAndRedirect" color="primary" class="mt-4">
            重試
          </v-btn>
          <v-btn to="/" color="secondary" class="mt-4 ml-4">
            返回首頁
          </v-btn>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { usePostStore } from '../../store/post.ts'
import logger from '../../utils/logger.ts'

const router = useRouter()
const postStore = usePostStore()

const error = ref<string | null>(null)
const loading = computed<boolean>(() => postStore.loading)

onMounted(async () => {
  await createDraftAndRedirect()
})

async function createDraftAndRedirect(): Promise<void> {
  error.value = null

  try {
    logger.info('開始創建草稿文章');

    // 創建草稿文章
    const draftPost = await postStore.createDraftPost()

    logger.info('草稿創建成功，跳轉到編輯頁面', { postId: draftPost.id });

    // 跳轉到編輯頁面
    router.push({
      name: 'post-edit',
      params: { id: draftPost.id }
    })
  } catch (err: any) {
    logger.error('創建草稿失敗:', err);
    error.value = '創建草稿失敗：' + (err.message || '發生未知錯誤')
  }
}
</script>

<style scoped>
.editor-container {
  margin-top: 20px;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
</style>