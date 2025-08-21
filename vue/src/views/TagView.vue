<template>
  <v-container>
    <v-row>
      <!-- 左側文章列表 -->
      <v-col cols="12" md="8">
        <div v-if="loading" class="text-center py-8">
          <v-progress-circular
            indeterminate
            color="primary"
            size="64"
          ></v-progress-circular>
          <p class="mt-4">載入中...</p>
        </div>
        
        <div v-else-if="error" class="text-center py-8">
          <v-alert type="error">
            {{ error }}
          </v-alert>
          <v-btn @click="loadTag" color="primary" class="mt-4">
            重試
          </v-btn>
          <v-btn to="/" color="secondary" class="mt-4 ml-4">
            返回首頁
          </v-btn>
        </div>
        
        <div v-else-if="tag">
          <header class="tag-header text-center py-8 mb-8">
            <v-chip
              size="x-large"
              :color="tag.color || 'primary'"
              class="mb-4 px-4 py-2"
            >
              <v-icon start>mdi-tag</v-icon>
              {{ tag.name }}
            </v-chip>
            <h1 class="text-h3 font-weight-bold mb-4">{{ tag.name }} 標籤</h1>
            <p class="text-body-2 grey--text">{{ posts.length }} 篇文章</p>
          </header>
          
          <div v-if="posts.length === 0" class="text-center py-8">
            <p class="text-body-1">這個標籤下還沒有文章</p>
            <v-btn v-if="isAuthenticated" to="/posts/create" color="primary" class="mt-4">
              發表第一篇文章
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
        </div>
      </v-col>

      <!-- 右側標籤和分類 -->
      <v-col cols="12" md="4">
        <!-- 熱門標籤 -->
        <v-card class="mb-6" variant="outlined">
          <v-card-title class="text-h5 font-weight-bold">熱門標籤</v-card-title>
          <v-card-text>
            <div class="d-flex flex-wrap">
              <v-chip
                v-for="t in tags"
                :key="t.id"
                :to="{ name: 'tag', params: { id: t.id }}"
                class="ma-1"
                size="small"
                :color="t.color || 'primary'"
                variant="outlined"
                :active="t.id == route.params.id"
              >
                {{ t.name }}
              </v-chip>
            </div>
            <div v-if="tags.length === 0" class="text-center py-2">
              <p class="text-body-2 grey--text">沒有可用的標籤</p>
            </div>
          </v-card-text>
        </v-card>

        <!-- 分類瀏覽 -->
        <v-card variant="outlined">
          <v-card-title class="text-h5 font-weight-bold">分類瀏覽</v-card-title>
          <v-card-text>
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
                <v-list-item-title>{{ category.name }}</v-list-item-title>
                <v-list-item-subtitle>{{ category.postCount || 0 }} 篇文章</v-list-item-subtitle>
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
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { usePostStore } from '../store/post'
import { useAuthStore } from '../store/auth'
import PostCard from '../components/post/PostCard.vue'
import TagService from '../services/tag.service'
import CategoryService from '../services/category.service'

const route = useRoute()
const postStore = usePostStore()
const authStore = useAuthStore()

const tag = ref(null)
const tagLoading = ref(false)
const tagError = ref(null)
const page = ref(1)
const categories = ref([])
const tags = ref([])

const posts = computed(() => postStore.posts)
const loading = computed(() => postStore.loading || tagLoading.value)
const error = computed(() => postStore.error || tagError.value)
const totalPages = computed(() => postStore.totalPages)
const isPaginationAvailable = computed(() => postStore.isPaginationAvailable)
const isAuthenticated = computed(() => authStore.isAuthenticated)

// 當標籤 ID 變更時，重新加載數據
watch(() => route.params.id, (newId) => {
  if (newId) {
    loadTag()
    page.value = 1
    loadPosts()
  }
}, { immediate: true })

onMounted(() => {
  loadTag()
  loadPosts()
  loadCategories()
  loadTags()
})

async function loadTag() {
  const tagId = route.params.id
  if (!tagId) return
  
  tagLoading.value = true
  tagError.value = null
  
  try {
    tag.value = await TagService.getTag(tagId)
  } catch (err) {
    console.error('加載標籤失敗:', err)
    tagError.value = '無法加載標籤信息'
  } finally {
    tagLoading.value = false
  }
}

async function loadPosts() {
  const tagId = route.params.id
  if (!tagId) return
  
  try {
    await postStore.fetchPostsByTag(tagId, page.value - 1)
  } catch (err) {
    console.error('加載標籤文章失敗:', err)
  }
}

async function loadCategories() {
  try {
    categories.value = await CategoryService.getAllCategories()
  } catch (err) {
    console.error('載入分類失敗:', err)
  }
}

async function loadTags() {
  try {
    tags.value = await TagService.getAllTags()
  } catch (err) {
    console.error('載入標籤失敗:', err)
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
.tag-header {
  background-color: #f5f5f5;
  border-radius: 8px;
}
</style>