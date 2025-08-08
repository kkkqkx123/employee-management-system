// API client configuration and base service

import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, AxiosError, AxiosHeaders } from 'axios';
import { API_BASE_URL } from '../constants';
import type { ApiResponse, ApiError, RequestConfig } from '../types/api';
import { CSRFProtection } from '../utils/csrfProtection';
import { RateLimiter, SecurityUtils } from '../utils/security';
import { createTokenStorage, TokenSecurity } from '../utils/tokenSecurity';

// Create axios instance with security headers
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest', // CSRF protection
  },
  withCredentials: true, // Include cookies for CSRF protection
});

// Secure token management
const tokenStorage = createTokenStorage();

export const setAuthToken = (token: string | null) => {
  if (token) {
    // Validate token format before setting
    if (TokenSecurity.isValidJWTFormat(token)) {
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      tokenStorage.setToken(token);
    } else {
      console.warn('Invalid JWT token format');
      return;
    }
  } else {
    delete apiClient.defaults.headers.common['Authorization'];
    tokenStorage.removeToken();
  }
};

// Initialize token from secure storage
const storedToken = tokenStorage.getToken();
if (storedToken) {
  setAuthToken(storedToken);
}

// Request interceptor with security measures
apiClient.interceptors.request.use(
  (config) => {
    const method = config.method?.toUpperCase() || 'GET';
    const url = config.url || '';

    // Rate limiting check
    const rateLimitKey = `api_${method}_${url}`;
    if (!RateLimiter.isAllowed(rateLimitKey, 100, 60000)) { // 100 requests per minute
      return Promise.reject(new Error('Rate limit exceeded'));
    }

    // Add CSRF protection for state-changing operations
    if (CSRFProtection.requiresProtection(method)) {
      const csrfHeaders = CSRFProtection.getHeaders();
      const headers = new AxiosHeaders(config.headers);
      Object.entries(csrfHeaders).forEach(([key, value]) => {
        headers.set(key, value);
      });
      config.headers = headers;
    }

    // Sanitize request data
    if (config.data && typeof config.data === 'object') {
      config.data = SecurityUtils.sanitizeFormData(config.data);
    }

    // Add timestamp to prevent caching for GET requests
    if (method === 'GET') {
      config.params = {
        ...config.params,
        _t: Date.now(),
      };
    }

    // Add security headers
    const securityHeaders = new AxiosHeaders(config.headers);
    securityHeaders.set('X-Content-Type-Options', 'nosniff');
    securityHeaders.set('X-Frame-Options', 'DENY');
    securityHeaders.set('X-XSS-Protection', '1; mode=block');
    config.headers = securityHeaders;

    // Log request in development
    if (import.meta.env.DEV) {
      console.log(`🚀 API Request: ${method} ${url}`, {
        params: config.params,
        data: config.data,
        headers: config.headers,
      });
    }

    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor
apiClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // Log response in development
    if (import.meta.env.DEV) {
      console.log(`✅ API Response: ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data);
    }

    return response;
  },
  async (error: AxiosError<ApiError>) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

    // Log error in development
    if (import.meta.env.DEV) {
      console.error(`❌ API Error: ${error.config?.method?.toUpperCase()} ${error.config?.url}`, {
        status: error.response?.status,
        data: error.response?.data,
      });
    }

    // Handle 401 errors (unauthorized)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Try to refresh token
        const refreshToken = tokenStorage.getRefreshToken();
        if (refreshToken) {
          const response = await axios.post(`${API_BASE_URL}/auth/refresh-token`, {
            refreshToken,
          });

          const { token } = response.data.data;
          setAuthToken(token);

          // Retry original request
          if (originalRequest.headers) {
            originalRequest.headers['Authorization'] = `Bearer ${token}`;
          }
          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, clear tokens and redirect to login
        setAuthToken(null);
        tokenStorage.removeRefreshToken();
        CSRFProtection.clearToken();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // Handle other errors
    const apiError: ApiError = {
      success: false,
      message: error.response?.data?.message || error.message || 'An unexpected error occurred',
      errors: error.response?.data?.errors,
      timestamp: new Date().toISOString(),
      path: error.config?.url,
    };

    return Promise.reject(apiError);
  }
);

// Generic API methods
export class ApiService {
  static async get<T>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    const response = await apiClient.get<ApiResponse<T>>(url, config);
    return response.data;
  }

  static async post<T>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>> {
    const response = await apiClient.post<ApiResponse<T>>(url, data, config);
    return response.data;
  }

  static async put<T>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>> {
    const response = await apiClient.put<ApiResponse<T>>(url, data, config);
    return response.data;
  }

  static async patch<T>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>> {
    const response = await apiClient.patch<ApiResponse<T>>(url, data, config);
    return response.data;
  }

  static async delete<T>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    const response = await apiClient.delete<ApiResponse<T>>(url, config);
    return response.data;
  }

  // File upload method
  static async uploadFile<T>(url: string, file: File, onProgress?: (progress: number) => void): Promise<ApiResponse<T>> {
    const formData = new FormData();
    formData.append('file', file);

    const config: AxiosRequestConfig = {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(progress);
        }
      },
    };

    const response = await apiClient.post<ApiResponse<T>>(url, formData, config);
    return response.data;
  }

  // Download file method
  static async downloadFile(url: string, filename?: string): Promise<void> {
    const response = await apiClient.get(url, {
      responseType: 'blob',
    });

    const blob = new Blob([response.data]);
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename || 'download';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  }
}

// Export the configured axios instance for direct use if needed
export { apiClient };
export default ApiService;