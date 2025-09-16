<template>
  <v-container style="padding-top: 100px;margin-bottom: 200px">
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-6">編輯文章</h1>

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
          <p class="mt-4">載入文章中...</p>
        </div>

        <v-form v-else-if="post" @submit.prevent="updatePost" ref="postForm">
          <v-card class="mb-6">
            <v-card-text>
              <v-text-field
                v-model="title"
                label="文章標題"
                variant="outlined"
                :rules="[v => !!v || '標題不能為空']"
                required
              ></v-text-field>

              <v-select
                v-model="categoryId"
                :items="categories"
                item-title="name"
                item-value="id"
                label="選擇分類"
                variant="outlined"
                :rules="[v => !!v || '請選擇分類']"
                required
              ></v-select>

              <v-combobox
                v-model="selectedTags"
                :items="availableTags"
                item-title="name"
                label="選擇標籤（可多選）"
                multiple
                chips
                variant="outlined"
                hint="可以選擇現有標籤或創建新標籤"
                persistent-hint
              >
                <!-- 已選中的標籤進行渲染 -->
                <template v-slot:chip="{ props, item }">
                  <v-chip
                    v-bind="props"
                    :color="item.raw && item.raw.color ? item.raw.color : 'primary'"
                    variant="outlined"
                  >
                    {{ item.title }}
                  </v-chip>
                </template>
                
                <!-- 選單中的標籤項目渲染 -->
                <template v-slot:item="{ props, item }">
                  <v-list-item
                    v-bind="props"
                    :prepend-icon="'mdi-tag'"
                    :title="item.raw.name"
                  >
                    <template v-slot:prepend>
                      <v-avatar
                        :color="item.raw.color || 'primary'"
                        size="24"
                        class="mr-2"
                      ></v-avatar>
                    </template>
                  </v-list-item>
                </template>
              </v-combobox>

              <v-card-title class="px-0 pt-4">文章內容</v-card-title>
              
              <!-- 使用自定義 MarkdownEditor 組件 -->
              <div class="editor-container">
                <markdown-editor
                  v-model="content"
                  :post-id="post?.id"
                  height="400px"
                  placeholder="開始編寫您的文章內容..."
                  @ready="editorReady"
                  @image-uploaded="handleImageUploaded"
                  @image-upload-error="handleImageUploadError"
                />
              </div>

              <v-switch
                v-model="published"
                label="發布文章"
                color="primary"
                hide-details
                class="mt-4"
              ></v-switch>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn 
                color="secondary" 
                text 
                :to="{ name: 'post-detail', params: { id: post.id } }"
              >
                取消
              </v-btn>
              <v-btn 
                color="primary" 
                type="submit"
                :loading="saving"
              >
                更新文章
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-form>

        <div v-else-if="error" class="text-center py-8">
          <v-alert type="error">
            {{ error }}
          </v-alert>
          <v-btn @click="fetchPost" color="primary" class="mt-4">
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePostStore } from '@/store/post.ts'
import { useAuthStore } from '@/store/auth.ts'
import CategoryService from '@/services/category.service.ts'
import TagService from '@/services/tag.service.ts'
import { MarkdownEditor } from '@/components/editor'
import { Category, Tag, Post } from '@/types'
import logger from '@/utils/logger.ts'

// 定義表單驗證類型
interface VForm {
  validate: () => Promise<{ valid: boolean }>;
}

const route = useRoute()
const router = useRouter()
const postStore = usePostStore()
const authStore = useAuthStore()

const post = computed<Post | null>(() => postStore.currentPost)
const loading = computed<boolean>(() => postStore.loading)
const error = ref<string | null>(null)
const saving = ref<boolean>(false)

const title = ref<string>('')
const content = ref<string>('')
const categoryId = ref<number | null>(null)
const published = ref<boolean>(false)
const categories = ref<Category[]>([])
const availableTags = ref<Tag[]>([])
const selectedTags = ref<(Tag | string)[]>([])
const postForm = ref<VForm | null>(null)

// 編輯器就緒事件
function editorReady(editor: any): void {
  logger.info('編輯器就緒:', { editor: !!editor });
}

// 處理圖片上傳成功事件
function handleImageUploaded(url: string): void {
  logger.info('圖片上傳成功:', { url });
}

// 處理圖片上傳失敗事件
function handleImageUploadError(err: any): void {
  logger.error('圖片上傳失敗:', err);
  error.value = '圖片上傳失敗，請再試一次';
}

// 檢查當前用戶是否為文章作者或管理員
const isAuthorOrAdmin = computed<boolean>(() => {
  if (!post.value || !authStore.user) return false
  
  return (
    post.value.author.id === authStore.user.id ||
    authStore.isAdmin
  )
})

onMounted(async () => {
  await fetchCategories()
  await fetchTags()
  await fetchPost()
})

// 當文章數據加載完成後，填充表單
watch(post, (newPost) => {
  if (newPost) {
    title.value = newPost.title
    // 確保內容正確設置
    logger.info('載入文章內容：', {
      preview: newPost.content?.substring(0, 100),
      length: newPost.content?.length,
      type: typeof newPost.content
    });
    content.value = newPost.content || ''
    categoryId.value = newPost.category?.id || null
    published.value = newPost.published

    // 設置已選標籤
    if (newPost.tags && newPost.tags.length > 0) {
      selectedTags.value = [...newPost.tags]
    }
  }
}, { immediate: true })

// 檢查權限，如果不是作者或管理員，跳轉到首頁
watch(isAuthorOrAdmin, (hasPermission) => {
  if (post.value && !hasPermission) {
    router.push('/')
  }
})

async function fetchPost(): Promise<void> {
  error.value = null
  try {
    logger.info('開始載入文章:', route.params.id);
    await postStore.fetchPostById(route.params.id as string)
    logger.info('文章加載完成：', {
      title: postStore.currentPost?.title,
      contentType: typeof postStore.currentPost?.content,
      contentLength: postStore.currentPost?.content?.length
    });
  } catch (err: any) {
    error.value = '獲取文章失敗: ' + err.message
    console.error('獲取文章失敗:', err)
  }
}

async function fetchCategories(): Promise<void> {
  try {
    categories.value = await CategoryService.getAllCategories()
  } catch (err) {
    console.error('獲取分類失敗:', err)
    error.value = '無法載入分類'
  }
}

async function fetchTags(): Promise<void> {
  try {
    availableTags.value = await TagService.getAllTags()
  } catch (err) {
    console.error('獲取標籤失敗:', err)
    error.value = '無法載入標籤'
  }
}

async function updatePost(): Promise<void> {
  if (!postForm.value || !post.value) return
  
  const { valid } = await postForm.value.validate()
  
  if (!valid) return
  
  error.value = null
  saving.value = true
  
  try {
    // 使用 Markdown 編輯器的內容應該已經是字符串格式，不需要複雜的轉換
    logger.debug('更新前的內容類型與值：', {
      type: typeof content.value,
      preview: content.value?.substring(0, 100),
      length: content.value?.length
    });
    
    // 處理標籤 ID
    const tagIds: number[] = []
    
    for (const tag of selectedTags.value) {
      if (typeof tag === 'object' && tag && 'id' in tag && tag.id) {
        // 已有標籤
        tagIds.push(tag.id)
      } else if (typeof tag === 'string' || (typeof tag === 'object' && tag && 'name' in tag && tag.name && !('id' in tag))) {
        // 新標籤
        const tagName = typeof tag === 'string' ? tag : tag.name
        // 為新標籤設置預設藍色
        const response = await TagService.createTag({ name: tagName, color: '#1976D2' })
        tagIds.push(response.id)
      }
    }
    
    const postData = {
      title: title.value,
      content: content.value,
      categoryId: categoryId.value as number,
      tagIds: tagIds,
      published: published.value
    }
    
    logger.debug('提交的更新文章數據:', postData);
    
    await postStore.updatePost(post.value.id, postData)
    
    router.push({
      name: 'post-detail',
      params: { id: post.value.id }
    })
  } catch (err: any) {
    logger.error('更新文章失敗:', err);
    error.value = '更新文章失敗：' + (err.message || '發生未知錯誤')
  } finally {
    saving.value = false
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