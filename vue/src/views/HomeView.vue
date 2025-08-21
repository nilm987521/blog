<template>
  <div style="padding-top: 100px;margin-bottom: 200px">
    <v-container>
      <v-row>
        <!-- 左側文章列表 -->
        <v-col cols="12" md="8">
          <h2 class="text-h4 font-weight-bold mb-6">最新文章</h2>

          <div v-if="loading">
            <v-skeleton-loader
              v-for="i in 6"
              :key="i"
              type="card"
              height="200"
              class="mb-4"
            ></v-skeleton-loader>
          </div>

          <div v-else-if="error" class="text-center py-8">
            <v-alert type="error">
              {{ error }}
            </v-alert>
            <v-btn @click="loadPosts" color="primary" class="mt-4">
              重試
            </v-btn>
          </div>

          <div v-else-if="posts.length === 0" class="text-center py-8">
            <p class="text-h6 grey--text">
              目前還沒有文章，快來發布第一篇吧！
            </p>
            <v-btn v-if="isAuthenticated" to="/posts/create" color="primary" class="mt-4">
              寫文章
            </v-btn>
            <v-btn v-else to="/login" color="primary" class="mt-4">
              登入以發布文章
            </v-btn>
          </div>

          <div v-else>
            <post-card 
              v-for="post in posts" 
              :key="post.id" 
              :post="post"
              class="mb-4"
            ></post-card>
          </div>

          <div v-if="isPaginationAvailable" class="text-center mt-8">
            <v-pagination
              v-model="page"
              :length="totalPages"
              @update:modelValue="handlePageChange"
              total-visible="7"
            ></v-pagination>
          </div>
        </v-col>

        <!-- 右側標籤和分類 -->
        <v-col cols="12" md="4">
          <!-- 熱門標籤 -->
          <h2 class="text-h4 font-weight-bold">熱門 Tag</h2>
          <v-card class="mb-6" elevation="1">
            <v-card-text class="pt-4">
              <div class="d-flex flex-wrap">
                <v-chip
                  v-for="tag in tags"
                  :key="tag.id"
                  :to="{ name: 'tag', params: { id: tag.id }}"
                  class="ma-1"
                  size="small"
                  :color="tag.color || 'primary'"
                  variant="outlined"
                >
                  # {{ tag.name }}
                </v-chip>
              </div>
              <div v-if="tags.length === 0" class="text-center py-2">
                <p class="text-body-2 grey--text">沒有可用的標籤</p>
              </div>
            </v-card-text>
          </v-card>

          <!-- 分類瀏覽 -->
          <h2 class="text-h4 font-weight-bold">所有分類</h2>
          <v-card class="mb-6" elevation="1">
            <v-card-text class="pt-1">
              <v-list>
                <v-list-item
                  v-for="category in categories"
                  :key="category.id"
                  :to="{ name: 'category', params: { id: category.id }}"
                  class="px-2"
                  rounded="lg"
                >
                  <template v-slot:prepend>
                    <v-icon color="primary">mdi-folder-outline</v-icon>
                  </template>
                  <v-list-item-title tag="span" style="display: inline;">
                    {{ category.name }}
                    <v-list-item-subtitle tag="span" style="display: inline;">
                      ( {{ category.posts.length || 0 }} 篇文章 )
                    </v-list-item-subtitle>
                  </v-list-item-title>
                </v-list-item>
              </v-list>
              <div v-if="categories.length === 0" class="text-center py-2">
                <p class="text-body-2 grey--text">沒有可用的分類</p>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePostStore } from '@/store/post'
import { useAuthStore } from '@/store/auth'
import PostCard from '@/components/post/PostCard.vue'
import CategoryService from '@/services/category.service'
import TagService from '@/services/tag.service'

const postStore = usePostStore()
const authStore = useAuthStore()

const posts = computed(() => postStore.posts)
const loading = computed(() => postStore.loading)
const error = computed(() => postStore.error)
const totalPages = computed(() => postStore.totalPages)
const isPaginationAvailable = computed(() => postStore.isPaginationAvailable)
const isAuthenticated = computed(() => authStore.isAuthenticated)

const page = ref(1)
const categories = ref([])
const tags = ref([])

onMounted(async () => {
  await loadPosts()
  await loadCategories()
  await loadTags()
})

async function loadPosts() {
  try {
    await postStore.fetchPosts(page.value - 1)
  } catch (err) {
    console.error('載入文章失敗:', err)
  }
}

async function loadCategories() {
  try {
    categories.value = await CategoryService.getAllCategories()
  } catch (err) {
    console.error('載入分類失敗:', err)
    // 如果載入失敗，設置空陣列避免顯示錯誤
    categories.value = []
  }
}

async function loadTags() {
  try {
    tags.value = await TagService.getAllTags()
  } catch (err) {
    console.error('載入標籤失敗:', err)
    // 如果載入失敗，設置空陣列避免顯示錯誤
    tags.value = []
  }
}

function handlePageChange(newPage) {
  page.value = newPage
  loadPosts()
  // 滾動到頁面頂部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.v-list-item__prepend {
  display: block;
}

.hero-section {
  padding: 80px 0;
  background-color: #f5f5f5;
  border-radius: 0 0 30px 30px;
}

.category-card {
  transition: transform 0.3s;
}

.category-card:hover {
  transform: translateY(-5px);
}

.v-list-item--density-default {
  min-height: 0;
}
</style>