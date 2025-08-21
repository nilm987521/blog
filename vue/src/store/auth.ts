import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import AuthService from '../services/auth.service'
import router from '../router'
import {User} from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed<boolean>(() => !!token.value)
  const isAdmin = computed<boolean>(() => {
    if (!user.value || !user.value.roles) return false
    return user.value.roles.includes('ROLE_ADMIN')
  })

  // 初始化身份驗證狀態
  function initAuth(): void {
    console.log('Initializing auth state')
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken && storedUser) {
      try {
        token.value = storedToken
        user.value = JSON.parse(storedUser) as User
        console.log('Auth state restored from localStorage')
        console.log('Token:', token.value)
        console.log('User:', user.value)
        
        // 驗證令牌是否有效
        // 使用延遲以避免在 app 初始化時呼叫 API
        setTimeout(() => {
          // 可以在這裡加入驗證 token 有效性的邏輯
          verifyToken()
        }, 500)
      } catch (e) {
        console.error('Error parsing stored user data:', e)
        // 重置失效的資料
        token.value = null
        user.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    } else {
      console.log('No auth state found in localStorage')
      // 確保狀態一致性
      token.value = null
      user.value = null
    }
  }

  // 驗證 token
  async function verifyToken(): Promise<boolean> {
    if (!token.value) return false
    
    try {
      // 這裡可以添加一個對後端的驗證請求
      // 例如，獲取當前用戶資料
      // 如果 token 無效，服務器會返回 401
      console.log('Verifying token validity...')
      return true
    } catch (err) {
      console.error('Token verification failed:', err)
      // 如果令牌無效，則清除身份驗證狀態
      token.value = null
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      return false
    }
  }

  // 登錄
  async function login(username: string, password: string): Promise<any> { // 更改返回類型為 any
    loading.value = true
    error.value = null
    
    try {
      const response = await AuthService.login(username, password)
      console.log('Raw login response:', response)
      
      // 從伺服器回應中提取 token
      // JwtResponse 格式
      if (response.token) {
        token.value = response.token
      } else if (response.accessToken) { // 相容 AuthResponse 格式
        token.value = response.accessToken
        console.log('Using accessToken field instead of token')
      } else {
        console.error('No token found in response!', response)
        throw new Error('Login response did not contain a valid token')
      }
      
      // 從回應中提取用戶信息
      if (response.user) {
      // AuthResponse 格式
      user.value = response.user
      } else if (response.id && response.username) {
      // JwtResponse 格式
      user.value = {
      id: response.id,
      username: response.username,
      email: response.email || '',
      fullName: response.fullName || '',
        profileImage: response.profileImage || '',
          roles: response.roles || []
            }
          } else {
        console.error('Invalid user data in response:', response)
        throw new Error('Login response did not contain valid user data')
      }
      
      // 保存到 localStorage
      if (token.value) {
        localStorage.setItem('token', token.value)
      }

      if (user.value) {
        localStorage.setItem('user', JSON.stringify(user.value))
      }

      console.log('Login successful, token saved:', token.value)
      console.log('User info:', user.value)
      
      return response
    } catch (err: any) {
      console.error('Login error full details:', err)
      error.value = err.response?.data?.message || '登入失敗，請檢查您的用戶名和密碼'
      console.error('Login failed:', error.value)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // Google 登入
  async function googleLogin(code: string): Promise<any> {
    loading.value = true
    error.value = null
    
    try {
      const response = await AuthService.googleLogin(code)
      console.log('Google login response:', response)
      
      // 從伺服器回應中提取 token
      if (response.token) {
        token.value = response.token
      } else if (response.accessToken) {
        token.value = response.accessToken
      } else {
        console.error('No token found in Google login response!', response)
        throw new Error('Google login response did not contain a valid token')
      }
      
      // 從回應中提取用戶信息
      if (response.user) {
        user.value = response.user
      } else if (response.id && response.username) {
        user.value = {
          id: response.id,
          username: response.username,
          email: response.email || '',
          fullName: response.fullName || '',
          profileImage: response.profileImage || '',
          roles: response.roles || []
        }
      } else {
        console.error('Invalid user data in Google login response:', response)
        throw new Error('Google login response did not contain valid user data')
      }
      
      // 保存到 localStorage
      if (token.value) {
        localStorage.setItem('token', token.value)
      }

      if (user.value) {
        localStorage.setItem('user', JSON.stringify(user.value))
      }

      console.log('Google login successful, token saved:', token.value)
      console.log('User info:', user.value)
      
      return response
    } catch (err: any) {
      console.error('Google login error full details:', err)
      error.value = err.response?.data?.message || 'Google 登入失敗'
      console.error('Google login failed:', error.value)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 登出
  function logout(): void {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    console.log('User logged out, auth state cleared')
    router.push('/login')
  }

  // 註冊
  async function register(userData: { username: string; email: string; password: string }): Promise<any> {
    loading.value = true
    error.value = null
    
    try {
      return await AuthService.register(userData)
    } catch (err: any) {
      error.value = err.response?.data?.message || '註冊失敗，請稍後再試'
      throw error.value
    } finally {
      loading.value = false
    }
  }

  return {
    user,
    token,
    loading,
    error,
    isAuthenticated,
    isAdmin,
    initAuth,
    verifyToken,
    login,
    googleLogin,
    register,
    logout
  }
})