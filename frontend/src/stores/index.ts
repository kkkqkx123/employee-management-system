// Stores index - exports all stores and selectors

export { 
  default as useAuthStore,
  useAuth,
  useAuthActions,
  usePermissions
} from './authStore';

export {
  default as useUIStore,
  useTheme,
  useNavigation,
  useLoading,
  useModal,
  useNotifications,
  useGlobalSearch,
  useLayout,
  useResponsive
} from './uiStore';

export {
  default as useNotificationStore,
  useNotifications as useNotificationData,
  useNotificationActions,
  useUnreadNotifications,
  useRecentNotifications
} from './notificationStore';

// Re-export types
export type { UINotification } from './uiStore';