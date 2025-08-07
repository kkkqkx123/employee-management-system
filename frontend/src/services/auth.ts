// Authentication service

import ApiService, { setAuthToken } from './api';
import { API_ENDPOINTS } from '../types/api';
import type { LoginRequest, LoginResponse, RegisterRequest, User, TokenPayload } from '../types/auth';

export class AuthService {
  /**
   * Login user with credentials
   */
  static async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await ApiService.post<LoginResponse>(API_ENDPOINTS.AUTH.LOGIN, credentials);
      
      if (response.success && response.data) {
        // Store tokens
        setAuthToken(response.data.token);
        if (credentials.rememberMe) {
          localStorage.setItem('refresh_token', response.data.token); // In real app, this would be a separate refresh token
        }
        
        // Store user data
        localStorage.setItem('user_data', JSON.stringify(response.data.user));
        localStorage.setItem('user_permissions', JSON.stringify(response.data.permissions));
        
        return response.data;
      }
      
      throw new Error(response.message || 'Login failed');
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  /**
   * Register new user
   */
  static async register(userData: RegisterRequest): Promise<User> {
    try {
      const response = await ApiService.post<User>('/auth/register', userData);
      
      if (response.success && response.data) {
        return response.data;
      }
      
      throw new Error(response.message || 'Registration failed');
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  }

  /**
   * Logout user
   */
  static async logout(): Promise<void> {
    try {
      // Call logout endpoint to invalidate token on server
      await ApiService.post(API_ENDPOINTS.AUTH.LOGOUT);
    } catch (error) {
      console.error('Logout error:', error);
      // Continue with local logout even if server call fails
    } finally {
      // Clear local storage
      setAuthToken(null);
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('user_data');
      localStorage.removeItem('user_permissions');
    }
  }

  /**
   * Validate current token
   */
  static async validateToken(): Promise<boolean> {
    try {
      const response = await ApiService.get<boolean>(API_ENDPOINTS.AUTH.VALIDATE);
      return response.success && response.data;
    } catch (error) {
      console.error('Token validation error:', error);
      return false;
    }
  }

  /**
   * Refresh authentication token
   */
  static async refreshToken(): Promise<string> {
    try {
      const refreshToken = localStorage.getItem('refresh_token');
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await ApiService.post<{ token: string }>(API_ENDPOINTS.AUTH.REFRESH, {
        refreshToken,
      });

      if (response.success && response.data) {
        setAuthToken(response.data.token);
        return response.data.token;
      }

      throw new Error(response.message || 'Token refresh failed');
    } catch (error) {
      console.error('Token refresh error:', error);
      // Clear invalid tokens
      this.logout();
      throw error;
    }
  }

  /**
   * Get current user from localStorage
   */
  static getCurrentUser(): User | null {
    try {
      const userData = localStorage.getItem('user_data');
      return userData ? JSON.parse(userData) : null;
    } catch (error) {
      console.error('Error getting current user:', error);
      return null;
    }
  }

  /**
   * Get current user permissions from localStorage
   */
  static getCurrentPermissions(): string[] {
    try {
      const permissions = localStorage.getItem('user_permissions');
      return permissions ? JSON.parse(permissions) : [];
    } catch (error) {
      console.error('Error getting current permissions:', error);
      return [];
    }
  }

  /**
   * Check if user has specific permission
   */
  static hasPermission(permission: string): boolean {
    const permissions = this.getCurrentPermissions();
    return permissions.includes(permission);
  }

  /**
   * Check if user has any of the specified permissions
   */
  static hasAnyPermission(permissions: string[]): boolean {
    const userPermissions = this.getCurrentPermissions();
    return permissions.some(permission => userPermissions.includes(permission));
  }

  /**
   * Check if user has all of the specified permissions
   */
  static hasAllPermissions(permissions: string[]): boolean {
    const userPermissions = this.getCurrentPermissions();
    return permissions.every(permission => userPermissions.includes(permission));
  }

  /**
   * Decode JWT token (client-side only, for UI purposes)
   */
  static decodeToken(token: string): TokenPayload | null {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  /**
   * Check if token is expired
   */
  static isTokenExpired(token: string): boolean {
    const decoded = this.decodeToken(token);
    if (!decoded) return true;
    
    const currentTime = Date.now() / 1000;
    return decoded.exp < currentTime;
  }

  /**
   * Get token from localStorage
   */
  static getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  /**
   * Check if user is authenticated
   */
  static isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    
    return !this.isTokenExpired(token);
  }
}

export default AuthService;