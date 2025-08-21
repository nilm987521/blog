// 用戶接口
export interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
  fullName?: string;
  bio?: string;
  profileImage?: string;
  avatar?: string; // 兼容舊屬性
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// 分類接口
export interface Category {
  id: number;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

// 標籤接口
export interface Tag {
  id: number;
  name: string;
  color?: string; // 新增標籤顏色屬性
  createdAt?: string;
  updatedAt?: string;
}

// 作者接口(簡化的用戶接口)
export interface Author {
  id: number;
  username: string;
  avatar?: string;
}

// 評論接口
export interface Comment {
  id: number;
  content: string;
  user: User;
  postId: number;
  createdAt?: string;
  updatedAt?: string;
}

// 文章接口
export interface Post {
  id: number;
  title: string;
  content: string;
  author: Author;
  category?: Category;
  tags?: Tag[];
  comments?: Comment[];
  featuredImage?: string;
  published: boolean;
  viewCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

// 分頁回應接口
export interface PageResponse<T> {
  content: T[];
  pageable: {
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    pageNumber: number;
    pageSize: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

// 通用響應接口
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: any;
}

// 文件上傳響應
export interface FileUploadResponse {
  fileName: string;
  fileDownloadUri: string;
  fileType: string;
  size: number;
}

// 認證請求/回應接口
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: User;
}
