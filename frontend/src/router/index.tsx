import { Routes, Route, Navigate, lazy, Suspense } from 'react-router-dom';
import { ROUTES, PERMISSIONS } from '@/constants';
import { ProtectedRoute } from '@/features/auth/components';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';

// Lazy load pages for code splitting
const DashboardPage = lazy(() => import('@/pages/DashboardPage'));
const EmployeesPage = lazy(() => import('@/pages/EmployeesPage'));
const DepartmentsPage = lazy(() => import('@/pages/DepartmentsPage'));
const ChatPage = lazy(() => import('@/pages/ChatPage'));
const EmailPage = lazy(() => import('@/pages/EmailPage'));
const NotificationsPage = lazy(() => import('@/pages/NotificationsPage'));
const PermissionsPage = lazy(() => import('@/pages/PermissionsPage'));
const LoginPage = lazy(() => import('@/pages/LoginPage'));
const NotFoundPage = lazy(() => import('@/pages/NotFoundPage'));

export const AppRouter = () => {
  return (
    <Suspense fallback={<LoadingSpinner size="lg" />}>
      <Routes>
        {/* Public routes */}
        <Route path={ROUTES.LOGIN} element={<LoginPage />} />
        
        {/* Protected routes */}
        <Route path="/" element={<ProtectedRoute />}>
          <Route index element={<Navigate to={ROUTES.DASHBOARD} replace />} />
          <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
          
          <Route 
            path={ROUTES.EMPLOYEES} 
            element={
              <ProtectedRoute requiredPermission={PERMISSIONS.EMPLOYEE_READ}>
                <EmployeesPage />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path={ROUTES.DEPARTMENTS} 
            element={
              <ProtectedRoute requiredPermission={PERMISSIONS.DEPARTMENT_READ}>
                <DepartmentsPage />
              </ProtectedRoute>
            } 
          />
          
          <Route path={ROUTES.CHAT} element={<ChatPage />} />
          <Route path={ROUTES.EMAIL} element={<EmailPage />} />
          <Route path={ROUTES.NOTIFICATIONS} element={<NotificationsPage />} />
          
          <Route 
            path={ROUTES.PERMISSIONS} 
            element={
              <ProtectedRoute requiredPermission={PERMISSIONS.USER_READ}>
                <PermissionsPage />
              </ProtectedRoute>
            } 
          />
        </Route>
        
        {/* 404 route */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
};