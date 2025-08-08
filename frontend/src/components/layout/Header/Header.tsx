import { Group, Text, ActionIcon, Menu, Avatar } from '@mantine/core';
import { 
  IconUser, 
  IconSettings, 
  IconLogout,
  IconSearch
} from '@tabler/icons-react';
import type { ReactNode } from 'react';
import { HeaderNotifications } from '../../../features/notifications';
import classes from './Header.module.css';

export interface HeaderProps {
  /** Mobile burger menu */
  burger?: ReactNode;
  /** Desktop burger menu */
  desktopBurger?: ReactNode;
  /** User information */
  user?: {
    name: string;
    email: string;
    avatar?: string;
  };
  /** Search handler */
  onSearch?: (query: string) => void;
  /** Notification click handler */
  onNotificationClick?: () => void;
  /** Profile click handler */
  onProfileClick?: () => void;
  /** Settings click handler */
  onSettingsClick?: () => void;
  /** Logout handler */
  onLogout?: () => void;
}

export const Header = ({
  burger,
  desktopBurger,
  user = { name: 'John Doe', email: 'john.doe@company.com' },
  onSearch,
  onNotificationClick,
  onProfileClick,
  onSettingsClick,
  onLogout,
}: HeaderProps) => {
  return (
    <Group h="100%" px="md" justify="space-between" className={classes.header}>
      <Group>
        {burger}
        {desktopBurger}
        <Text size="lg" fw={600} className={classes.title}>
          Employee Management System
        </Text>
      </Group>   
   <Group gap="md">
        <ActionIcon
          variant="subtle"
          size="lg"
          onClick={onSearch ? () => onSearch('') : undefined}
          className={classes.actionIcon}
        >
          <IconSearch size={18} />
        </ActionIcon>

        <HeaderNotifications onClick={onNotificationClick} />     
   <Menu shadow="md" width={200}>
          <Menu.Target>
            <Group className={classes.userMenu}>
              <Avatar src={user.avatar} size="sm" radius="xl">
                {user.name.split(' ').map(n => n[0]).join('')}
              </Avatar>
              <div className={classes.userInfo}>
                <Text size="sm" fw={500}>
                  {user.name}
                </Text>
                <Text size="xs" c="dimmed">
                  {user.email}
                </Text>
              </div>
            </Group>
          </Menu.Target>        
  <Menu.Dropdown>
            <Menu.Item
              leftSection={<IconUser size={14} />}
              onClick={onProfileClick}
            >
              Profile
            </Menu.Item>
            <Menu.Item
              leftSection={<IconSettings size={14} />}
              onClick={onSettingsClick}
            >
              Settings
            </Menu.Item>
            <Menu.Divider />
            <Menu.Item
              leftSection={<IconLogout size={14} />}
              onClick={onLogout}
              color="red"
            >
              Logout
            </Menu.Item>
          </Menu.Dropdown>
        </Menu>
      </Group>
    </Group>
  );
};