/**
 * Token management utilities for secure JWT handling
 */

const TOKEN_KEY = 'auth_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_DATA_KEY = 'user_data';
const PERMISSIONS_KEY = 'user_permissions';

export class TokenManager {
  /**
   * Store authentication token securely
   */
  static setToken(token: string): void {
    try {
      // In a production app, consider using httpOnly cookies for better security
      localStorage.setItem(TOKEN_KEY, token);
    } catch (error) {
      console.error('Failed to store token:', error);
    }
  }

  /**
   * Get stored authentication token
   */
  static getToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch (error) {
      console.error('Failed to retrieve token:', error);
      return null;
    }
  }

  /**
   * Remove authentication token
   */
  static removeToken(): void {
    try {
      localStorage.removeItem(TOKEN_KEY);
    } catch (error) {
      console.error('Failed to remove token:', error);
    }
  }

  /**
   * Store refresh token
   */
  static setRefreshToken(refreshToken: string): void {
    try {
      localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    } catch (error) {
      console.error('Failed to store refresh token:', error);
    }
  }

  /**
   * Get stored refresh token
   */
  static getRefreshToken(): string | null {
    try {
      return localStorage.getItem(REFRESH_TOKEN_KEY);
    } catch (error) {
      console.error('Failed to retrieve refresh token:', error);
      return null;
    }
  }

  /**
   * Remove refresh token
   */
  static removeRefreshToken(): void {
    try {
      localStorage.removeItem(REFRESH_TOKEN_KEY);
    } catch (error) {
      console.error('Failed to remove refresh token:', error);
    }
  }

  /**
   * Store user data
   */
  static setUserData(userData: any): void {
    try {
      localStorage.setItem(USER_DATA_KEY, JSON.stringify(userData));
    } catch (error) {
      console.error('Failed to store user data:', error);
    }
  }

  /**
   * Get stored user data
   */
  static getUserData(): any | null {
    try {
      const data = localStorage.getItem(USER_DATA_KEY);
      return data ? JSON.parse(data) : null;
    } catch (error) {
      console.error('Failed to retrieve user data:', error);
      return null;
    }
  }

  /**
   * Remove user data
   */
  static removeUserData(): void {
    try {
      localStorage.removeItem(USER_DATA_KEY);
    } catch (error) {
      console.error('Failed to remove user data:', error);
    }
  }

  /**
   * Store user permissions
   */
  static setPermissions(permissions: string[]): void {
    try {
      localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(permissions));
    } catch (error) {
      console.error('Failed to store permissions:', error);
    }
  }

  /**
   * Get stored permissions
   */
  static getPermissions(): string[] {
    try {
      const data = localStorage.getItem(PERMISSIONS_KEY);
      return data ? JSON.parse(data) : [];
    } catch (error) {
      console.error('Failed to retrieve permissions:', error);
      return [];
    }
  }

  /**
   * Remove permissions
   */
  static removePermissions(): void {
    try {
      localStorage.removeItem(PERMISSIONS_KEY);
    } catch (error) {
      console.error('Failed to remove permissions:', error);
    }
  }

  /**
   * Clear all stored authentication data
   */
  static clearAll(): void {
    this.removeToken();
    this.removeRefreshToken();
    this.removeUserData();
    this.removePermissions();
  }

  /**
   * Decode JWT token payload (client-side only, for UI purposes)
   */
  static decodeToken(token: string): any | null {
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
    try {
      const decoded = this.decodeToken(token);
      if (!decoded || !decoded.exp) return true;
      
      const currentTime = Date.now() / 1000;
      return decoded.exp < currentTime;
    } catch (error) {
      console.error('Error checking token expiry:', error);
      return true;
    }
  }

  /**
   * Get token expiry time in milliseconds
   */
  static getTokenExpiry(token: string): number | null {
    try {
      const decoded = this.decodeToken(token);
      if (!decoded || !decoded.exp) return null;
      
      return decoded.exp * 1000; // Convert to milliseconds
    } catch (error) {
      console.error('Error getting token expiry:', error);
      return null;
    }
  }

  /**
   * Get time until token expires in milliseconds
   */
  static getTimeUntilExpiry(token: string): number | null {
    try {
      const expiry = this.getTokenExpiry(token);
      if (!expiry) return null;
      
      return expiry - Date.now();
    } catch (error) {
      console.error('Error calculating time until expiry:', error);
      return null;
    }
  }

  /**
   * Check if token needs refresh (within threshold)
   */
  static shouldRefreshToken(token: string, thresholdMinutes: number = 5): boolean {
    try {
      const timeUntilExpiry = this.getTimeUntilExpiry(token);
      if (!timeUntilExpiry) return false;
      
      const thresholdMs = thresholdMinutes * 60 * 1000;
      return timeUntilExpiry <= thresholdMs && timeUntilExpiry > 0;
    } catch (error) {
      console.error('Error checking if token should refresh:', error);
      return false;
    }
  }
}

export default TokenManager;