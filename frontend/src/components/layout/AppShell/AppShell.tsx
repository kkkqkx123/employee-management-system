import { AppShell as MantineAppShell, Burger } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { ReactNode } from 'react';
import { Navigation } from '../Navigation';
import { Header } from '../Header';
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

  return (
    <MantineAppShell
      header={withHeader ? { height: headerHeight } : undefined}
      navbar={
        withNavigation
          ? {
              width: navigationWidth,
              breakpoint: 'sm',
              collapsed: { mobile: !mobileOpened, desktop: !desktopOpened },
            }
          : undefined
      }
      padding="md"
      className={classes.shell}
    >
      {withHeader && (
        <MantineAppShell.Header className={classes.header}>
          <Header
            burger={
              withNavigation ? (
                <Burger
                  opened={mobileOpened}
                  onClick={toggleMobile}
                  hiddenFrom="sm"
                  size="sm"
                />
              ) : undefined
            }
            desktopBurger={
              withNavigation ? (
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

      {withNavigation && (
        <MantineAppShell.Navbar p="md" className={classes.navbar}>
          <Navigation />
        </MantineAppShell.Navbar>
      )}

      <MantineAppShell.Main className={classes.main}>
        {children}
      </MantineAppShell.Main>
    </MantineAppShell>
  );
};