import axios from './axios.config';
import { Comment } from '../types';

// CommentService 的請求類型
export interface CreateCommentRequest {
  content: string;
  postId: number;
}

export interface UpdateCommentRequest {
  content: string;
}

const API_URL = '/comments';

class CommentService {
  async getAllComments(): Promise<Comment[]> {
    const response = await axios.get(API_URL);
    return response.data.content;
  }
  
  async getCommentsByPost(postId: number): Promise<Comment[]> {
    const response = await axios.get(`/api/posts/${postId}/comments`);
    return response.data;
  }

  async createComment(commentData: CreateCommentRequest): Promise<Comment> {
    const response = await axios.post(API_URL, commentData);
    return response.data;
  }

  async updateComment(id: number, commentData: UpdateCommentRequest): Promise<Comment> {
    const response = await axios.put(`${API_URL}/${id}`, commentData);
    return response.data;
  }

  async deleteComment(id: number): Promise<any> {
    const response = await axios.delete(`${API_URL}/${id}`);
    return response.data;
  }
}

export default new CommentService();
