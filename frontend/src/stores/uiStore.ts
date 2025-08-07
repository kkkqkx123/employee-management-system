// UI state store using Zustand

import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';

export interface UIState {
  // Theme
  theme: 'light' | 'dark' | 'auto';
  
  // Navigation
  sidebarCollapsed: boolean;
  mobileMenuOpen: boolean;
  
  // Loading states
  globalLoading: boolean;
  loadingMessage: string | null;
  
  // Modals and overlays
  activeModal: string | null;
  modalProps: Record<string, any>;
  
  // Notifications
  notifications: UINotification[];
  
  // Search
  globalSearchOpen: boolean;
  globalSearchQuery: string;
  
  // Layout
  headerHeight: number;
  sidebarWidth: number;
  
  // Responsive
  isMobile: boolean;
  isTablet: boolean;
  screenSize: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
}

export interface UINotification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message?: string;
  autoClose?: boolean;
  duration?: number;
  action?: {
    label: string;
    onClick: () => void;
  };
  createdAt: number;
}

interface UIStore extends UIState {
  // Theme actions
  setTheme: (theme: 'light' | 'dark' | 'auto') => void;
  toggleTheme: () => void;
  
  // Navigation actions
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  toggleMobileMenu: () => void;
  setMobileMenuOpen: (open: boolean) => void;
  
  // Loading actions
  setGlobalLoading: (loading: boolean, message?: string) => void;
  
  // Modal actions
  openModal: (modalId: string, props?: Record<string, any>) => void;
  closeModal: () => void;
  
  // Notification actions
  showNotification: (notification: Omit<UINotification, 'id' | 'createdAt'>) => string;
  hideNotification: (id: string) => void;
  clearNotifications: () => void;
  
  // Search actions
  setGlobalSearchOpen: (open: boolean) => void;
  setGlobalSearchQuery: (query: string) => void;
  
  // Layout actions
  setHeaderHeight: (height: number) => void;
  setSidebarWidth: (width: number) => void;
  
  // Responsive actions
  setScreenSize: (size: 'xs' | 'sm' | 'md' | 'lg' | 'xl') => void;
  setIsMobile: (isMobile: boolean) => void;
  setIsTablet: (isTablet: boolean) => void;
}

export const useUIStore = create<UIStore>()(
  devtools(
    persist(
      (set, get) => ({
        // Initial state
        theme: 'light',
        sidebarCollapsed: false,
        mobileMenuOpen: false,
        globalLoading: false,
        loadingMessage: null,
        activeModal: null,
        modalProps: {},
        notifications: [],
        globalSearchOpen: false,
        globalSearchQuery: '',
        headerHeight: 60,
        sidebarWidth: 280,
        isMobile: false,
        isTablet: false,
        screenSize: 'lg',

        // Theme actions
        setTheme: (theme) => {
          set({ theme });
          
          // Apply theme to document
          const root = document.documentElement;
          if (theme === 'dark') {
            root.classList.add('dark');
          } else if (theme === 'light') {
            root.classList.remove('dark');
          } else {
            // Auto theme - check system preference
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            if (prefersDark) {
              root.classList.add('dark');
            } else {
              root.classList.remove('dark');
            }
          }
        },

        toggleTheme: () => {
          const { theme } = get();
          const newTheme = theme === 'light' ? 'dark' : 'light';
          get().setTheme(newTheme);
        },

        // Navigation actions
        toggleSidebar: () => {
          set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed }));
        },

        setSidebarCollapsed: (collapsed) => {
          set({ sidebarCollapsed: collapsed });
        },

        toggleMobileMenu: () => {
          set((state) => ({ mobileMenuOpen: !state.mobileMenuOpen }));
        },

        setMobileMenuOpen: (open) => {
          set({ mobileMenuOpen: open });
        },

        // Loading actions
        setGlobalLoading: (loading, message = null) => {
          set({ globalLoading: loading, loadingMessage: message });
        },

        // Modal actions
        openModal: (modalId, props = {}) => {
          set({ activeModal: modalId, modalProps: props });
        },

        closeModal: () => {
          set({ activeModal: null, modalProps: {} });
        },

        // Notification actions
        showNotification: (notification) => {
          const id = `notification-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
          const newNotification: UINotification = {
            ...notification,
            id,
            createdAt: Date.now(),
            autoClose: notification.autoClose ?? true,
            duration: notification.duration ?? 5000,
          };

          set((state) => ({
            notifications: [...state.notifications, newNotification],
          }));

          // Auto-remove notification if autoClose is enabled
          if (newNotification.autoClose) {
            setTimeout(() => {
              get().hideNotification(id);
            }, newNotification.duration);
          }

          return id;
        },

        hideNotification: (id) => {
          set((state) => ({
            notifications: state.notifications.filter(n => n.id !== id),
          }));
        },

        clearNotifications: () => {
          set({ notifications: [] });
        },

        // Search actions
        setGlobalSearchOpen: (open) => {
          set({ globalSearchOpen: open });
          if (!open) {
            set({ globalSearchQuery: '' });
          }
        },

        setGlobalSearchQuery: (query) => {
          set({ globalSearchQuery: query });
        },

        // Layout actions
        setHeaderHeight: (height) => {
          set({ headerHeight: height });
        },

        setSidebarWidth: (width) => {
          set({ sidebarWidth: width });
        },

        // Responsive actions
        setScreenSize: (size) => {
          set({ screenSize: size });
        },

        setIsMobile: (isMobile) => {
          set({ isMobile });
        },

        setIsTablet: (isTablet) => {
          set({ isTablet });
        },
      }),
      {
        name: 'ui-store',
        partialize: (state) => ({
          // Only persist user preferences
          theme: state.theme,
          sidebarCollapsed: state.sidebarCollapsed,
        }),
      }
    ),
    {
      name: 'ui-store',
    }
  )
);

// Selectors for better performance
export const useTheme = () => useUIStore((state) => ({
  theme: state.theme,
  setTheme: state.setTheme,
  toggleTheme: state.toggleTheme,
}));

export const useNavigation = () => useUIStore((state) => ({
  sidebarCollapsed: state.sidebarCollapsed,
  mobileMenuOpen: state.mobileMenuOpen,
  toggleSidebar: state.toggleSidebar,
  setSidebarCollapsed: state.setSidebarCollapsed,
  toggleMobileMenu: state.toggleMobileMenu,
  setMobileMenuOpen: state.setMobileMenuOpen,
}));

export const useLoading = () => useUIStore((state) => ({
  globalLoading: state.globalLoading,
  loadingMessage: state.loadingMessage,
  setGlobalLoading: state.setGlobalLoading,
}));

export const useModal = () => useUIStore((state) => ({
  activeModal: state.activeModal,
  modalProps: state.modalProps,
  openModal: state.openModal,
  closeModal: state.closeModal,
}));

export const useNotifications = () => useUIStore((state) => ({
  notifications: state.notifications,
  showNotification: state.showNotification,
  hideNotification: state.hideNotification,
  clearNotifications: state.clearNotifications,
}));

export const useGlobalSearch = () => useUIStore((state) => ({
  globalSearchOpen: state.globalSearchOpen,
  globalSearchQuery: state.globalSearchQuery,
  setGlobalSearchOpen: state.setGlobalSearchOpen,
  setGlobalSearchQuery: state.setGlobalSearchQuery,
}));

export const useLayout = () => useUIStore((state) => ({
  headerHeight: state.headerHeight,
  sidebarWidth: state.sidebarWidth,
  setHeaderHeight: state.setHeaderHeight,
  setSidebarWidth: state.setSidebarWidth,
}));

export const useResponsive = () => useUIStore((state) => ({
  isMobile: state.isMobile,
  isTablet: state.isTablet,
  screenSize: state.screenSize,
  setScreenSize: state.setScreenSize,
  setIsMobile: state.setIsMobile,
  setIsTablet: state.setIsTablet,
}));

export default useUIStore;