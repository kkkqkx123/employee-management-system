import { NavLink, ScrollArea, Group, Text, ThemeIcon, Badge } from '@mantine/core';
import { 
  IconDashboard, 
  IconUsers, 
  IconBuilding, 
  IconMessage, 
  IconMail, 
  IconBell,
  IconShield
} from '@tabler/icons-react';
import { useLocation, Link } from 'react-router-dom';
import { ROUTES, PERMISSIONS } from '@/constants';
import { usePermissionCheck } from '@/features/auth/hooks';
import { useNotificationStore } from '@/stores';
import classes from './Navigation.module.css';

interface NavigationItem {
  label: string;
  icon: React.ComponentType<any>;
  href: string;
  description?: string;
  badge?: string | number;
  permission?: string;
  showBadge?: () => number | undefined;
}

const navigationItems: NavigationItem[] = [
  {
    label: 'Dashboard',
    icon: IconDashboard,
    href: ROUTES.DASHBOARD,
    description: 'Overview and analytics',
  },
  {
    label: 'Employees',
    icon: IconUsers,
    href: ROUTES.EMPLOYEES,
    description: 'Manage employee records',
    permission: PERMISSIONS.EMPLOYEE_READ,
  },
  {
    label: 'Departments',
    icon: IconBuilding,
    href: ROUTES.DEPARTMENTS,
    description: 'Organizational structure',
    permission: PERMISSIONS.DEPARTMENT_READ,
  },
  {
    label: 'Chat',
    icon: IconMessage,
    href: ROUTES.CHAT,
    description: 'Team communication',
  },
  {
    label: 'Email',
    icon: IconMail,
    href: ROUTES.EMAIL,
    description: 'Email management',
  },
  {
    label: 'Notifications',
    icon: IconBell,
    href: ROUTES.NOTIFICATIONS,
    description: 'System notifications',
  },
  {
    label: 'Permissions',
    icon: IconShield,
    href: ROUTES.PERMISSIONS,
    description: 'Access control',
    permission: PERMISSIONS.USER_READ,
  },
];

export const Navigation = () => {
  const location = useLocation();
  const { hasPermission } = usePermissionCheck();
  const { unreadCount } = useNotificationStore();

  // Filter navigation items based on permissions
  const visibleItems = navigationItems.filter((item) => {
    if (!item.permission) return true;
    return hasPermission(item.permission);
  });

  return (
    <ScrollArea className={classes.scrollArea}>
      <div className={classes.header}>
        <Group>
          <ThemeIcon size="lg" variant="gradient" gradient={{ from: 'blue', to: 'cyan' }}>
            <IconUsers size={20} />
          </ThemeIcon>
          <div>
            <Text fw={600} size="sm">
              Employee Management
            </Text>
            <Text size="xs" c="dimmed">
              System
            </Text>
          </div>
        </Group>
      </div>

      <div className={classes.navSection}>
        {visibleItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.href;
          
          // Get badge count for notifications
          let badgeCount: number | undefined;
          if (item.showBadge) {
            badgeCount = item.showBadge();
          } else if (item.href === ROUTES.NOTIFICATIONS) {
            badgeCount = unreadCount > 0 ? unreadCount : undefined;
          }

          return (
            <NavLink
              key={item.href}
              component={Link}
              to={item.href}
              label={item.label}
              description={item.description}
              leftSection={<Icon size={18} />}
              rightSection={
                badgeCount ? (
                  <Badge size="sm" variant="filled" color="red">
                    {badgeCount > 99 ? '99+' : badgeCount}
                  </Badge>
                ) : item.badge ? (
                  <Text size="xs" c="dimmed">
                    {item.badge}
                  </Text>
                ) : null
              }
              active={isActive}
              className={classes.navLink}
            />
          );
        })}
      </div>
    </ScrollArea>
  );
};