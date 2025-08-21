<template>
  <v-container style="padding-top: 100px;margin-bottom: 200px">
    <h1 class="text-h4 mb-6">搜索文章</h1>

    <v-card class="mb-8" elevation="3">
      <v-card-text>
        <v-text-field
            v-model="searchQuery"
            label="請輸入搜索關鍵詞"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            clearable
            @keyup.enter="search"
        ></v-text-field>

        <v-btn
            color="primary"
            block
            @click="search"
            :loading="loading"
            :disabled="!searchQuery.trim()"
        >
          搜索
        </v-btn>
      </v-card-text>
    </v-card>

    <div v-if="hasSearched">
      <div v-if="loading">
        <v-skeleton-loader
            v-for="i in 3"
            :key="i"
            type="card"
            class="mb-4"
        ></v-skeleton-loader>
      </div>

      <div v-else-if="error" class="text-center py-8">
        <v-alert type="error">
          {{ error }}
        </v-alert>
        <v-btn @click="search" color="primary" class="mt-4">
          重試
        </v-btn>
      </div>

      <div v-else-if="posts.length === 0" class="text-center py-8">
        <v-alert type="info">
          沒有找到與 "{{ searchQuery }}" 相關的文章
        </v-alert>
      </div>

      <template v-else>
        <h2 class="text-h5 mb-4">搜索結果: {{ posts.length }} 篇文章</h2>

        <div>
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
      </template>
    </div>
  </v-container>
</template>

<script setup>
import {ref, computed, watch, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {usePostStore} from '../store/post'
import PostCard from '../components/post/PostCard.vue'
import CategoryService from '../services/category.service'
import TagService from '../services/tag.service'

const route = useRoute()
const router = useRouter()
const postStore = usePostStore()

const searchQuery = ref('')
const hasSearched = ref(false)
const page = ref(1)
const categories = ref([])
const tags = ref([])

const posts = computed(() => postStore.posts)
const loading = computed(() => postStore.loading)
const error = computed(() => postStore.error)
const totalPages = computed(() => postStore.totalPages)
const isPaginationAvailable = computed(() => postStore.isPaginationAvailable)

// 從 URL 參數初始化搜索查詢
watch(() => route.query, (newQuery) => {
  if (newQuery.q) {
    searchQuery.value = newQuery.q
    page.value = Number(newQuery.page) || 1
    performSearch()
  }
}, {immediate: true})

onMounted(() => {
  loadCategories()
  loadTags()
})

function search() {
  if (!searchQuery.value.trim()) return

  page.value = 1
  updateRouteQuery()
  performSearch()
}

function handlePageChange(newPage) {
  page.value = newPage
  updateRouteQuery()
  performSearch()
  // 滾動到頁面頂部
  window.scrollTo({top: 0, behavior: 'smooth'})
}

function updateRouteQuery() {
  router.push({
    query: {
      q: searchQuery.value,
      page: page.value
    }
  })
}

async function performSearch() {
  if (!searchQuery.value.trim()) return

  try {
    await postStore.searchPosts(searchQuery.value, page.value - 1)
    hasSearched.value = true
  } catch (err) {
    console.error('搜索失敗:', err)
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
</script>

<style scoped>
/* 可以添加一些自訂樣式 */
</style>