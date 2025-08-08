import { Group, Select, ActionIcon, Tooltip } from '@mantine/core';
import { IconRefresh, IconFilter } from '@tabler/icons-react';
import { NotificationFilter as FilterType, NotificationType, NotificationPriority } from '../types';
import classes from './NotificationFilter.module.css';

interface NotificationFilterProps {
  filter: FilterType;
  onChange: (filter: FilterType) => void;
  onRefresh?: () => void;
}

export const NotificationFilter = ({
  filter,
  onChange,
  onRefresh
}: NotificationFilterProps) => {
  const typeOptions = [
    { value: '', label: 'All Types' },
    { value: NotificationType.SYSTEM, label: 'System' },
    { value: NotificationType.ANNOUNCEMENT, label: 'Announcement' },
    { value: NotificationType.CHAT, label: 'Chat' },
    { value: NotificationType.EMAIL, label: 'Email' },
    { value: NotificationType.EMPLOYEE, label: 'Employee' },
    { value: NotificationType.DEPARTMENT, label: 'Department' },
    { value: NotificationType.PAYROLL, label: 'Payroll' },
    { value: NotificationType.SECURITY, label: 'Security' },
  ];

  const priorityOptions = [
    { value: '', label: 'All Priorities' },
    { value: NotificationPriority.URGENT, label: 'Urgent' },
    { value: NotificationPriority.HIGH, label: 'High' },
    { value: NotificationPriority.NORMAL, label: 'Normal' },
    { value: NotificationPriority.LOW, label: 'Low' },
  ];

  const readStatusOptions = [
    { value: '', label: 'All' },
    { value: 'false', label: 'Unread' },
    { value: 'true', label: 'Read' },
  ];

  const handleFilterChange = (key: keyof FilterType, value: string) => {
    const newFilter = { ...filter };
    
    if (value === '') {
      delete newFilter[key];
    } else {
      if (key === 'read') {
        newFilter[key] = value === 'true';
      } else {
        (newFilter as any)[key] = value;
      }
    }
    
    onChange(newFilter);
  };

  return (
    <Group gap="xs" className={classes.filterContainer}>
      <IconFilter size={14} color="var(--mantine-color-gray-6)" />
      
      <Select
        placeholder="Type"
        size="xs"
        data={typeOptions}
        value={filter.type || ''}
        onChange={(value) => handleFilterChange('type', value || '')}
        className={classes.filterSelect}
        clearable
      />

      <Select
        placeholder="Priority"
        size="xs"
        data={priorityOptions}
        value={filter.priority || ''}
        onChange={(value) => handleFilterChange('priority', value || '')}
        className={classes.filterSelect}
        clearable
      />

      <Select
        placeholder="Status"
        size="xs"
        data={readStatusOptions}
        value={filter.read !== undefined ? filter.read.toString() : ''}
        onChange={(value) => handleFilterChange('read', value || '')}
        className={classes.filterSelect}
        clearable
      />

      {onRefresh && (
        <Tooltip label="Refresh notifications">
          <ActionIcon
            size="sm"
            variant="subtle"
            onClick={onRefresh}
          >
            <IconRefresh size={14} />
          </ActionIcon>
        </Tooltip>
      )}
    </Group>
  );
};