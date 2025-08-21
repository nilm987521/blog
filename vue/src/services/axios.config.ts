import axios, { AxiosRequestConfig, AxiosResponse, AxiosError, InternalAxiosRequestConfig } from 'axios';

// 設置基礎 URL - 使用相對路徑，nginx會處理代理
axios.defaults.baseURL = '/api';

// 請求攔截器
axios.interceptors.request.use(
  (config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
    // 從 localStorage 直接獲取 token，而不是依賴 Pinia
    const token = localStorage.getItem('token');
    
    console.log('Request interceptor token from localStorage:', token);
    console.log('Request URL:', config.url, 'Method:', config.method);
    
    // 打印請求數據
    if (config.data) {
      console.log('Request data before processing:', config.data);
      
      // 特殊處理為對象的內容字段
      if (config.data.content && typeof config.data.content === 'object') {
        console.log('Converting content object to string', config.data.content);
        if (config.data.content.html) {
          config.data.content = config.data.content.html;
        } else if (config.data.content.text) {
          config.data.content = config.data.content.text;
        } else {
          config.data.content = JSON.stringify(config.data.content);
        }
      }
      
      console.log('Request data after processing:', config.data);
    }

    // 如過是使用gitlab api，就不注入JWT Token
    if (config.url?.includes("gitlab.nilm.cc")) {
        return config;
    }

    // 如果 token 存在且有效，將其添加到所有請求的頭部
    if (token && token.trim() !== '' && token !== 'null' && token !== 'undefined') {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('Authorization header set:', config.headers.Authorization);
    } else {
      console.log('No token found in localStorage, skipping Authorization header');
      // 確保沒有Authorization頭部
      delete config.headers.Authorization;
    }
    
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// 響應攔截器
axios.interceptors.response.use(
  (response: AxiosResponse): AxiosResponse => {
    console.log('Response from server:', response.config.url, response.status);
    return response;
  },
  (error: AxiosError) => {
    console.error('Error in response:', error.config?.url, error.response?.status, error.message);
    
    // 處理 401 未授權錯誤
    if (error.response && error.response.status === 401) {
      console.log('401 Unauthorized error detected');
      
      // 如果是 GitLab API 的請求，不進行重定向
      const url = error.config?.url || '';
      if (url.includes('gitlab.nilm.cc')) {
        console.log('GitLab API 401 error, not redirecting');
        return Promise.reject(error);
      }
      
      // 檢查是否為公共API端點（應該允許匿名訪問）
      const publicEndpoints = ['/api/posts', '/api/categories', '/api/tags', '/api/comments'];
      const isPublicEndpoint = publicEndpoints.some(endpoint => url.includes(endpoint));
      
      if (isPublicEndpoint) {
        console.log(`401 error on public endpoint ${url}, not redirecting`);
        return Promise.reject(error);
      }
      
      // 檢查目前页面路徑
      const currentPath = window.location.pathname;
      if (currentPath === '/about' || currentPath === '/login') {
        console.log(`Ignoring 401 error on ${currentPath} page`);
        return Promise.reject(error);
      }
      
      // 防止重複重定向的機制
      const isRedirecting = sessionStorage.getItem('auth_redirecting');
      if (isRedirecting) {
        console.log('Already redirecting, skipping additional redirect');
        return Promise.reject(error);
      }
      
      console.log('Clearing auth data and redirecting to login');
      // 設置重定向標記
      sessionStorage.setItem('auth_redirecting', 'true');
      
      // 清除localStorage
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      // 延遲重定向以防止競態條件
      setTimeout(() => {
        sessionStorage.removeItem('auth_redirecting');
        window.location.href = '/login';
      }, 100);
    }
    
    // 處理 500 錯誤
    if (error.response && error.response.status === 500) {
      console.error('500 Internal Server Error:', error.response.data);
      // 您可以在這裡添加特定的 500 錯誤處理邏輯
    }
    
    return Promise.reject(error);
  }
);

export default axios;