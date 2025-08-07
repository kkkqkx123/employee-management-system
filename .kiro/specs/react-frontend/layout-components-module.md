# Layout Components Module Implementation Guide

## Overview
This document provides detailed implementation specifications for the core layout components including AppShell, Navigation, Header, and responsive layout management.

## File Structure
```
src/components/layout/
├── AppShell/
│   ├── AppShell.tsx
│   ├── AppShell.module.css
│   ├── AppShell.test.tsx
│   └── index.ts
├── Navigation/
│   ├── Navigation.tsx
│   ├── NavigationItem.tsx
│   ├── NavigationGroup.tsx
│   ├── Navigation.module.css
│   ├── Navigation.test.tsx
│   └── index.ts
├── Header/
│   ├── Header.tsx
│   ├── UserMenu.tsx
│   ├── NotificationBell.tsx
│   ├── SearchBar.tsx
│   ├── Header.module.css
│   ├── Header.test.tsx
│   └── index.ts
├── Sidebar/
│   ├── Sidebar.tsx
│   ├── Sidebar.module.css
│   └── index.ts
└── index.ts
```

## Type Definitions

### layout.types.ts
```typescript
export interface NavigationItem {
  key: string;
  label: string;
  icon?: React.ReactNode;
  path?: string;
  children?: NavigationItem[];
  permissions?: string[];
  roles?: string[];
  badge?: string | number;
  disabled?: boolean;
  external?: boolean;
}

export interface AppShellProps {
  children: React.ReactNode;
  navigation?: NavigationItem[];
  user?: User;
  className?: string;
  sidebarCollapsed?: boolean;
  onSidebarToggle?: (collapsed: boolean) => void;
}

export interface HeaderProps {
  user?: User;
  onMenuToggle?: () => void;
  showMenuToggle?: boolean;
  searchPlaceholder?: string;
  onSearch?: (query: string) => void;
  className?: string;
}

export interface NavigationProps {
  items: NavigationItem[];
  collapsed?: boolean;
  activeKey?: string;
  onItemClick?: (item: NavigationItem) => void;
  className?: string;
}

export interface SidebarProps {
  children: React.ReactNode;
  collapsed?: boolean;
  width?: number;
  collapsedWidth?: number;
  className?: string;
}
```

## AppShell Component

### AppShell.tsx
```typescript
import React, { useState, useEffect } from 'react';
import { clsx } from 'clsx';
import { useMediaQuery } from '@mantine/hooks';
import { Header } from '../Header';
import { Navigation } from '../Navigation';
import { Sidebar } from '../Sidebar';
import { AppShellProps } from '../types/layout.types';
import { useAuth } from '../../features/auth/hooks/useAuth';
import styles from './AppShell.module.css';

export const AppShell: React.FC<AppShellProps> = ({
  children,
  navigation = [],
  className,
  sidebarCollapsed: controlledCollapsed,
  onSidebarToggle,
}) => {
  const { user } = useAuth();
  const isMobile = useMediaQuery('(max-width: 768px)');
  const [internalCollapsed, setInternalCollapsed] = useState(false);
  
  // Use controlled or internal state
  const collapsed = controlledCollapsed !== undefined ? controlledCollapsed : internalCollapsed;
  const setCollapsed = onSidebarToggle || setInternalCollapsed;

  // Auto-collapse on mobile
  useEffect(() => {
    if (isMobile && !collapsed) {
      setCollapsed(true);
    }
  }, [isMobile, collapsed, setCollapsed]);

  const handleMenuToggle = () => {
    setCollapsed(!collapsed);
  };

  const handleNavigationItemClick = () => {
    // Auto-collapse on mobile after navigation
    if (isMobile && !collapsed) {
      setCollapsed(true);
    }
  };

  const appShellClasses = clsx(
    styles.appShell,
    {
      [styles.sidebarCollapsed]: collapsed,
      [styles.mobile]: isMobile,
    },
    className
  );

  return (
    <div className={appShellClasses}>
      <Header
        user={user}
        onMenuToggle={handleMenuToggle}
        showMenuToggle={true}
        className={styles.header}
      />
      
      <Sidebar
        collapsed={collapsed}
        className={styles.sidebar}
      >
        <Navigation
          items={navigation}
          collapsed={collapsed}
          onItemClick={handleNavigationItemClick}
        />
      </Sidebar>
      
      <main className={styles.main}>
        <div className={styles.content}>
          {children}
        </div>
      </main>
      
      {/* Mobile overlay */}
      {isMobile && !collapsed && (
        <div
          className={styles.overlay}
          onClick={() => setCollapsed(true)}
          aria-hidden="true"
        />
      )}
    </div>
  );
};
```

### AppShell.module.css
```css
.appShell {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  transition: left 0.3s ease;
}

.sidebar {
  position: fixed;
  top: 60px; /* Header height */
  left: 0;
  bottom: 0;
  z-index: 90;
  transition: transform 0.3s ease;
}

.main {
  flex: 1;
  margin-top: 60px; /* Header height */
  margin-left: 280px; /* Sidebar width */
  transition: margin-left 0.3s ease;
  overflow: hidden;
}

.content {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
}

.sidebarCollapsed .main {
  margin-left: 80px; /* Collapsed sidebar width */
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 85;
}

/* Mobile styles */
.mobile .header {
  left: 0;
}

.mobile .sidebar {
  transform: translateX(-100%);
}

.mobile .main {
  margin-left: 0;
}

.mobile:not(.sidebarCollapsed) .sidebar {
  transform: translateX(0);
}

@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
}
```## He
ader Component

### Header.tsx
```typescript
import React, { useState } from 'react';
import { clsx } from 'clsx';
import { Button, TextInput } from '@mantine/core';
import { IconMenu2, IconSearch, IconBell } from '@tabler/icons-react';
import { UserMenu } from './UserMenu';
import { NotificationBell } from './NotificationBell';
import { SearchBar } from './SearchBar';
import { HeaderProps } from '../types/layout.types';
import { User } from '../../features/auth/types/auth.types';
import styles from './Header.module.css';

export const Header: React.FC<HeaderProps> = ({
  user,
  onMenuToggle,
  showMenuToggle = false,
  searchPlaceholder = 'Search...',
  onSearch,
  className,
}) => {
  const headerClasses = clsx(styles.header, className);

  return (
    <header className={headerClasses}>
      <div className={styles.left}>
        {showMenuToggle && (
          <Button
            variant="subtle"
            size="sm"
            onClick={onMenuToggle}
            className={styles.menuToggle}
            aria-label="Toggle navigation menu"
          >
            <IconMenu2 size={20} />
          </Button>
        )}
        
        <div className={styles.logo}>
          <h1>Employee Management</h1>
        </div>
      </div>

      <div className={styles.center}>
        <SearchBar
          placeholder={searchPlaceholder}
          onSearch={onSearch}
          className={styles.searchBar}
        />
      </div>

      <div className={styles.right}>
        <NotificationBell className={styles.notifications} />
        
        {user && (
          <UserMenu
            user={user}
            className={styles.userMenu}
          />
        )}
      </div>
    </header>
  );
};
```

### UserMenu.tsx
```typescript
import React, { useState } from 'react';
import { Menu, Avatar, Text, Divider } from '@mantine/core';
import { IconUser, IconSettings, IconLogout, IconChevronDown } from '@tabler/icons-react';
import { useAuth } from '../../features/auth/hooks/useAuth';
import { User } from '../../features/auth/types/auth.types';
import styles from './Header.module.css';

interface UserMenuProps {
  user: User;
  className?: string;
}

export const UserMenu: React.FC<UserMenuProps> = ({ user, className }) => {
  const { logout } = useAuth();
  const [opened, setOpened] = useState(false);

  const handleLogout = () => {
    logout();
    setOpened(false);
  };

  const displayName = user.firstName && user.lastName 
    ? `${user.firstName} ${user.lastName}`
    : user.username;

  return (
    <Menu
      opened={opened}
      onChange={setOpened}
      position="bottom-end"
      withArrow
      arrowPosition="center"
    >
      <Menu.Target>
        <button className={`${styles.userMenuTrigger} ${className}`}>
          <Avatar
            src={user.profilePicture}
            alt={displayName}
            size="sm"
            radius="xl"
          />
          <div className={styles.userInfo}>
            <Text size="sm" weight={500}>
              {displayName}
            </Text>
            <Text size="xs" color="dimmed">
              {user.email}
            </Text>
          </div>
          <IconChevronDown size={16} />
        </button>
      </Menu.Target>

      <Menu.Dropdown>
        <Menu.Label>Account</Menu.Label>
        
        <Menu.Item
          icon={<IconUser size={16} />}
          onClick={() => {
            // Navigate to profile
            setOpened(false);
          }}
        >
          Profile
        </Menu.Item>
        
        <Menu.Item
          icon={<IconSettings size={16} />}
          onClick={() => {
            // Navigate to settings
            setOpened(false);
          }}
        >
          Settings
        </Menu.Item>
        
        <Divider />
        
        <Menu.Item
          icon={<IconLogout size={16} />}
          color="red"
          onClick={handleLogout}
        >
          Logout
        </Menu.Item>
      </Menu.Dropdown>
    </Menu>
  );
};
```

### SearchBar.tsx
```typescript
import React, { useState, useCallback } from 'react';
import { TextInput } from '@mantine/core';
import { IconSearch } from '@tabler/icons-react';
import { useDebouncedCallback } from '@mantine/hooks';

interface SearchBarProps {
  placeholder?: string;
  onSearch?: (query: string) => void;
  className?: string;
  debounceMs?: number;
}

export const SearchBar: React.FC<SearchBarProps> = ({
  placeholder = 'Search...',
  onSearch,
  className,
  debounceMs = 300,
}) => {
  const [value, setValue] = useState('');

  const debouncedSearch = useDebouncedCallback((query: string) => {
    onSearch?.(query);
  }, debounceMs);

  const handleChange = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = event.target.value;
    setValue(newValue);
    debouncedSearch(newValue);
  }, [debouncedSearch]);

  const handleKeyDown = useCallback((event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      onSearch?.(value);
    }
  }, [value, onSearch]);

  return (
    <TextInput
      placeholder={placeholder}
      value={value}
      onChange={handleChange}
      onKeyDown={handleKeyDown}
      icon={<IconSearch size={16} />}
      className={className}
      styles={{
        root: { width: '100%', maxWidth: 400 },
        input: { 
          backgroundColor: 'var(--color-gray-50)',
          border: '1px solid var(--color-gray-200)',
        }
      }}
    />
  );
};
```

### Header.module.css
```css
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
  background: white;
  border-bottom: 1px solid var(--color-gray-200);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.menuToggle {
  padding: 8px;
  border-radius: 6px;
}

.logo h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-gray-900);
}

.center {
  flex: 1;
  display: flex;
  justify-content: center;
  max-width: 500px;
  margin: 0 24px;
}

.searchBar {
  width: 100%;
}

.right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notifications {
  position: relative;
}

.userMenuTrigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.userMenuTrigger:hover {
  background-color: var(--color-gray-100);
}

.userInfo {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
}

@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }
  
  .center {
    display: none;
  }
  
  .logo h1 {
    font-size: 16px;
  }
  
  .userInfo {
    display: none;
  }
}
```

## Navigation Component

### Navigation.tsx
```typescript
import React from 'react';
import { useLocation } from 'react-router-dom';
import { clsx } from 'clsx';
import { NavigationItem } from './NavigationItem';
import { NavigationGroup } from './NavigationGroup';
import { NavigationProps } from '../types/layout.types';
import { useAuth } from '../../features/auth/hooks/useAuth';
import styles from './Navigation.module.css';

export const Navigation: React.FC<NavigationProps> = ({
  items,
  collapsed = false,
  activeKey,
  onItemClick,
  className,
}) => {
  const location = useLocation();
  const { hasPermission, hasRole } = useAuth();

  const isItemVisible = (item: NavigationItem): boolean => {
    // Check permissions
    if (item.permissions && item.permissions.length > 0) {
      if (!item.permissions.some(permission => hasPermission(permission))) {
        return false;
      }
    }

    // Check roles
    if (item.roles && item.roles.length > 0) {
      if (!item.roles.some(role => hasRole(role))) {
        return false;
      }
    }

    return true;
  };

  const isItemActive = (item: NavigationItem): boolean => {
    if (activeKey) {
      return activeKey === item.key;
    }
    
    if (item.path) {
      return location.pathname === item.path || 
             location.pathname.startsWith(item.path + '/');
    }
    
    return false;
  };

  const renderNavigationItem = (item: NavigationItem) => {
    if (!isItemVisible(item)) {
      return null;
    }

    const hasChildren = item.children && item.children.length > 0;
    const isActive = isItemActive(item);

    if (hasChildren) {
      return (
        <NavigationGroup
          key={item.key}
          item={item}
          collapsed={collapsed}
          isActive={isActive}
          onItemClick={onItemClick}
        >
          {item.children?.map(renderNavigationItem)}
        </NavigationGroup>
      );
    }

    return (
      <NavigationItem
        key={item.key}
        item={item}
        collapsed={collapsed}
        isActive={isActive}
        onClick={() => onItemClick?.(item)}
      />
    );
  };

  const navigationClasses = clsx(
    styles.navigation,
    {
      [styles.collapsed]: collapsed,
    },
    className
  );

  return (
    <nav className={navigationClasses} role="navigation">
      <div className={styles.navigationContent}>
        {items.map(renderNavigationItem)}
      </div>
    </nav>
  );
};
```