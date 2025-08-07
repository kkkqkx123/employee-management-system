import { NavLink, ScrollArea, Group, Text, ThemeIcon } from '@mantine/core';
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
import { ROUTES } from '@/constants';
import classes from './Navigation.module.css';

interface NavigationItem {
  label: string;
  icon: React.ComponentType<any>;
  href: string;
  description?: string;
  badge?: string | number;
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
  },
  {
    label: 'Departments',
    icon: IconBuilding,
    href: ROUTES.DEPARTMENTS,
    description: 'Organizational structure',
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
  },
];

export const Navigation = () => {
  const location = useLocation();

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
        {navigationItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.href;

          return (
            <NavLink
              key={item.href}
              component={Link}
              to={item.href}
              label={item.label}
              description={item.description}
              leftSection={<Icon size={18} />}
              rightSection={item.badge && (
                <Text size="xs" c="dimmed">
                  {item.badge}
                </Text>
              )}
              active={isActive}
              className={classes.navLink}
            />
          );
        })}
      </div>
    </ScrollArea>
  );
};