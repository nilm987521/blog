<template>
  <div style="padding-top: 30px">
    <v-container v-if="loading">
      <v-skeleton-loader
        type="article, actions"
        class="mx-auto"
      ></v-skeleton-loader>
    </v-container>

    <v-container v-else-if="error" class="text-center py-8">
      <v-alert type="error">
        {{ error }}
      </v-alert>
      <v-btn @click="fetchPost" color="primary" class="mt-4">
        重試
      </v-btn>
      <v-btn to="/" color="secondary" class="mt-4 ml-4">
        返回首頁
      </v-btn>
    </v-container>

    <div v-else-if="post">
      <div class="post-header text-center py-10">
        <v-container class="m.5">
          <v-chip
            v-if="post.category"
            color="primary"
            :to="{ name: 'category', params: { id: post.category.id }}"
            class="mb-4"
          >
            {{ post.category.name }}
          </v-chip>
          
          <h1 class="text-h3 font-weight-bold mb-4">{{ post.title }}</h1>
          
          <div class="d-flex align-center justify-center mb-4">
            <v-icon small class="mr-1">mdi-calendar</v-icon>
            <span>{{ formatDate(post.createdAt) }}</span>
            <v-divider vertical class="mx-3"></v-divider>
            <v-icon small class="mr-1">mdi-comment-outline</v-icon>
            <span>{{ post.comments?.length || 0 }} 評論</span>
          </div>
          
          <div class="d-flex flex-wrap justify-center">
            <v-chip
              v-for="tag in post.tags"
              :key="tag.id"
              variant="outlined"
              :color="tag.color || 'secondary'"
              :to="{ name: 'tag', params: { id: tag.id }}"
              class="ma-1"
            >
              {{ tag.name }}
            </v-chip>
          </div>
        </v-container>
      </div>

      <v-container>
        <v-row>
          <v-col cols="12" md="20">
            <div class="post-content mb-8">
              <!-- Markdown 內容渲染 -->
              <md-preview 
                v-if="post.content"
                :model-value="post.content"
                :preview-theme="'default'"
                :code-theme="'github'"
                style="padding: 20px; background: #FFFFFF;"
              />
              <!-- 如果沒有使用 Markdown，則使用傳統 HTML 顯示 (向後兼容) -->
              <div v-else-if="post.content" v-html="post.content" class="text-body-1"></div>
            </div>
            
            <v-divider class="mb-8"></v-divider>

            <!-- 文章操作按鈕 -->
            <div v-if="isAuthor || isAdmin" class="mb-8">
              <v-btn
                color="primary"
                :to="{ name: 'post-edit', params: { id: post.id }}"
                class="mr-2"
              >
                <v-icon left>mdi-pencil</v-icon>
                編輯文章
              </v-btn>
              
              <v-btn color="error" @click="confirmDelete">
                <v-icon left>mdi-delete</v-icon>
                刪除文章
              </v-btn>
            </div>

            <!-- 評論區 -->
            <h2 class="text-h5 mb-4">評論 ({{ post.comments?.length || 0 }})</h2>
            
            <div v-if="isAuthenticated" class="mb-8">
              <v-textarea
                v-model="newComment"
                label="寫下您的評論"
                variant="outlined"
                rows="4"
                hide-details
                style="background: #ffffff"
              ></v-textarea>
              <v-btn color="primary" class="mt-2" @click="submitComment" :loading="commentLoading">
                發表評論
              </v-btn>
            </div>
            
            <div v-else class="text-center py-4 mb-4">
              <p>請 <router-link to="/login">登入</router-link> 後發表評論</p>
            </div>

            <div v-if="post.comments && post.comments.length > 0">
              <v-card
                v-for="comment in post.comments"
                :key="comment.id"
                class="mb-4"
                variant="outlined"
              >
                <v-card-text>
                  <div class="d-flex align-center mb-2">
                    <v-avatar size="36" class="mr-2">
                      <v-img src="/avatar-placeholder.png"></v-img>
                    </v-avatar>
                    <div>
                      <div class="text-subtitle-1">{{ comment.user.username }}</div>
                      <div class="text-caption">{{ formatDate(comment.createdAt) }}</div>
                    </div>
                  </div>
                  <p class="text-body-1">{{ comment.content }}</p>
                </v-card-text>
              </v-card>
            </div>
            
            <div v-else class="text-center py-4">
              <p class="text-body-1">暫無評論，成為第一個發表評論的人吧！</p>
            </div>
          </v-col>
        </v-row>
      </v-container>

      <!-- 相關文章 -->
      <v-container class="mt-8">
        <h2 class="text-h4 mb-6">推薦文章</h2>
        <v-row>
          <v-col
            v-for="(relatedPost, index) in relatedPosts"
            :key="index"
            cols="12"
            sm="6"
            md="4"
          >
            <post-card :post="relatedPost"></post-card>
          </v-col>
        </v-row>
      </v-container>
    </div>

    <!-- 刪除確認對話框 -->
    <v-dialog v-model="deleteDialog" max-width="500">
      <v-card>
        <v-card-title class="text-h5">確認刪除</v-card-title>
        <v-card-text>
          您確定要刪除這篇文章嗎？此操作無法撤銷。
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" text @click="deleteDialog = false">取消</v-btn>
          <v-btn color="error" @click="deletePost" :loading="deleteLoading">確認刪除</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePostStore } from '../store/post'
import { useAuthStore } from '../store/auth'
import PostCard from '../components/post/PostCard.vue'
import CommentService from '../services/comment.service'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const route = useRoute()
const router = useRouter()
const postStore = usePostStore()
const authStore = useAuthStore()

const post = computed(() => postStore.currentPost)
const loading = computed(() => postStore.loading)
const error = computed(() => postStore.error)
const isAuthenticated = computed(() => authStore.isAuthenticated)
const currentUser = computed(() => authStore.user)

const isAuthor = computed(() => {
  if (!isAuthenticated.value || !post.value) return false
  return post.value.author.id === currentUser.value.id
})

const isAdmin = computed(() => authStore.isAdmin)

const newComment = ref('')
const commentLoading = ref(false)
const deleteDialog = ref(false)
const deleteLoading = ref(false)
const relatedPosts = ref([])

onMounted(async () => {
  await fetchPost()
  await fetchRelatedPosts()
})

async function fetchPost() {
  try {
    await postStore.fetchPostById(route.params.id)
  } catch (err) {
    console.error('獲取文章失敗:', err)
  }
}

// 模擬獲取相關文章的函數，實際應用中可能需要後端支持
async function fetchRelatedPosts() {
  try {
    // 這裡只是為了示範，實際上可能需要一個專門的 API 來獲取相關文章
    // 例如基於同一分類或標籤的其他文章
    const response = await postStore.fetchPosts(0, 3)
    relatedPosts.value = response.content.filter(p => p.id !== post.value.id)
  } catch (err) {
    console.error('獲取相關文章失敗:', err)
  }
}

function formatDate(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

async function submitComment() {
  if (!newComment.value.trim()) return
  
  commentLoading.value = true
  
  try {
    const commentData = {
      content: newComment.value,
      postId: post.value.id
    }
    
    await CommentService.createComment(commentData)
    
    // 重新獲取文章以更新評論
    await fetchPost()
    
    newComment.value = ''
  } catch (err) {
    console.error('發表評論失敗:', err)
    alert('發表評論失敗，請稍後再試')
  } finally {
    commentLoading.value = false
  }
}

function confirmDelete() {
  deleteDialog.value = true
}

async function deletePost() {
  deleteLoading.value = true
  
  try {
    await postStore.deletePost(post.value.id)
    deleteDialog.value = false
    router.push('/')
  } catch (err) {
    console.error('刪除文章失敗:', err)
    alert('刪除文章失敗，請稍後再試')
  } finally {
    deleteLoading.value = false
  }
}
</script>

<style scoped>
.post-header {
  background-color: #f5f5f5;
}

/* 自訂 Markdown 預覽樣式 */
:deep(.md-editor-preview-wrapper) {
  padding: 0 !important;
  margin: 0 !important;
  border: none !important;
}

:deep(.md-editor) {
  border: none !important;
  box-shadow: none !important;
}

:deep(.md-editor-toolbar) {
  display: none !important;
}

.post-content {
  line-height: 1.8;
  font-size: 1.1rem;
}

/* 向後兼容的 HTML 內容樣式 */
.post-content :deep(h1), 
.post-content :deep(h2), 
.post-content :deep(h3),
.post-content :deep(h4),
.post-content :deep(h5),
.post-content :deep(h6) {
  margin-top: 1.5em;
  margin-bottom: 0.75em;
}

.post-content :deep(p) {
  margin-bottom: 1.5em;
}

.post-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1.5em 0;
}

.post-content :deep(blockquote) {
  padding: 10px 20px;
  margin: 20px 0;
  border-left: 4px solid #1976D2;
  background-color: #f5f5f5;
  font-style: italic;
}

.post-content :deep(code) {
  background-color: #f0f0f0;
  padding: 2px 4px;
  border-radius: 4px;
}

.post-content :deep(pre) {
  background-color: #f0f0f0;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 20px 0;
}

.post-content :deep(a) {
  color: #1976D2;
  text-decoration: none;
}

.post-content :deep(a:hover) {
  text-decoration: underline;
}
</style>