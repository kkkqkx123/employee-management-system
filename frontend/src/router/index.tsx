import { Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES, PERMISSIONS } from '@/constants';
import { ProtectedRoute } from '@/features/auth/components';
import { DashboardPage } from '@/pages/DashboardPage';
import { EmployeesPage } from '@/pages/EmployeesPage';
import { DepartmentsPage } from '@/pages/DepartmentsPage';
import { ChatPage } from '@/pages/ChatPage';
import { EmailPage } from '@/pages/EmailPage';
import { NotificationsPage } from '@/pages/NotificationsPage';
import { PermissionsPage } from '@/pages/PermissionsPage';
import { LoginPage } from '@/pages/LoginPage';
import { NotFoundPage } from '@/pages/NotFoundPage';

export const AppRouter = () => {
  return (
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
  );
};