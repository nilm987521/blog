<template>
  <v-app-bar
      app
      color="primary"
      dark
      elevation="4"
      style="position: fixed"
  >
    <v-app-bar-title>
      <router-link to="/" class="text-decoration-none text-white">NILM.CC</router-link>
        <span class="text-subtitle-2 text-white font-weight-light" style="opacity: 0.8;">
            邊踩坑邊分享，技術路上不孤單
        </span>
    </v-app-bar-title>

    <v-spacer></v-spacer>

    <!-- 導航選項 -->
    <v-btn to="/" text>
      <v-icon icon="mdi-home"></v-icon>
      首頁
    </v-btn>

    <v-btn to="/search" text>
      <v-icon left>mdi-magnify</v-icon>
      搜索
    </v-btn>

    <v-btn to="/about" text>
      <v-icon icon="mdi-account"></v-icon>
      關於我
    </v-btn>

    <template v-if="isAuthenticated">
      <v-btn to="/posts/create" text color="success" class="ml-2">
        <v-icon left>mdi-pencil</v-icon>
        寫文章
      </v-btn>

      <v-menu>
        <template v-slot:activator="{ props }">
          <v-btn text v-bind="props" class="text-white">
            <v-avatar size="32" class="mr-2">
              <v-img v-if="userAvatar" :src="userAvatar" alt="用戶頭像"></v-img>
              <v-icon v-else>mdi-account-circle</v-icon>
            </v-avatar>
            <span>{{ displayName }}</span>
            <v-icon right class="ml-1">mdi-chevron-down</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-item v-if="isAdmin" to="/admin">
            <template v-slot:prepend>
              <v-icon>mdi-shield-account</v-icon>
            </template>
            <v-list-item-title>管理面板</v-list-item-title>
          </v-list-item>

          <v-divider></v-divider>

          <v-list-item @click="logout" class="text-error">
            <template v-slot:prepend>
              <v-icon color="error">mdi-logout</v-icon>
            </template>
            <v-list-item-title>登出</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </template>

    <template v-else>
      <v-btn to="/login" text>
        <v-icon left>mdi-login</v-icon>
        登入
      </v-btn>
    </template>
  </v-app-bar>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useAuthStore} from '@/store/auth'
import CategoryService from '@/services/category.service'
import {useRoute} from 'vue-router'

const authStore = useAuthStore()
const route = useRoute()
const categories = ref([])

// 使用計算屬性觀察身份認證狀態
const isAuthenticated = computed(() => authStore.isAuthenticated)
const isAdmin = computed(() => authStore.isAdmin)
const username = computed(() => authStore.user?.username || '用戶')

// 新增計算屬性用於顯示名稱和頭像
const displayName = computed(() => {
  // 優先使用全名，如果沒有則使用用戶名
  return authStore.user?.fullName || authStore.user?.username || '用戶'
})

const userAvatar = computed(() => {
  // 如果有頭像 URL 則使用，否則返回 null
  return authStore.user?.profileImage || null
})

// 監聽路由變化，確保在路由變化時重新檢查身份認證狀態
watch(
    () => route.path,
    () => {
      console.log('Route changed, checking auth status')
      checkAuthStatus()
    }
)

// 直接監聽 authStore 的 isAuthenticated 變化
watch(
    () => authStore.isAuthenticated,
    (newValue) => {
      console.log('Auth status changed:', newValue)
      if (newValue) {
        console.log('User authenticated, username:', authStore.user?.username)
      } else {
        console.log('User not authenticated')
      }
    }
)

onMounted(async () => {
  // 只在需要時加載分類數據（例如用戶已登錄時）
  if (isAuthenticated.value) {
    await loadCategories()
  }
  checkAuthStatus()
  // 強制刷新登入狀態
  authStore.initAuth()
})

// 檢查並強制更新身份認證狀態
function checkAuthStatus() {
  console.log('Checking auth status')
  console.log('Is authenticated:', isAuthenticated.value)
  if (isAuthenticated.value) {
    console.log('User is authenticated:', username.value)
  } else {
    console.log('User is not authenticated')
  }
}

async function loadCategories() {
  try {
    // 只在用戶已認證時才加載分類
    if (isAuthenticated.value) {
      categories.value = await CategoryService.getAllCategories()
    }
  } catch (error) {
    console.error('獲取分類失敗:', error)
  }
}

function logout() {
  authStore.logout()
}

</script>

<style scoped>
.v-app-bar a {
  text-decoration: none;
  color: white;
}

.v-app-bar-title a {
  font-weight: bold;
  font-size: 1.2rem;
}
</style>