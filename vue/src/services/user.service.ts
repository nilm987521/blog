import axios from './axios.config';
import { User } from '../types';

// UserService 的請求類型
export interface UpdateUserRequest {
  username?: string;
  email?: string;
  avatar?: string;
  roles?: string[];
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// UserService 的服務介面
export interface UserServiceInterface {
  getAllUsers(): Promise<User[]>;
  getUser(id: number): Promise<User>;
  updateUser(id: number, userData: UpdateUserRequest): Promise<User>;
  deleteUser(id: number): Promise<any>;
  changePassword(id: number, passwordData: ChangePasswordRequest): Promise<any>;
}

const API_URL = '/users';

class UserService implements UserServiceInterface {
  async getAllUsers(): Promise<User[]> {
    const response = await axios.get(API_URL);
    return response.data;
  }

  async getUser(id: number): Promise<User> {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
  }

  async updateUser(id: number, userData: UpdateUserRequest): Promise<User> {
    const response = await axios.put(`${API_URL}/${id}`, userData);
    return response.data;
  }

  async deleteUser(id: number): Promise<any> {
    const response = await axios.delete(`${API_URL}/${id}`);
    return response.data;
  }

  async changePassword(id: number, passwordData: ChangePasswordRequest): Promise<any> {
    const response = await axios.post(`${API_URL}/${id}/change-password`, passwordData);
    return response.data;
  }
}

export default new UserService();
