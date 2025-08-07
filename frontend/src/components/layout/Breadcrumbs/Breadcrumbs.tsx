import { Breadcrumbs as MantineBreadcrumbs, Anchor, Text } from '@mantine/core';
import { Link, useLocation } from 'react-router-dom';
import { ROUTES } from '@/constants';
import classes from './Breadcrumbs.module.css';

interface BreadcrumbItem {
  label: string;
  href?: string;
}

const routeLabels: Record<string, string> = {
  [ROUTES.DASHBOARD]: 'Dashboard',
  [ROUTES.EMPLOYEES]: 'Employees',
  [ROUTES.DEPARTMENTS]: 'Departments',
  [ROUTES.CHAT]: 'Chat',
  [ROUTES.EMAIL]: 'Email',
  [ROUTES.NOTIFICATIONS]: 'Notifications',
  [ROUTES.PERMISSIONS]: 'Permissions',
};

export const Breadcrumbs = () => {
  const location = useLocation();
  const pathSegments = location.pathname.split('/').filter(Boolean);
  
  const breadcrumbItems: BreadcrumbItem[] = [];
  
  // Build breadcrumb items from path segments
  let currentPath = '';
  pathSegments.forEach((segment) => {
    currentPath += `/${segment}`;
    const label = routeLabels[currentPath] || segment.charAt(0).toUpperCase() + segment.slice(1);
    breadcrumbItems.push({
      label,
      href: currentPath,
    });
  });
  
  // Don't show breadcrumbs for root paths or single-level paths
  if (breadcrumbItems.length <= 1) {
    return null;
  }
  
  return (
    <MantineBreadcrumbs className={classes.breadcrumbs}>
      {breadcrumbItems.map((item, index) => {
        const isLast = index === breadcrumbItems.length - 1;
        
        if (isLast) {
          return (
            <Text key={item.href} c="dimmed" size="sm">
              {item.label}
            </Text>
          );
        }
        
        return (
          <Anchor
            key={item.href}
            component={Link}
            to={item.href!}
            size="sm"
            className={classes.breadcrumbLink}
          >
            {item.label}
          </Anchor>
        );
      })}
    </MantineBreadcrumbs>
  );
};