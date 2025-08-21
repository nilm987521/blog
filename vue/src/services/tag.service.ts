import axios from './axios.config'
import { Tag } from '../types'
import { TagServiceInterface, CreateTagRequest } from '../types/services'

const API_URL = '/tags'

class TagService implements TagServiceInterface {
  async getAllTags(): Promise<Tag[]> {
    const response = await axios.get(API_URL)
    return response.data
  }

  async getTag(id: number): Promise<Tag> {
    const response = await axios.get(`${API_URL}/${id}`)
    return response.data
  }

  async createTag(tagData: CreateTagRequest): Promise<Tag> {
    const response = await axios.post(API_URL, tagData)
    return response.data
  }

  async updateTag(id: number, tagData: CreateTagRequest): Promise<Tag> {
    const response = await axios.put(`${API_URL}/${id}`, tagData)
    return response.data
  }

  async deleteTag(id: number): Promise<void> {
    const response = await axios.delete(`${API_URL}/${id}`)
    return response.data
  }
}

export default new TagService()
