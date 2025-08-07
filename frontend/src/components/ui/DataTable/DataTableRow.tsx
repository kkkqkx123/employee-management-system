import React from 'react';
import { clsx } from 'clsx';
import { Column, RowSelectionConfig } from '../types/ui.types';
import styles from './DataTable.module.css';

interface DataTableRowProps<T = any> {
  record: T;
  index: number;
  columns: Column<T>[];
  rowKey: string | ((record: T) => string);
  rowSelection?: RowSelectionConfig<T>;
  onRow?: (record: T, index: number) => React.HTMLAttributes<HTMLTableRowElement>;
  selectedRowKeys?: React.Key[];
}

export const DataTableRow = <T extends Record<string, any>>({
  record,
  index,
  columns,
  rowKey,
  rowSelection,
  onRow,
  selectedRowKeys = []
}: DataTableRowProps<T>) => {
  const getRowKey = (): string => {
    if (typeof rowKey === 'function') {
      return rowKey(record);
    }
    return record[rowKey] || index.toString();
  };

  const key = getRowKey();
  const isSelected = selectedRowKeys.includes(key);
  const rowProps = onRow?.(record, index) || {};

  const handleSelectionChange = (checked: boolean) => {
    if (rowSelection?.onChange) {
      const newSelectedKeys = checked
        ? [...selectedRowKeys, key]
        : selectedRowKeys.filter(k => k !== key);
      
      const selectedRows = newSelectedKeys.map(selectedKey => {
        // This is a simplified approach - in a real implementation,
        // you'd need access to all data to find the selected records
        return selectedKey === key ? record : null;
      }).filter(Boolean) as T[];

      rowSelection.onChange(newSelectedKeys, selectedRows);
    }

    if (rowSelection?.onSelect) {
      rowSelection.onSelect(record, checked, []);
    }
  };

  const checkboxProps = rowSelection?.getCheckboxProps?.(record) || {};

  return (
    <tr
      key={key}
      className={clsx(
        styles.tr,
        {
          [styles.selected]: isSelected,
          [styles.clickable]: !!onRow,
        }
      )}
      {...rowProps}
    >
      {rowSelection && (
        <td className={styles.td}>
          <input
            type={rowSelection.type || 'checkbox'}
            checked={isSelected}
            onChange={(e) => handleSelectionChange(e.target.checked)}
            disabled={checkboxProps.disabled}
            className={styles.selectionInput}
          />
        </td>
      )}
      {columns.map((column) => {
        const value = column.dataIndex ? record[column.dataIndex] : record[column.key];
        const cellContent = column.render ? column.render(value, record, index) : value;

        return (
          <td
            key={column.key}
            className={styles.td}
            style={{
              textAlign: column.align || 'left',
              position: column.fixed ? 'sticky' : undefined,
              left: column.fixed === 'left' ? 0 : undefined,
              right: column.fixed === 'right' ? 0 : undefined,
            }}
          >
            {cellContent}
          </td>
        );
      })}
    </tr>
  );
};