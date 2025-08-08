import { AppShell as MantineAppShell, Burger } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { ReactNode, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Navigation } from '../Navigation';
import { MobileNavigation } from '../MobileNavigation';
import { Header } from '../Header';
import { Breadcrumbs } from '../Breadcrumbs';
import { ROUTES } from '@/constants';
import { useResponsive } from '@/hooks';
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
  const [mobileOpened, { toggle: toggleMobile, close: closeMobile }] = useDisclosure();
  const [desktopOpened, { toggle: toggleDesktop }] = useDisclosure(true);
  const location = useLocation();
  const { isMobile, isTablet } = useResponsive();
  
  // Don't show navigation and header on login page
  const isLoginPage = location.pathname === ROUTES.LOGIN;
  const shouldShowNavigation = withNavigation && !isLoginPage;
  const shouldShowHeader = withHeader && !isLoginPage;
  
  // Responsive navigation width
  const responsiveNavWidth = isMobile ? Math.min(navigationWidth, 280) : navigationWidth;
  
  // Close mobile navigation on route change
  useEffect(() => {
    if (isMobile) {
      closeMobile();
    }
  }, [location.pathname, isMobile, closeMobile]);
  
  // Auto-collapse desktop navigation on tablet
  useEffect(() => {
    if (isTablet && desktopOpened) {
      toggleDesktop();
    }
  }, [isTablet]); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <MantineAppShell
      header={shouldShowHeader ? { height: headerHeight } : undefined}
      navbar={
        shouldShowNavigation
          ? {
              width: responsiveNavWidth,
              breakpoint: 'sm',
              collapsed: { mobile: !mobileOpened, desktop: !desktopOpened },
            }
          : undefined
      }
      padding={isLoginPage ? 0 : isMobile ? "sm" : "md"}
      className={classes.shell}
    >
      {shouldShowHeader && (
        <MantineAppShell.Header className={classes.header} component="header" role="banner">
          <Header
            burger={
              shouldShowNavigation ? (
                <Burger
                  opened={mobileOpened}
                  onClick={toggleMobile}
                  hiddenFrom="sm"
                  size="sm"
                  aria-label={mobileOpened ? "Close navigation menu" : "Open navigation menu"}
                  aria-expanded={mobileOpened}
                  aria-controls="mobile-navigation"
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
                  aria-label={desktopOpened ? "Collapse navigation" : "Expand navigation"}
                  aria-expanded={desktopOpened}
                  aria-controls="desktop-navigation"
                />
              ) : undefined
            }
          />
        </MantineAppShell.Header>
      )}

      {shouldShowNavigation && !isMobile && (
        <MantineAppShell.Navbar 
          p="md" 
          className={classes.navbar}
          component="aside"
          role="complementary"
          id="desktop-navigation"
          aria-label="Main navigation"
        >
          <Navigation />
        </MantineAppShell.Navbar>
      )}

      {/* Mobile Navigation Drawer */}
      {shouldShowNavigation && isMobile && (
        <MobileNavigation 
          opened={mobileOpened} 
          onClose={closeMobile}
          id="mobile-navigation"
        />
      )}

      <MantineAppShell.Main 
        className={classes.main}
        component="main"
        role="main"
        id="main-content"
        tabIndex={-1}
      >
        {!isLoginPage && (
          <nav aria-label="Breadcrumb navigation">
            <Breadcrumbs />
          </nav>
        )}
        {children}
      </MantineAppShell.Main>
    </MantineAppShell>
  );
};