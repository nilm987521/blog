<template>
  <div class="callback-container">
    <v-container>
      <v-row justify="center" align="center" style="min-height: 60vh;">
        <v-col cols="12" md="6" class="text-center">
          <v-card elevation="3" class="pa-5">
            <v-progress-circular v-if="loading" indeterminate color="primary" size="64"></v-progress-circular>
            
            <div v-if="!loading && !error" class="text-center">
              <v-icon color="success" size="64" class="mb-4">mdi-check-circle</v-icon>
              <h2 class="text-h4 font-weight-bold mb-4">登入成功</h2>
              <p class="mb-4">您已經成功登入，正在將您重定向到首頁...</p>
            </div>
            
            <div v-if="error" class="text-center">
              <v-icon color="error" size="64" class="mb-4">mdi-alert-circle</v-icon>
              <h2 class="text-h4 font-weight-bold mb-4">登入失敗</h2>
              <p class="mb-4">{{ error }}</p>
              <v-btn color="primary" @click="goToLogin">返回登入頁面</v-btn>
            </div>
            
            <div v-if="debugInfo" class="mt-4 text-left">
              <v-alert type="info" variant="tonal" class="text-caption">
                <pre>{{ debugInfo }}</pre>
              </v-alert>
            </div>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(true)
const error = ref(null)
const debugInfo = ref('')

onMounted(async () => {
  try {
    debugInfo.value = '在回調頁面初始化...\n'
    debugInfo.value += `完整URL: ${window.location.href}\n`
    
    const urlParams = new URLSearchParams(window.location.search)
    const code = urlParams.get('code')
    const state = urlParams.get('state')
    
    debugInfo.value += `授權碼: ${code ? '已獲取' : '缺失'}\n`
    debugInfo.value += `狀態: ${state || '無'}\n`
    
    if (!code) {
      throw new Error('授權代碼缺失')
    }
    
    // 清除URL參數，立即將URL變為乾淨的形式
    window.history.replaceState({}, document.title, '/oauth/callback')
    debugInfo.value += '已清除URL參數\n'
    
    // 將授權碼發送到後端進行處理
    debugInfo.value += '嘗試發送授權碼到後端...\n'
    const response = await authStore.googleLogin(code)
    debugInfo.value += '後端處理成功\n'
    debugInfo.value += `回應: ${JSON.stringify(response)}\n`
    
    // 延遲跳轉，讓用戶看到成功訊息
    debugInfo.value += '準備重定向到首頁...\n'
    setTimeout(() => {
      const redirectUrl = localStorage.getItem('auth_redirect_url') || '/'
      localStorage.removeItem('auth_redirect_url')
      debugInfo.value += `將重定向到: ${redirectUrl}\n`
      router.push(redirectUrl)
    }, 1500)
  } catch (err) {
    debugInfo.value += `登入錯誤: ${err.toString()}\n`
    if (err.response) {
      debugInfo.value += `錯誤回應: ${JSON.stringify(err.response.data)}\n`
    }
    error.value = err.toString() || '登入過程中發生錯誤'
    loading.value = false
  }
})

function goToLogin() {
  router.push('/login')
}
</script>

<style scoped>
.callback-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px 0;
}
</style>