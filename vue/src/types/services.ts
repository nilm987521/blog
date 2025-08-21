import { Category, Tag, PageResponse, FileUploadResponse, User, Comment } from '../types'
import { CreateCommentRequest, UpdateCommentRequest } from '../services/comment.service';
import { UpdateUserRequest, ChangePasswordRequest } from '../services/user.service';

export interface CreateTagRequest {
  name: string;
  color?: string;
}

export interface CategoryServiceInterface {
  getAllCategories(): Promise<Category[]>;
  getCategory(id: number): Promise<Category>;
  createCategory(categoryData: { name: string; description?: string }): Promise<Category>;
  updateCategory(id: number, categoryData: { name: string; description?: string }): Promise<Category>;
  deleteCategory(id: number): Promise<void>;
}

export interface TagServiceInterface {
  getAllTags(): Promise<Tag[]>;
  getTag(id: number): Promise<Tag>;
  createTag(tagData: CreateTagRequest): Promise<Tag>;
  updateTag(id: number, tagData: CreateTagRequest): Promise<Tag>;
  deleteTag(id: number): Promise<void>;
}

export interface UploadServiceInterface {
  uploadFile(file: File): Promise<FileUploadResponse>;
}

export interface CommentServiceInterface {
  getAllComments(): Promise<Comment[]>;
  getCommentsByPost(postId: number): Promise<Comment[]>;
  createComment(commentData: CreateCommentRequest): Promise<Comment>;
  updateComment(id: number, commentData: UpdateCommentRequest): Promise<Comment>;
  deleteComment(id: number): Promise<any>;
}

export interface UserServiceInterface {
  getAllUsers(): Promise<User[]>;
  getUser(id: number): Promise<User>;
  updateUser(id: number, userData: UpdateUserRequest): Promise<User>;
  deleteUser(id: number): Promise<any>;
  changePassword(id: number, passwordData: ChangePasswordRequest): Promise<any>;
}
