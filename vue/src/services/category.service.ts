import axios from './axios.config'
import { Category } from '@/types'
import { CategoryServiceInterface } from '@/types/services'

const API_URL = '/categories'

class CategoryService implements CategoryServiceInterface {
  async getAllCategories(): Promise<Category[]> {
    const response = await axios.get(API_URL)
    return response.data
  }

  async getCategory(id: number): Promise<Category> {
    const response = await axios.get(`${API_URL}/${id}`)
    return response.data
  }

  async createCategory(categoryData: { name: string; description?: string }): Promise<Category> {
    const response = await axios.post(API_URL, categoryData)
    return response.data
  }

  async updateCategory(id: number, categoryData: { name: string; description?: string }): Promise<Category> {
    const response = await axios.put(`${API_URL}/${id}`, categoryData)
    return response.data
  }

  async deleteCategory(id: number): Promise<void> {
    const response = await axios.delete(`${API_URL}/${id}`)
    return response.data
  }
}

export default new CategoryService()
