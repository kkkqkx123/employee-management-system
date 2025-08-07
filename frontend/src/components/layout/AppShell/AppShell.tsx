import { AppShell as MantineAppShell, Burger } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { ReactNode } from 'react';
import { useLocation } from 'react-router-dom';
import { Navigation } from '../Navigation';
import { Header } from '../Header';
import { Breadcrumbs } from '../Breadcrumbs';
import { ROUTES } from '@/constants';
import classes from './AppShell.module.css';

export interface AppShellProps {
  /** Main content */
  children: ReactNode;
  /** Whether to show navigation */
  withNavigation?: boolean;
  /** Whether to show header */
  withHeader?: boolean;
  /** Custom navigation width */
  navigationWidth?: number;
  /** Custom header height */
  headerHeight?: number;
}

export const AppShell = ({
  children,
  withNavigation = true,
  withHeader = true,
  navigationWidth = 280,
  headerHeight = 60,
}: AppShellProps) => {
  const [mobileOpened, { toggle: toggleMobile }] = useDisclosure();
  const [desktopOpened, { toggle: toggleDesktop }] = useDisclosure(true);
  const location = useLocation();
  
  // Don't show navigation and header on login page
  const isLoginPage = location.pathname === ROUTES.LOGIN;
  const shouldShowNavigation = withNavigation && !isLoginPage;
  const shouldShowHeader = withHeader && !isLoginPage;

  return (
    <MantineAppShell
      header={shouldShowHeader ? { height: headerHeight } : undefined}
      navbar={
        shouldShowNavigation
          ? {
              width: navigationWidth,
              breakpoint: 'sm',
              collapsed: { mobile: !mobileOpened, desktop: !desktopOpened },
            }
          : undefined
      }
      padding={isLoginPage ? 0 : "md"}
      className={classes.shell}
    >
      {shouldShowHeader && (
        <MantineAppShell.Header className={classes.header}>
          <Header
            burger={
              shouldShowNavigation ? (
                <Burger
                  opened={mobileOpened}
                  onClick={toggleMobile}
                  hiddenFrom="sm"
                  size="sm"
                />
              ) : undefined
            }
            desktopBurger={
              shouldShowNavigation ? (
                <Burger
                  opened={desktopOpened}
                  onClick={toggleDesktop}
                  visibleFrom="sm"
                  size="sm"
                />
              ) : undefined
            }
          />
        </MantineAppShell.Header>
      )}

      {shouldShowNavigation && (
        <MantineAppShell.Navbar p="md" className={classes.navbar}>
          <Navigation />
        </MantineAppShell.Navbar>
      )}

      <MantineAppShell.Main className={classes.main}>
        {!isLoginPage && <Breadcrumbs />}
        {children}
      </MantineAppShell.Main>
    </MantineAppShell>
  );
};