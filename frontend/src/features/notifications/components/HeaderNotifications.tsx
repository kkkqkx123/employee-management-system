import { NotificationDropdown } from './NotificationDropdown';
import { useNotifications } from '../hooks/useNotifications';

interface HeaderNotificationsProps {
  /** Custom click handler */
  onClick?: () => void;
}

export const HeaderNotifications = ({ onClick }: HeaderNotificationsProps) => {
  const { unreadCount } = useNotifications();

  return (
    <NotificationDropdown
      maxHeight={350}
      showFilter={false}
    />
  );
};