import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import PostService from '../services/post.service'
import type { Post, PageResponse } from '../types'

interface PostData {
  title: string;
  content: string;
  categoryId: number;
  tagIds: number[];
  published: boolean;
}

export const usePostStore = defineStore('post', () => {
  const posts = ref<Post[]>([])
  const currentPost = ref<Post | null>(null)
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)
  const totalPages = ref<number>(0)
  const currentPage = ref<number>(0)

  const isPaginationAvailable = computed<boolean>(() => totalPages.value > 1)

  // 獲取所有文章
  async function fetchPosts(
    page: number = 0, 
    size: number = 10, 
    sortBy: string = 'createdAt', 
    direction: string = 'desc'
  ): Promise<PageResponse<Post>> {
    loading.value = true
    error.value = null
    
    try {
      const response = await PostService.getAllPosts(page, size, sortBy, direction)
      posts.value = response.content
      totalPages.value = response.totalPages
      currentPage.value = response.number
      return response
    } catch (err: any) {
      error.value = '無法獲取文章列表'
      console.error(err)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 獲取單篇文章詳情
  async function fetchPostById(id: number | string): Promise<Post> {
    loading.value = true
    error.value = null
    
    try {
      const response = await PostService.getPost(id)
      currentPost.value = response
      return response
    } catch (err: any) {
      error.value = '無法獲取文章詳情'
      console.error(err)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 創建新文章
  async function createPost(postData: PostData): Promise<Post> {
    loading.value = true
    error.value = null
    
    try {
      const response = await PostService.createPost(postData)
      // 更新文章列表
      if (posts.value.length > 0) {
        await fetchPosts(currentPage.value)
      }
      return response
    } catch (err: any) {
      error.value = '創建文章失敗'
      console.error(err)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 更新文章
  async function updatePost(id: number | string, postData: PostData): Promise<Post> {
    loading.value = true
    error.value = null
    
    try {
      const response = await PostService.updatePost(id, postData)
      
      // 更新當前文章
      if (currentPost.value && currentPost.value.id === id) {
        currentPost.value = response
      }
      
      // 更新文章列表
      if (posts.value.length > 0) {
        const index = posts.value.findIndex(post => post.id === id)
        if (index !== -1) {
          posts.value[index] = response
        }
      }
      
      return response
    } catch (err: any) {
      error.value = '更新文章失敗'
      console.error(err)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 刪除文章
  async function deletePost(id: number | string): Promise<void> {
    loading.value = true
    error.value = null
    
    try {
      await PostService.deletePost(id)
      
      // 從列表中移除
      posts.value = posts.value.filter(post => post.id !== id)
      
      // 如果當前查看的文章被刪除，清空當前文章
      if (currentPost.value && currentPost.value.id === id) {
        currentPost.value = null
      }
    } catch (err: any) {
      error.value = '刪除文章失敗'
      console.error(err)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 按分類獲取文章
  async function fetchPostsByCategory(
    categoryId: number, 
    page: number = 0, 
    size: number = 10
  ): Promise<PageResponse<Post>> {
    loading.value = true
    error.value = null
    
    try {
      const response = await PostService.getPostsByCategory(categoryId, page, size)
      posts.value = response.content
      totalPages.value = response.totalPages
      currentPage.value = response.number
      return response
    } catch (err: any) {
      error.value = '無法獲取分類文章'
      console.error(err)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 按標籤獲取文章
  async function fetchPostsByTag(
    tagId: number, 
    page: number = 0, 
    size: number = 10
  ): Promise<PageResponse<Post>> {
    loading.value = true
    error.value = null
    
    try {
      const response = await PostService.getPostsByTag(tagId, page, size)
      posts.value = response.content
      totalPages.value = response.totalPages
      currentPage.value = response.number
      return response
    } catch (err: any) {
      error.value = '無法獲取標籤文章'
      console.error(err)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 搜索文章
  async function searchPosts(
    query: string, 
    page: number = 0, 
    size: number = 10
  ): Promise<PageResponse<Post>> {
    loading.value = true
    error.value = null
    
    try {
      const response = await PostService.searchPosts(query, page, size)
      posts.value = response.content
      totalPages.value = response.totalPages
      currentPage.value = response.number
      return response
    } catch (err: any) {
      error.value = '搜索文章失敗'
      console.error(err)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 按作者獲取文章
  async function fetchPostsByUser(
    userId: number, 
    page: number = 0, 
    size: number = 10
  ): Promise<PageResponse<Post>> {
    loading.value = true
    error.value = null
    
    try {
      const response = await PostService.getPostsByUser(userId, page, size)
      posts.value = response.content
      totalPages.value = response.totalPages
      currentPage.value = response.number
      return response
    } catch (err: any) {
      error.value = '無法獲取用戶文章'
      console.error(err)
      throw error.value
    } finally {
      loading.value = false
    }
  }

  // 重置狀態
  function reset(): void {
    posts.value = []
    currentPost.value = null
    loading.value = false
    error.value = null
    totalPages.value = 0
    currentPage.value = 0
  }

  return {
    posts,
    currentPost,
    loading,
    error,
    totalPages,
    currentPage,
    isPaginationAvailable,
    fetchPosts,
    fetchPostById,
    createPost,
    updatePost,
    deletePost,
    fetchPostsByCategory,
    fetchPostsByTag,
    searchPosts,
    fetchPostsByUser,
    reset
  }
})