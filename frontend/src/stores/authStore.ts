// Authentication store using Zustand

import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import AuthService from '../services/auth';
import webSocketService from '../services/websocket';
import type { User, LoginRequest, RegisterRequest, AuthState } from '../types/auth';

interface AuthStore extends AuthState {
  // Actions
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<void>;
  validateToken: () => Promise<boolean>;
  setUser: (user: User) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
  hasPermission: (permission: string) => boolean;
  hasAnyPermission: (permissions: string[]) => boolean;
  hasAllPermissions: (permissions: string[]) => boolean;
  initialize: () => Promise<void>;
}

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        // Initial state
        user: null,
        token: null,
        permissions: [],
        isAuthenticated: false,
        isLoading: false,
        error: null,

        // Actions
        login: async (credentials: LoginRequest) => {
          set({ isLoading: true, error: null });
          
          try {
            const response = await AuthService.login(credentials);
            
            set({
              user: response.user,
              token: response.token,
              permissions: response.permissions,
              isAuthenticated: true,
              isLoading: false,
              error: null,
            });

            // Connect to WebSocket after successful login
            try {
              await webSocketService.connect();
            } catch (wsError) {
              console.warn('WebSocket connection failed:', wsError);
              // Don't fail login if WebSocket connection fails
            }
          } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'Login failed';
            set({
              user: null,
              token: null,
              permissions: [],
              isAuthenticated: false,
              isLoading: false,
              error: errorMessage,
            });
            throw error;
          }
        },

        register: async (userData: RegisterRequest) => {
          set({ isLoading: true, error: null });
          
          try {
            await AuthService.register(userData);
            set({ isLoading: false, error: null });
          } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'Registration failed';
            set({
              isLoading: false,
              error: errorMessage,
            });
            throw error;
          }
        },

        logout: async () => {
          set({ isLoading: true });
          
          try {
            await AuthService.logout();
          } catch (error) {
            console.error('Logout error:', error);
          } finally {
            // Disconnect WebSocket
            webSocketService.disconnect();
            
            // Clear state
            set({
              user: null,
              token: null,
              permissions: [],
              isAuthenticated: false,
              isLoading: false,
              error: null,
            });
          }
        },

        refreshToken: async () => {
          try {
            const newToken = await AuthService.refreshToken();
            set({ token: newToken });
            
            // Update WebSocket auth token
            webSocketService.updateAuthToken(newToken);
          } catch (error) {
            console.error('Token refresh failed:', error);
            // Force logout on refresh failure
            get().logout();
            throw error;
          }
        },

        validateToken: async () => {
          try {
            const isValid = await AuthService.validateToken();
            if (!isValid) {
              get().logout();
            }
            return isValid;
          } catch (error) {
            console.error('Token validation failed:', error);
            get().logout();
            return false;
          }
        },

        setUser: (user: User) => {
          set({ user });
        },

        setError: (error: string | null) => {
          set({ error });
        },

        clearError: () => {
          set({ error: null });
        },

        hasPermission: (permission: string) => {
          const { permissions } = get();
          return permissions.includes(permission);
        },

        hasAnyPermission: (requiredPermissions: string[]) => {
          const { permissions } = get();
          return requiredPermissions.some(permission => permissions.includes(permission));
        },

        hasAllPermissions: (requiredPermissions: string[]) => {
          const { permissions } = get();
          return requiredPermissions.every(permission => permissions.includes(permission));
        },

        initialize: async () => {
          set({ isLoading: true });
          
          try {
            // Check if user is already authenticated
            const token = AuthService.getToken();
            const user = AuthService.getCurrentUser();
            const permissions = AuthService.getCurrentPermissions();

            if (token && user && !AuthService.isTokenExpired(token)) {
              set({
                user,
                token,
                permissions,
                isAuthenticated: true,
                isLoading: false,
              });

              // Connect to WebSocket
              try {
                await webSocketService.connect();
              } catch (wsError) {
                console.warn('WebSocket connection failed during initialization:', wsError);
              }
            } else {
              // Clear invalid data
              await get().logout();
            }
          } catch (error) {
            console.error('Auth initialization failed:', error);
            await get().logout();
          } finally {
            set({ isLoading: false });
          }
        },
      }),
      {
        name: 'auth-store',
        partialize: (state) => ({
          // Only persist essential data
          user: state.user,
          token: state.token,
          permissions: state.permissions,
          isAuthenticated: state.isAuthenticated,
        }),
      }
    ),
    {
      name: 'auth-store',
    }
  )
);

// Selectors for better performance
export const useAuth = () => useAuthStore((state) => ({
  user: state.user,
  isAuthenticated: state.isAuthenticated,
  isLoading: state.isLoading,
  error: state.error,
}));

export const useAuthActions = () => useAuthStore((state) => ({
  login: state.login,
  register: state.register,
  logout: state.logout,
  clearError: state.clearError,
  initialize: state.initialize,
}));

export const usePermissions = () => useAuthStore((state) => ({
  permissions: state.permissions,
  hasPermission: state.hasPermission,
  hasAnyPermission: state.hasAnyPermission,
  hasAllPermissions: state.hasAllPermissions,
}));

export default useAuthStore;