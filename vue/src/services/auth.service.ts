import axios from './axios.config'
import { AuthResponse, LoginRequest, RegisterRequest } from '@/types'

const API_URL = '/auth'

class AuthService {
  async login(username: string, password: string): Promise<any> { // 將返回類型改為 any
    const response = await axios.post(`${API_URL}/signin`, {
      username,
      password
    } as LoginRequest)
    return response.data
  }

  async register(userData: RegisterRequest): Promise<any> {
    const response = await axios.post(`${API_URL}/signup`, userData)
    return response.data
  }

  async googleLogin(code: string): Promise<any> {
    const response = await axios.post(`${API_URL}/oauth2/google/callback`, {
      code: code
    })
    return response.data
  }
}

export default new AuthService()
