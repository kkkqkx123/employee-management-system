import React, { useState, useMemo } from 'react';
import { clsx } from 'clsx';
import { DataTableProps } from '../types/ui.types';
import { DataTableHeader } from './DataTableHeader';
import { DataTableRow } from './DataTableRow';
import { DataTablePagination } from './DataTablePagination';
import { LoadingSpinner } from '../LoadingSpinner';
import { useResponsive } from '@/hooks';
import styles from './DataTable.module.css';

export const DataTable = <T extends Record<string, any>>({
  data,
  columns,
  loading = false,
  pagination,
  rowSelection,
  onRow,
  scroll,
  size = 'middle',
  bordered = false,
  showHeader = true,
  className,
  testId
}: DataTableProps<T>) => {
  const [sortColumn, setSortColumn] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>(
    rowSelection?.selectedRowKeys || []
  );
  const { isMobile, isTablet } = useResponsive();
  
  // Responsive size adjustment
  const responsiveSize = isMobile ? 'small' : isTablet ? 'middle' : size;

  // Handle sorting
  const handleSort = (columnKey: string) => {
    if (sortColumn === columnKey) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortColumn(columnKey);
      setSortDirection('asc');
    }
  };

  // Sort data
  const sortedData = useMemo(() => {
    if (!sortColumn) return data;
    
    return [...data].sort((a, b) => {
      const aValue = a[sortColumn];
      const bValue = b[sortColumn];
      
      if (aValue < bValue) return sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }, [data, sortColumn, sortDirection]);

  // Handle row selection
  const handleSelectionChange = (newSelectedRowKeys: React.Key[], selectedRows: T[]) => {
    setSelectedRowKeys(newSelectedRowKeys);
    rowSelection?.onChange?.(newSelectedRowKeys, selectedRows);
  };

  const handleSelectAll = (checked: boolean) => {
    const allKeys = sortedData.map((record, index) => {
      if (typeof rowSelection?.selectedRowKeys === 'function') {
        return (rowSelection.selectedRowKeys as any)(record);
      }
      return record.id || index.toString();
    });

    const newSelectedKeys = checked ? allKeys : [];
    const selectedRows = checked ? sortedData : [];
    
    setSelectedRowKeys(newSelectedKeys);
    rowSelection?.onSelectAll?.(checked, selectedRows, sortedData);
    rowSelection?.onChange?.(newSelectedKeys, selectedRows);
  };

  const tableClasses = clsx(
    styles.table,
    styles[responsiveSize],
    {
      [styles.bordered]: bordered,
      [styles.loading]: loading,
      [styles.mobile]: isMobile,
      [styles.tablet]: isTablet,
    },
    className
  );

  const containerClasses = clsx(
    styles.container,
    {
      [styles.scrollable]: scroll || isMobile, // Always scrollable on mobile
      [styles.mobileContainer]: isMobile,
    }
  );

  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className={containerClasses} data-testid={testId}>
      <div 
        className={styles.tableWrapper}
        style={{
          overflowX: scroll?.x || isMobile ? 'auto' : undefined,
          overflowY: scroll?.y ? 'auto' : undefined,
          maxHeight: scroll?.y,
        }}
      >
        <table className={tableClasses}>
          <DataTableHeader
            columns={columns}
            sortColumn={sortColumn}
            sortDirection={sortDirection}
            onSort={handleSort}
            showHeader={showHeader}
          />
          <tbody className={styles.tbody}>
            {sortedData.length === 0 ? (
              <tr>
                <td 
                  colSpan={columns.length + (rowSelection ? 1 : 0)} 
                  className={styles.emptyCell}
                >
                  <div className={styles.emptyState}>
                    No data available
                  </div>
                </td>
              </tr>
            ) : (
              sortedData.map((record, index) => (
                <DataTableRow
                  key={record.id || index}
                  record={record}
                  index={index}
                  columns={columns}
                  rowKey="id"
                  rowSelection={rowSelection ? {
                    ...rowSelection,
                    onChange: handleSelectionChange,
                  } : undefined}
                  onRow={onRow}
                  selectedRowKeys={selectedRowKeys}
                />
              ))
            )}
          </tbody>
        </table>
      </div>

      {pagination && (
        <DataTablePagination pagination={pagination} />
      )}
    </div>
  );
};