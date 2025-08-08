import React, { useState, useMemo, useRef } from 'react';
import { clsx } from 'clsx';
import { DataTableProps } from '../types/ui.types';
import { DataTableHeader } from './DataTableHeader';
import { DataTableRow } from './DataTableRow';
import { DataTablePagination } from './DataTablePagination';
import { LoadingSpinner } from '../LoadingSpinner';
import { useResponsive } from '@/hooks';
import { useAnnouncements } from '@/hooks/useAccessibility';
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
  testId,
  caption,
  ariaLabel,
  ariaLabelledBy
}: DataTableProps<T>) => {
  const [sortColumn, setSortColumn] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>(
    rowSelection?.selectedRowKeys || []
  );
  const [focusedRowIndex, setFocusedRowIndex] = useState<number>(-1);
  const { isMobile, isTablet } = useResponsive();
  const tableRef = useRef<HTMLTableElement>(null);
  const { announce } = useAnnouncements();
  
  // Responsive size adjustment
  const responsiveSize = isMobile ? 'small' : isTablet ? 'middle' : size;

  // Handle sorting with accessibility announcements
  const handleSort = (columnKey: string) => {
    const column = columns.find(col => col.key === columnKey);
    const columnTitle = column?.title || columnKey;
    
    if (sortColumn === columnKey) {
      const newDirection = sortDirection === 'asc' ? 'desc' : 'asc';
      setSortDirection(newDirection);
      announce(`Table sorted by ${columnTitle} in ${newDirection}ending order`);
    } else {
      setSortColumn(columnKey);
      setSortDirection('asc');
      announce(`Table sorted by ${columnTitle} in ascending order`);
    }
  };

  // Handle keyboard navigation
  const handleTableKeyDown = (event: React.KeyboardEvent) => {
    if (!data.length) return;

    const { key } = event;
    let newFocusedIndex = focusedRowIndex;

    switch (key) {
      case 'ArrowDown':
        event.preventDefault();
        newFocusedIndex = Math.min(focusedRowIndex + 1, data.length - 1);
        break;
      case 'ArrowUp':
        event.preventDefault();
        newFocusedIndex = Math.max(focusedRowIndex - 1, 0);
        break;
      case 'Home':
        event.preventDefault();
        newFocusedIndex = 0;
        break;
      case 'End':
        event.preventDefault();
        newFocusedIndex = data.length - 1;
        break;
      case 'Enter':
      case ' ':
        if (focusedRowIndex >= 0 && onRow?.onClick) {
          event.preventDefault();
          onRow.onClick(data[focusedRowIndex], focusedRowIndex);
        }
        break;
    }

    if (newFocusedIndex !== focusedRowIndex && newFocusedIndex >= 0) {
      setFocusedRowIndex(newFocusedIndex);
      // Focus the row
      const rows = tableRef.current?.querySelectorAll('tbody tr');
      const targetRow = rows?.[newFocusedIndex] as HTMLElement;
      if (targetRow) {
        targetRow.focus();
      }
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
        role="region"
        aria-label={ariaLabel || "Data table"}
        tabIndex={0}
        onKeyDown={handleTableKeyDown}
      >
        <table 
          ref={tableRef}
          className={tableClasses}
          role="table"
          aria-label={ariaLabel}
          aria-labelledby={ariaLabelledBy}
          aria-rowcount={data.length}
          aria-colcount={columns.length + (rowSelection ? 1 : 0)}
        >
          {caption && (
            <caption className={styles.caption}>
              {caption}
            </caption>
          )}
          <DataTableHeader
            columns={columns}
            sortColumn={sortColumn}
            sortDirection={sortDirection}
            onSort={handleSort}
            showHeader={showHeader}
            rowSelection={rowSelection}
            onSelectAll={handleSelectAll}
            selectedRowKeys={selectedRowKeys}
            totalRows={sortedData.length}
          />
          <tbody className={styles.tbody} role="rowgroup">
            {sortedData.length === 0 ? (
              <tr role="row">
                <td 
                  colSpan={columns.length + (rowSelection ? 1 : 0)} 
                  className={styles.emptyCell}
                  role="cell"
                >
                  <div className={styles.emptyState} role="status" aria-live="polite">
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
                  focused={index === focusedRowIndex}
                  onFocus={() => setFocusedRowIndex(index)}
                />
              ))
            )}
          </tbody>
        </table>
      </div>

      {pagination && (
        <DataTablePagination 
          pagination={pagination}
          ariaLabel="Table pagination navigation"
        />
      )}
      
      {/* Screen reader announcements */}
      <div className="sr-only" aria-live="polite" aria-atomic="true">
        {loading && "Loading table data"}
        {sortColumn && `Table sorted by ${columns.find(col => col.key === sortColumn)?.title || sortColumn} in ${sortDirection}ending order`}
        {selectedRowKeys.length > 0 && `${selectedRowKeys.length} row${selectedRowKeys.length === 1 ? '' : 's'} selected`}
      </div>
    </div>
  );
};