import { NavLink, ScrollArea, Group, Text, ThemeIcon, Badge, Divider } from '@mantine/core';
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
import { useResponsive } from '@/hooks';
import classes from './Navigation.module.css';

interface NavigationItem {
  label: string;
  icon: React.ComponentType<{ size?: number }>;
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
  const { isMobile } = useResponsive();

  // Filter navigation items based on permissions
  const visibleItems = navigationItems.filter((item) => {
    if (!item.permission) return true;
    return hasPermission(item.permission);
  });

  // Group navigation items for better mobile organization
  const primaryItems = visibleItems.slice(0, 3); // Dashboard, Employees, Departments
  const communicationItems = visibleItems.slice(3, 6); // Chat, Email, Notifications
  const adminItems = visibleItems.slice(6); // Permissions

  const renderNavItems = (items: typeof navigationItems, showDivider = false) => (
    <>
      {showDivider && <Divider my="sm" />}
      {items.map((item) => {
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
            description={!isMobile ? item.description : undefined}
            leftSection={<Icon size={isMobile ? 20 : 18} />}
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
    </>
  );

  return (
    <ScrollArea className={classes.scrollArea}>
      <div className={classes.header}>
        <Group gap={isMobile ? "xs" : "sm"}>
          <ThemeIcon 
            size={isMobile ? "md" : "lg"} 
            variant="gradient" 
            gradient={{ from: 'blue', to: 'cyan' }}
          >
            <IconUsers size={isMobile ? 16 : 20} />
          </ThemeIcon>
          <div className={classes.headerText}>
            <Text fw={600} size={isMobile ? "xs" : "sm"} truncate>
              {isMobile ? "EMS" : "Employee Management"}
            </Text>
            {!isMobile && (
              <Text size="xs" c="dimmed">
                System
              </Text>
            )}
          </div>
        </Group>
      </div>

      <div className={classes.navSection}>
        {isMobile ? (
          // Mobile: Group items by category
          <>
            {renderNavItems(primaryItems)}
            {communicationItems.length > 0 && (
              <>
                <Text size="xs" c="dimmed" className={classes.sectionLabel}>
                  Communication
                </Text>
                {renderNavItems(communicationItems)}
              </>
            )}
            {adminItems.length > 0 && (
              <>
                <Text size="xs" c="dimmed" className={classes.sectionLabel}>
                  Administration
                </Text>
                {renderNavItems(adminItems)}
              </>
            )}
          </>
        ) : (
          // Desktop: Show all items in one list
          renderNavItems(visibleItems)
        )}
      </div>
    </ScrollArea>
  );
};