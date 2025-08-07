import React from 'react';
import { clsx } from 'clsx';
import { Column } from '../types/ui.types';
import styles from './DataTable.module.css';

interface DataTableHeaderProps<T = any> {
  columns: Column<T>[];
  sortColumn?: string;
  sortDirection?: 'asc' | 'desc';
  onSort?: (columnKey: string) => void;
  showHeader?: boolean;
}

export const DataTableHeader = <T extends Record<string, any>>({
  columns,
  sortColumn,
  sortDirection,
  onSort,
  showHeader = true
}: DataTableHeaderProps<T>) => {
  if (!showHeader) return null;

  const handleSort = (column: Column<T>) => {
    if (column.sortable && onSort) {
      onSort(column.key);
    }
  };

  return (
    <thead className={styles.thead}>
      <tr className={styles.headerRow}>
        {columns.map((column) => (
          <th
            key={column.key}
            className={clsx(
              styles.th,
              {
                [styles.sortable]: column.sortable,
                [styles.sorted]: sortColumn === column.key,
              }
            )}
            style={{
              width: column.width,
              textAlign: column.align || 'left',
              position: column.fixed ? 'sticky' : undefined,
              left: column.fixed === 'left' ? 0 : undefined,
              right: column.fixed === 'right' ? 0 : undefined,
            }}
            onClick={() => handleSort(column)}
          >
            <div className={styles.headerContent}>
              <span className={styles.headerTitle}>{column.title}</span>
              {column.sortable && (
                <span className={styles.sortIcon}>
                  {sortColumn === column.key ? (
                    sortDirection === 'asc' ? '↑' : '↓'
                  ) : (
                    '↕'
                  )}
                </span>
              )}
            </div>
          </th>
        ))}
      </tr>
    </thead>
  );
};