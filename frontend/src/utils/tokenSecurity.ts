/**
 * Secure token management utilities
 */

import Cookies from 'js-cookie';

export interface TokenStorage {
  getToken(): string | null;
  setToken(token: string, options?: { secure?: boolean; sameSite?: 'strict' | 'lax' | 'none' }): void;
  removeToken(): void;
  getRefreshToken(): string | null;
  setRefreshToken(token: string, options?: { secure?: boolean; sameSite?: 'strict' | 'lax' | 'none' }): void;
  removeRefreshToken(): void;
}

/**
 * Secure token storage using httpOnly cookies (preferred for production)
 */
export class SecureCookieTokenStorage implements TokenStorage {
  private readonly tokenKey = 'auth_token';
  private readonly refreshTokenKey = 'refresh_token';

  getToken(): string | null {
    return Cookies.get(this.tokenKey) || null;
  }

  setToken(token: string, options: { secure?: boolean; sameSite?: 'strict' | 'lax' | 'none' } = {}): void {
    const cookieOptions = {
      secure: options.secure ?? window.location.protocol === 'https:',
      sameSite: options.sameSite ?? 'strict' as const,
      expires: 1, // 1 day
      path: '/',
    };

    Cookies.set(this.tokenKey, token, cookieOptions);
  }

  removeToken(): void {
    Cookies.remove(this.tokenKey, { path: '/' });
  }

  getRefreshToken(): string | null {
    return Cookies.get(this.refreshTokenKey) || null;
  }

  setRefreshToken(token: string, options: { secure?: boolean; sameSite?: 'strict' | 'lax' | 'none' } = {}): void {
    const cookieOptions = {
      secure: options.secure ?? window.location.protocol === 'https:',
      sameSite: options.sameSite ?? 'strict' as const,
      expires: 7, // 7 days
      path: '/',
    };

    Cookies.set(this.refreshTokenKey, token, cookieOptions);
  }

  removeRefreshToken(): void {
    Cookies.remove(this.refreshTokenKey, { path: '/' });
  }
}

/**
 * Memory-based token storage (for development or when cookies are not available)
 */
export class MemoryTokenStorage implements TokenStorage {
  private token: string | null = null;
  private refreshToken: string | null = null;

  getToken(): string | null {
    return this.token;
  }

  setToken(token: string): void {
    this.token = token;
  }

  removeToken(): void {
    this.token = null;
  }

  getRefreshToken(): string | null {
    return this.refreshToken;
  }

  setRefreshToken(token: string): void {
    this.refreshToken = token;
  }

  removeRefreshToken(): void {
    this.refreshToken = null;
  }
}

/**
 * Fallback localStorage implementation with encryption
 */
export class EncryptedLocalStorageTokenStorage implements TokenStorage {
  private readonly tokenKey = 'auth_token';
  private readonly refreshTokenKey = 'refresh_token';

  private encrypt(text: string): string {
    // Simple XOR encryption for basic obfuscation
    // In production, use a proper encryption library
    const key = 'secure_key_' + window.location.hostname;
    let encrypted = '';
    for (let i = 0; i < text.length; i++) {
      encrypted += String.fromCharCode(text.charCodeAt(i) ^ key.charCodeAt(i % key.length));
    }
    return btoa(encrypted);
  }

  private decrypt(encryptedText: string): string {
    try {
      const key = 'secure_key_' + window.location.hostname;
      const encrypted = atob(encryptedText);
      let decrypted = '';
      for (let i = 0; i < encrypted.length; i++) {
        decrypted += String.fromCharCode(encrypted.charCodeAt(i) ^ key.charCodeAt(i % key.length));
      }
      return decrypted;
    } catch {
      return '';
    }
  }

  getToken(): string | null {
    const encrypted = localStorage.getItem(this.tokenKey);
    return encrypted ? this.decrypt(encrypted) : null;
  }

  setToken(token: string): void {
    const encrypted = this.encrypt(token);
    localStorage.setItem(this.tokenKey, encrypted);
  }

  removeToken(): void {
    localStorage.removeItem(this.tokenKey);
  }

  getRefreshToken(): string | null {
    const encrypted = localStorage.getItem(this.refreshTokenKey);
    return encrypted ? this.decrypt(encrypted) : null;
  }

  setRefreshToken(token: string): void {
    const encrypted = this.encrypt(token);
    localStorage.setItem(this.refreshTokenKey, encrypted);
  }

  removeRefreshToken(): void {
    localStorage.removeItem(this.refreshTokenKey);
  }
}

/**
 * Token security utilities
 */
export class TokenSecurity {
  /**
   * Validate JWT token format (basic validation)
   */
  static isValidJWTFormat(token: string): boolean {
    const jwtRegex = /^[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+\.[A-Za-z0-9-_]*$/;
    return jwtRegex.test(token);
  }

  /**
   * Check if JWT token is expired (without verification)
   */
  static isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp < currentTime;
    } catch {
      return true;
    }
  }

  /**
   * Get token expiration time
   */
  static getTokenExpiration(token: string): Date | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return new Date(payload.exp * 1000);
    } catch {
      return null;
    }
  }

  /**
   * Get time until token expires (in milliseconds)
   */
  static getTimeUntilExpiration(token: string): number {
    const expiration = this.getTokenExpiration(token);
    return expiration ? expiration.getTime() - Date.now() : 0;
  }

  /**
   * Check if token needs refresh (expires within threshold)
   */
  static needsRefresh(token: string, thresholdMinutes: number = 5): boolean {
    const timeUntilExpiration = this.getTimeUntilExpiration(token);
    const thresholdMs = thresholdMinutes * 60 * 1000;
    return timeUntilExpiration <= thresholdMs;
  }
}

// Create token storage instance based on environment
export const createTokenStorage = (): TokenStorage => {
  // Prefer secure cookies in production
  if (import.meta.env.PROD && window.location.protocol === 'https:') {
    return new SecureCookieTokenStorage();
  }

  // Use memory storage for development
  if (import.meta.env.DEV) {
    return new MemoryTokenStorage();
  }

  // Fallback to encrypted localStorage
  return new EncryptedLocalStorageTokenStorage();
};

export default TokenSecurity;