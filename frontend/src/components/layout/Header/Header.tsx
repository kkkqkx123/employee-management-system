import { Group, Text, ActionIcon, Menu, Avatar, Tooltip } from '@mantine/core';
import { 
  IconUser, 
  IconSettings, 
  IconLogout,
  IconSearch
} from '@tabler/icons-react';
import type { ReactNode } from 'react';
import { HeaderNotifications } from '../../../features/notifications';
import { useResponsive } from '@/hooks';
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
  const { isMobile } = useResponsive();
  
  return (
    <Group h="100%" px={isMobile ? "sm" : "md"} justify="space-between" className={classes.header}>
      <Group gap={isMobile ? "xs" : "sm"}>
        {burger}
        {desktopBurger}
        <Text 
          size={isMobile ? "md" : "lg"} 
          fw={600} 
          className={classes.title}
          truncate
        >
          {isMobile ? "EMS" : "Employee Management System"}
        </Text>
      </Group>   
      
      <Group gap={isMobile ? "xs" : "md"}>
        {!isMobile && (
          <Tooltip label="Search">
            <ActionIcon
              variant="subtle"
              size="lg"
              onClick={onSearch ? () => onSearch('') : undefined}
              className={classes.actionIcon}
            >
              <IconSearch size={18} />
            </ActionIcon>
          </Tooltip>
        )}

        <HeaderNotifications onClick={onNotificationClick} />     
        
        <Menu shadow="md" width={isMobile ? 180 : 200} position="bottom-end">
          <Menu.Target>
            <Group className={classes.userMenu} gap={isMobile ? "xs" : "sm"}>
              <Avatar src={user.avatar} size={isMobile ? "sm" : "sm"} radius="xl">
                {user.name.split(' ').map(n => n[0]).join('')}
              </Avatar>
              {!isMobile && (
                <div className={classes.userInfo}>
                  <Text size="sm" fw={500} truncate>
                    {user.name}
                  </Text>
                  <Text size="xs" c="dimmed" truncate>
                    {user.email}
                  </Text>
                </div>
              )}
            </Group>
          </Menu.Target>        
          
          <Menu.Dropdown>
            {isMobile && (
              <>
                <Menu.Label>
                  <Text size="xs" truncate>{user.name}</Text>
                  <Text size="xs" c="dimmed" truncate>{user.email}</Text>
                </Menu.Label>
                <Menu.Divider />
              </>
            )}
            
            {isMobile && (
              <Menu.Item
                leftSection={<IconSearch size={14} />}
                onClick={onSearch ? () => onSearch('') : undefined}
              >
                Search
              </Menu.Item>
            )}
            
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