<template>
  <v-container style="padding-top: 80px">
    <v-row justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card elevation="3" class="pa-5">
          <v-card-title class="text-h4 font-weight-bold text-center pb-4">
            登入
          </v-card-title>

          <v-alert
              v-if="error"
              type="error"
              class="mb-4"
              closable
              @click:close="error = null"
          >
            {{ error }}
          </v-alert>

          <v-form @submit.prevent="handleLogin" ref="loginForm">
            <v-text-field
                v-model="username"
                label="用戶名"
                prepend-inner-icon="mdi-account"
                variant="outlined"
                :rules="[v => !!v || '用戶名不能為空']"
                required
            ></v-text-field>

            <v-text-field
                v-model="password"
                label="密碼"
                prepend-inner-icon="mdi-lock"
                :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                :type="showPassword ? 'text' : 'password'"
                variant="outlined"
                @click:append-inner="showPassword = !showPassword"
                :rules="[v => !!v || '密碼不能為空']"
                required
            ></v-text-field>

            <v-btn
                type="submit"
                color="primary"
                size="large"
                block
                class="mt-4"
                :loading="loading"
            >
              登入
            </v-btn>
          </v-form>
          
          <div class="text-center my-4">
            <v-divider class="my-3">
              <span class="text-body-2 text-medium-emphasis px-2">或使用以下方式登入</span>
            </v-divider>
            
            <v-btn
              variant="outlined"
              prepend-icon="mdi-google"
              color="error"
              block
              class="mt-3"
              @click="handleGoogleLogin"
              :loading="googleLoading"
            >
              使用 Google 帳號登入
            </v-btn>
          </div>

          <div class="text-center mt-4">
            <p>常見問題？ <a href="#" @click.prevent="showFAQ">查看幫助</a></p>
          </div>

          <div v-if="debugInfo" class="mt-4 pt-2 border-t">
            <v-alert type="info" variant="tonal" class="text-caption">
              <pre>{{ debugInfo }}</pre>
            </v-alert>
          </div>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- FAQ 對話框 -->
    <v-dialog v-model="showFAQDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          常見問題
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" color="white" @click="showFAQDialog = false"></v-btn>
        </v-card-title>
        
        <v-card-text class="pa-4">
          <v-expansion-panels variant="accordion">
            <v-expansion-panel>
              <v-expansion-panel-title>如何申請帳號？</v-expansion-panel-title>
              <v-expansion-panel-text>
                本網站已停止開放註冊，目前只接受使用 Google 帳號登入。如果您需要特殊帳號，請聯繫管理員。
              </v-expansion-panel-text>
            </v-expansion-panel>
            
            <v-expansion-panel>
              <v-expansion-panel-title>忘記密碼怎麼辦？</v-expansion-panel-title>
              <v-expansion-panel-text>
                如果您忘記了密碼，請使用 Google 帳號登入。如果您使用的是已註冊的密碼帳號，請聯繫管理員重設密碼。
              </v-expansion-panel-text>
            </v-expansion-panel>
            
            <v-expansion-panel>
              <v-expansion-panel-title>使用 Google 登入是否安全？</v-expansion-panel-title>
              <v-expansion-panel-text>
                是的，使用 Google 帳號登入是非常安全的。我們不會存取您的 Google 密碼，僅獲取您的基本資訊（如姓名和電子郵件）以認證您的身份。
              </v-expansion-panel-text>
            </v-expansion-panel>
            
            <v-expansion-panel>
              <v-expansion-panel-title>沒有 Google 帳號怎麼辦？</v-expansion-panel-title>
              <v-expansion-panel-text>
                如果您沒有 Google 帳號，您可以免費申請一個。請前往 <a href="https://accounts.google.com/signup" target="_blank">Google 帳號申請頁面</a>。
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
        </v-card-text>
        
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn color="primary" @click="showFAQDialog = false">關閉</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import {ref, computed, watch, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {useAuthStore} from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const loginForm = ref(null)
const error = ref(null)
const debugInfo = ref('')
const googleLoading = ref(false)
const showFAQDialog = ref(false)

const loading = computed(() => authStore.loading)

onMounted(() => {
  // 檢查 URL 中是否有錯誤參數
  const urlParams = new URLSearchParams(window.location.search);
  const errorParam = urlParams.get('error');
  if (errorParam) {
    error.value = `Google 登入失敗: ${errorParam}`;
    // 清除 URL 參數
    history.replaceState({}, document.title, '/login');
  }
})

// 監聽認證狀態變化
watch(() => authStore.isAuthenticated, (newValue) => {
  if (newValue) {
    debugInfo.value += `\n認證狀態變更為: ${newValue}, 用戶: ${authStore.user?.username}`
  }
})

async function handleLogin() {
  const {valid} = await loginForm.value.validate()

  if (!valid) return

  error.value = null
  debugInfo.value = '嘗試登入...'

  try {
    const response = await authStore.login(username.value, password.value)
    debugInfo.value += `\n登入成功! Token: ${authStore.token ? '已獲取' : '缺失'}`

    // 檢查 localStorage 是否正確保存了 token
    const storedToken = localStorage.getItem('token')
    debugInfo.value += `\nLocalStorage token: ${storedToken ? '已存在' : '缺失'}`

    if (!storedToken && authStore.token) {
      // 如果 auth store 已經有 token 但 localStorage 沒有，手動保存
      localStorage.setItem('token', authStore.token)
      debugInfo.value += `\n手動保存 token 成功`
    }

    // 延遲後再次檢查
    setTimeout(() => {
      const checkToken = localStorage.getItem('token')
      debugInfo.value += `\n延遲檢查 token: ${checkToken ? '已存在' : '缺失'}`

      if (checkToken) {
        debugInfo.value += '\n驗證成功，準備跳轉...'
        setTimeout(() => {
          router.push('/')
        }, 1000)
      } else {
        error.value = 'Token 保存失敗，請重試'
      }
    }, 500)
  } catch (err) {
    error.value = err
    debugInfo.value += `\n登入失敗: ${err}`
  }
}

async function handleGoogleLogin() {
  googleLoading.value = true
  error.value = null
  debugInfo.value = '嘗試使用 Google 帳號登入...'
  
  try {
    // 取得當前的主機URL
    const host = window.location.origin
    const redirectUri = `${host}/oauth/callback`
    
    // 直接打開後端的 OAuth2 授權 URL，並指定回調URL
    const googleAuthUrl = `${import.meta.env.VITE_FRONTEND_URL || ''}/api/auth/oauth2/google?redirect_uri=${encodeURIComponent(redirectUri)}`
    debugInfo.value += `\n打開 Google 登入 URL: ${googleAuthUrl}`
    debugInfo.value += `\n重定向 URI: ${redirectUri}`
    
    // 保存需要最終返回的頁面 URL
    localStorage.setItem('auth_redirect_url', '/')
    
    // 直接在當前視窗導航到 Google 登入頁面
    window.location.href = googleAuthUrl
  } catch (err) {
    googleLoading.value = false
    error.value = `Google 登入失敗: ${err.message || err}`
    debugInfo.value += `\nGoogle 登入錯誤: ${err.message || err}`
  }
}

// 顯示常見問題對話框
function showFAQ() {
  showFAQDialog.value = true
}
</script>

<style scoped>
pre {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
