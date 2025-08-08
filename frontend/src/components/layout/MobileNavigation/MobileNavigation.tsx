import React from 'react';
import { Drawer, NavLink, ScrollArea, Group, Text, ThemeIcon, Badge, Divider } from '@mantine/core';
import { 
  IconDashboard, 
  IconUsers, 
  IconBuilding, 
  IconMessage, 
  IconMail, 
  IconBell,
  IconShield,
  IconX
} from '@tabler/icons-react';
import { useLocation, Link } from 'react-router-dom';
import { ROUTES, PERMISSIONS } from '@/constants';
import { usePermissionCheck } from '@/features/auth/hooks';
import { useNotificationStore } from '@/stores';
import { TouchGesture } from '@/components/ui';
import classes from './MobileNavigation.module.css';

interface MobileNavigationProps {
  /** Whether the drawer is opened */
  opened: boolean;
  /** Callback to close the drawer */
  onClose: () => void;
}

interface NavigationItem {
  label: string;
  icon: React.ComponentType<{ size?: number }>;
  href: string;
  description?: string;
  badge?: string | number;
  permission?: string;
  category: 'primary' | 'communication' | 'admin';
}

const navigationItems: NavigationItem[] = [
  {
    label: 'Dashboard',
    icon: IconDashboard,
    href: ROUTES.DASHBOARD,
    description: 'Overview and analytics',
    category: 'primary',
  },
  {
    label: 'Employees',
    icon: IconUsers,
    href: ROUTES.EMPLOYEES,
    description: 'Manage employee records',
    permission: PERMISSIONS.EMPLOYEE_READ,
    category: 'primary',
  },
  {
    label: 'Departments',
    icon: IconBuilding,
    href: ROUTES.DEPARTMENTS,
    description: 'Organizational structure',
    permission: PERMISSIONS.DEPARTMENT_READ,
    category: 'primary',
  },
  {
    label: 'Chat',
    icon: IconMessage,
    href: ROUTES.CHAT,
    description: 'Team communication',
    category: 'communication',
  },
  {
    label: 'Email',
    icon: IconMail,
    href: ROUTES.EMAIL,
    description: 'Email management',
    category: 'communication',
  },
  {
    label: 'Notifications',
    icon: IconBell,
    href: ROUTES.NOTIFICATIONS,
    description: 'System notifications',
    category: 'communication',
  },
  {
    label: 'Permissions',
    icon: IconShield,
    href: ROUTES.PERMISSIONS,
    description: 'Access control',
    permission: PERMISSIONS.USER_READ,
    category: 'admin',
  },
];

/**
 * MobileNavigation component optimized for mobile devices
 * Features swipe-to-close and categorized navigation items
 */
export const MobileNavigation: React.FC<MobileNavigationProps> = ({
  opened,
  onClose,
}) => {
  const location = useLocation();
  const { hasPermission } = usePermissionCheck();
  const { unreadCount } = useNotificationStore();

  // Filter navigation items based on permissions
  const visibleItems = navigationItems.filter((item) => {
    if (!item.permission) return true;
    return hasPermission(item.permission);
  });

  // Group items by category
  const primaryItems = visibleItems.filter(item => item.category === 'primary');
  const communicationItems = visibleItems.filter(item => item.category === 'communication');
  const adminItems = visibleItems.filter(item => item.category === 'admin');

  const renderNavItems = (items: NavigationItem[], title?: string) => (
    <>
      {title && (
        <Text size="xs" c="dimmed" className={classes.sectionTitle}>
          {title}
        </Text>
      )}
      {items.map((item) => {
        const Icon = item.icon;
        const isActive = location.pathname === item.href;
        
        // Get badge count for notifications
        let badgeCount: number | undefined;
        if (item.href === ROUTES.NOTIFICATIONS) {
          badgeCount = unreadCount > 0 ? unreadCount : undefined;
        }

        return (
          <NavLink
            key={item.href}
            component={Link}
            to={item.href}
            label={item.label}
            leftSection={<Icon size={20} />}
            rightSection={
              badgeCount ? (
                <Badge size="sm" variant="filled" color="red">
                  {badgeCount > 99 ? '99+' : badgeCount}
                </Badge>
              ) : null
            }
            active={isActive}
            className={classes.navLink}
            onClick={onClose} // Close drawer when item is clicked
          />
        );
      })}
    </>
  );

  return (
    <Drawer
      opened={opened}
      onClose={onClose}
      size="280px"
      position="left"
      withCloseButton={false}
      styles={{
        content: {
          display: 'flex',
          flexDirection: 'column',
        },
        body: {
          padding: 0,
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
        },
      }}
      className={classes.drawer}
    >
      <TouchGesture onSwipeLeft={onClose} className={classes.touchArea}>
        {/* Header */}
        <div className={classes.header}>
          <Group justify="space-between" align="center">
            <Group gap="sm">
              <ThemeIcon size="md" variant="gradient" gradient={{ from: 'blue', to: 'cyan' }}>
                <IconUsers size={16} />
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
            <ThemeIcon
              variant="subtle"
              size="lg"
              onClick={onClose}
              className={classes.closeButton}
            >
              <IconX size={18} />
            </ThemeIcon>
          </Group>
        </div>

        {/* Navigation Content */}
        <ScrollArea className={classes.scrollArea}>
          <div className={classes.navSection}>
            {renderNavItems(primaryItems)}
            
            {communicationItems.length > 0 && (
              <>
                <Divider my="md" />
                {renderNavItems(communicationItems, 'Communication')}
              </>
            )}
            
            {adminItems.length > 0 && (
              <>
                <Divider my="md" />
                {renderNavItems(adminItems, 'Administration')}
              </>
            )}
          </div>
        </ScrollArea>

        {/* Footer */}
        <div className={classes.footer}>
          <Text size="xs" c="dimmed" ta="center">
            Swipe left to close
          </Text>
        </div>
      </TouchGesture>
    </Drawer>
  );
};