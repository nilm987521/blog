import axios from './axios.config'
import type { Post, PageResponse } from '@/types'

const API_URL = '/posts'

interface PostData {
  title: string;
  content: string;
  categoryId: number;
  tagIds: number[];
  published: boolean;
}

class PostService {
  async getAllPosts(
    page: number = 0, 
    size: number = 10, 
    sortBy: string = 'createdAt', 
    direction: string = 'desc'
  ): Promise<PageResponse<Post>> {
    const response = await axios.get(API_URL, {
      params: { page, size, sortBy, direction }
    })
    return response.data
  }

  async getPost(id: number | string): Promise<Post> {
    const response = await axios.get(`${API_URL}/${id}`)
    return response.data
  }

  async createPost(postData: PostData): Promise<Post> {
    console.log('PostService - 創建文章数据:', postData)
    const response = await axios.post(API_URL, postData)
    return response.data
  }

  async updatePost(id: number | string, postData: PostData): Promise<Post> {
    console.log('PostService - 更新文章数据:', id, postData)
    const response = await axios.put(`${API_URL}/${id}`, postData)
    return response.data
  }

  async deletePost(id: number | string): Promise<any> {
    const response = await axios.delete(`${API_URL}/${id}`)
    return response.data
  }

  async getPostsByCategory(
    categoryId: number, 
    page: number = 0, 
    size: number = 10
  ): Promise<PageResponse<Post>> {
    const response = await axios.get(`/api/posts/category/${categoryId}`, {
      params: { page, size }
    })
    return response.data
  }

  async getPostsByTag(
    tagId: number, 
    page: number = 0, 
    size: number = 10
  ): Promise<PageResponse<Post>> {
    const response = await axios.get(`/api/posts/tag/${tagId}`, {
      params: { page, size }
    })
    return response.data
  }

  async searchPosts(
    query: string, 
    page: number = 0, 
    size: number = 10
  ): Promise<PageResponse<Post>> {
    const response = await axios.get(`/api/posts/search`, {
      params: { query, page, size }
    })
    return response.data
  }

  async getPostsByUser(
    userId: number, 
    page: number = 0, 
    size: number = 10
  ): Promise<PageResponse<Post>> {
    const response = await axios.get(`/api/posts/user/${userId}`, {
      params: { page, size }
    })
    return response.data
  }
}

export default new PostService()
