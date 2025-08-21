<template>
  <v-app>
    <v-layout>
      <AppHeader />

      <v-main class="main-container">
        <router-view />
      </v-main>

      <AppFooter />
    </v-layout>
  </v-app>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from './store/auth'
import AppHeader from './components/layout/AppHeader.vue'
import AppFooter from './components/layout/AppFooter.vue'

const authStore = useAuthStore()

onMounted(() => {
  // 嘗試從本地存儲恢復用戶會話
  authStore.initAuth()
  
  // 每次刷新頁面後則檢查用戶登入狀態
  window.addEventListener('storage', (event) => {
    if (event.key === 'token' || event.key === 'user') {
      console.log('Storage changed, reinitializing auth')
      authStore.initAuth()
    }
  })
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@300;400;500;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css');

body {
  font-family: 'Noto Sans TC', sans-serif;
  background-color: #f4f4f4;
  margin: 0;
  padding: 0;
}

.main-container {
  padding: 0 !important;
}

.v-application .v-main__wrap {
  display: block !important;
  width: 100% !important;
}

/* 標題樣式 */
h1, h2, h3, h4, h5, h6 {
  font-weight: 500;
  margin-bottom: 0.8rem;
}

/* 鏈接樣式 */
a {
  text-decoration: none;
  color: #1976D2;
}

/* 卡片樣式 */
.v-card {
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
}
</style>