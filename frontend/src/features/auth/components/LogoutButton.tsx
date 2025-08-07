import React from 'react';
import { Button, Menu, UnstyledButton, Group, Text, Avatar } from '@mantine/core';
import { IconLogout, IconChevronDown, IconUser, IconSettings } from '@tabler/icons-react';
import { useAuth, useAuthActions } from '../../../stores/authStore';
import { modals } from '@mantine/modals';

interface LogoutButtonProps {
  variant?: 'button' | 'menu';
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
}

export const LogoutButton: React.FC<LogoutButtonProps> = ({
  variant = 'button',
  size = 'sm',
}) => {
  const { user, isLoading } = useAuth();
  const { logout } = useAuthActions();

  const handleLogout = () => {
    modals.openConfirmModal({
      title: 'Confirm Logout',
      children: (
        <Text size="sm">
          Are you sure you want to log out? You will need to sign in again to access your account.
        </Text>
      ),
      labels: { confirm: 'Logout', cancel: 'Cancel' },
      confirmProps: { color: 'red' },
      onConfirm: async () => {
        try {
          await logout();
        } catch (error) {
          console.error('Logout failed:', error);
        }
      },
    });
  };

  if (variant === 'menu' && user) {
    return (
      <Menu shadow="md" width={200} position="bottom-end">
        <Menu.Target>
          <UnstyledButton>
            <Group gap="xs">
              <Avatar
                src={null}
                alt={`${user.firstName} ${user.lastName}`}
                size={32}
                color="blue"
              >
                {user.firstName?.[0]}{user.lastName?.[0]}
              </Avatar>
              
              <div style={{ flex: 1 }}>
                <Text size="sm" fw={500}>
                  {user.firstName} {user.lastName}
                </Text>
                <Text c="dimmed" size="xs">
                  {user.email}
                </Text>
              </div>
              
              <IconChevronDown size={14} />
            </Group>
          </UnstyledButton>
        </Menu.Target>

        <Menu.Dropdown>
          <Menu.Label>Account</Menu.Label>
          
          <Menu.Item
            leftSection={<IconUser size={14} />}
            onClick={() => {
              // Navigate to profile page
              console.log('Navigate to profile');
            }}
          >
            Profile
          </Menu.Item>
          
          <Menu.Item
            leftSection={<IconSettings size={14} />}
            onClick={() => {
              // Navigate to settings page
              console.log('Navigate to settings');
            }}
          >
            Settings
          </Menu.Item>
          
          <Menu.Divider />
          
          <Menu.Item
            leftSection={<IconLogout size={14} />}
            color="red"
            onClick={handleLogout}
            disabled={isLoading}
          >
            Logout
          </Menu.Item>
        </Menu.Dropdown>
      </Menu>
    );
  }

  return (
    <Button
      leftSection={<IconLogout size={16} />}
      variant="light"
      color="red"
      size={size}
      onClick={handleLogout}
      loading={isLoading}
      disabled={isLoading}
    >
      Logout
    </Button>
  );
};

export default LogoutButton;