import axios from './axios.config'
import { FileUploadResponse } from '../types'
import { UploadServiceInterface } from '../types/services'

const API_URL = '/files'

class UploadService implements UploadServiceInterface {
  async uploadFile(file: File): Promise<FileUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    const response = await axios.post(`${API_URL}/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data
  }
}

export default new UploadService()
